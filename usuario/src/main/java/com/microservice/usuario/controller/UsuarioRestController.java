package com.microservice.usuario.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gateway.apigateway.security.JwtUtil;
import com.microservice.usuario.entitie.Usuario;
import com.microservice.usuario.service.UsuarioService;
import com.microservice.usuario.entitie.dto.MascotaDTO;
import com.microservice.usuario.entitie.dto.UsuarioDTO;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;




@RequestMapping("/api/usuario")
@RestController
public class UsuarioRestController {
    
    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Value("${mascotas.service.url:http://localhost:8082}")
    private String mascotasServiceUrl;

        // DTO para login
        public static class LoginRequest {
            private String email;
            private String password;

            public String getEmail() { return email; }
            public void setEmail(String email) { this.email = email; }
            public String getPassword() { return password; }
            public void setPassword(String password) { this.password = password; }
        }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Usuario usuario = usuarioService.buscarPorEmail(loginRequest.getEmail());
            if (usuario == null) {
                Map<String, String> err = new HashMap<>();
                err.put("error", "Usuario no encontrado");
                return ResponseEntity.status(401).body(err);
            }
            // Validar password usando BCrypt
            if (!passwordEncoder.matches(loginRequest.getPassword(), usuario.getPassword())) {
                Map<String, String> err = new HashMap<>();
                err.put("error", "Credenciales inválidas");
                return ResponseEntity.status(401).body(err);
            }
            String rol = usuario.getRol() == Usuario.Rol.ADMIN ? "ROLE_ADMIN" : "ROLE_USER";
            String token = jwtUtil.generateToken(usuario.getEmail(), List.of(rol));
            Map<String, Object> resp = new HashMap<>();
            resp.put("token", token);
            Map<String, Object> userResp = new HashMap<>();
            userResp.put("id", usuario.getId());
            userResp.put("rut", usuario.getRut());
            userResp.put("nombre", usuario.getNombre());
            userResp.put("apellido", usuario.getApellido());
            userResp.put("email", usuario.getEmail());
            userResp.put("telefono", usuario.getTelefono());
            userResp.put("activo", usuario.getActivo());
            userResp.put("rol", usuario.getRol());
            resp.put("user", userResp);
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Error en el login");
            return ResponseEntity.status(500).body(err);
        }
    }
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Usuario usuario){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean callerIsAdmin = false;
        if(auth != null && auth.isAuthenticated() && auth.getAuthorities() != null){
            for(GrantedAuthority ga : auth.getAuthorities()) {
                if("ROLE_ADMIN".equals(ga.getAuthority()) || "ADMIN".equals(ga.getAuthority())){
                    callerIsAdmin = true;
                    break;
                }
            }
        }


        // Si el usuario autenticado NO es admin, forzar rol USER
        if(!callerIsAdmin){
            usuario.setRol(Usuario.Rol.USER);
        } else {
            // Si es admin y no se especifica rol, asignar USER por defecto
            if (usuario.getRol() == null) {
                usuario.setRol(Usuario.Rol.USER);
            }
            // Si es admin y se especifica rol, se respeta el rol enviado
        }

        try {
            // Generate token before persisting so a token error never leaves a half-successful create.
            String token = null;
            if (usuario.getEmail() != null) {
                String rol = usuario.getRol() == Usuario.Rol.ADMIN ? "ROLE_ADMIN" : "ROLE_USER";
                token = jwtUtil.generateToken(usuario.getEmail(), List.of(rol));
            }

            Usuario nuevoUsuario = usuarioService.crear(usuario);
            Map<String, Object> resp = new HashMap<>();
            if(token != null){
                resp.put("token", token);
            }
            if (nuevoUsuario != null) {
                Map<String, Object> userResp = new HashMap<>();
                userResp.put("id", nuevoUsuario.getId());
                userResp.put("rut", nuevoUsuario.getRut());
                userResp.put("nombre", nuevoUsuario.getNombre());
                userResp.put("apellido", nuevoUsuario.getApellido());
                userResp.put("email", nuevoUsuario.getEmail());
                userResp.put("telefono", nuevoUsuario.getTelefono());
                userResp.put("activo", nuevoUsuario.getActivo());
                userResp.put("rol", nuevoUsuario.getRol());
                resp.put("user", userResp);
            } else {
                resp.put("user", null);
            }
            return ResponseEntity.ok(resp);            
        }catch (IllegalArgumentException ex) {
            Map<String, String> err = new HashMap<>();
            err.put("error", ex.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> ObtenerPorId(@PathVariable Long id, @RequestParam(value = "plain", required = false) Boolean plain){
        try {
            Usuario existe = usuarioService.ObtenerPorId(id);
            if (existe == null) return ResponseEntity.notFound().build();
            // Si plain=true, devolver solo los datos planos del usuario (para WebClient de mascotas)
            if (plain != null && plain) {
                return ResponseEntity.ok(new UsuarioDTO(
                    existe.getId(),
                    existe.getRut(),
                    existe.getNombre(),
                    existe.getApellido(),
                    existe.getEmail(),
                    existe.getTelefono(),
                    existe.getActivo(),
                    existe.getRol() != null ? existe.getRol().name() : null
                ));
            }
            MascotaDTO[] mascotas = null;
            try {
                mascotas = webClientBuilder.build()
                        .get()
                        .uri(mascotasServiceUrl + "/api/mascotas/usuario/" + id)
                        .retrieve()
                        .bodyToMono(MascotaDTO[].class)
                        .block();
            } catch (Exception ignored) {}
            return ResponseEntity.ok(new UsuarioConMascotasResponse(existe, mascotas));
        } catch (RuntimeException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/mascotas")
    public ResponseEntity<Usuario> obtenerUsuarioConMascotas(@PathVariable Long id) {
        try {
            Usuario respuesta = usuarioService.obtenerUsuarioConMascotas(id);
            return ResponseEntity.ok(respuesta);
        } catch (RuntimeException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<?> listarTodos(){
        List<Usuario> usuarios = usuarioService.listarTodos();
        List<UsuarioConMascotasResponse> result = usuarios.stream().map(usuario -> {
            MascotaDTO[] mascotas = null;
            try {
                mascotas = webClientBuilder.build()
                        .get()
                        .uri(mascotasServiceUrl + "/api/mascotas/usuario/" + usuario.getId())
                        .retrieve()
                        .bodyToMono(MascotaDTO[].class)
                        .block();
            } catch (Exception ignored) {}
            return new UsuarioConMascotasResponse(usuario, mascotas);
        }).toList();
        return ResponseEntity.ok(result);
    }

    // DTO de respuesta enriquecida
    public static class UsuarioConMascotasResponse {
        private Usuario usuario;
        private MascotaDTO[] mascotas;

        public UsuarioConMascotasResponse(Usuario usuario, MascotaDTO[] mascotas) {
            this.usuario = usuario;
            this.mascotas = mascotas;
        }

        public Usuario getUsuario() { return usuario; }
        public MascotaDTO[] getMascotas() { return mascotas; }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id){
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizar(@PathVariable Long id, @RequestBody Usuario usuarioActualizado){
        Usuario usuario = usuarioService.actualizar(id, usuarioActualizado);
        return ResponseEntity.ok(usuario);
    }    
}
