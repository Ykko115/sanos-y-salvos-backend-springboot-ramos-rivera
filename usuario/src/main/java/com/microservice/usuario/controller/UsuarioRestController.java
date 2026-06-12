package com.microservice.usuario.controller;
 
import java.util.HashMap;
import java.util.List;
import java.util.Map;
 
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
 
import com.microservice.usuario.security.JwtUtil;
import com.microservice.usuario.entitie.Usuario;
import com.microservice.usuario.entitie.dto.ActualizarUsuarioDTO;
import com.microservice.usuario.entitie.dto.CrearUsuarioDTO;
import com.microservice.usuario.entitie.dto.MascotaDTO;
import com.microservice.usuario.exception.ServicioNoDisponibleException;
import com.microservice.usuario.service.UsuarioService;
 
@RequestMapping("/api/usuario")
@RestController
public class UsuarioRestController {
 
    // Constantes para evitar literales duplicados (java:S1192)
    private static final String ROLE_ADMIN  = "ROLE_ADMIN";
    private static final String ROLE_USER   = "ROLE_USER";
    private static final String ERROR_KEY   = "error";
 
    private final UsuarioService usuarioService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final WebClient.Builder webClientBuilder;

    @Value("${mascotas.service.url:http://mascotas:8082}")
    private String mascotasServiceUrl;

    public UsuarioRestController(UsuarioService usuarioService,
                                 JwtUtil jwtUtil,
                                 PasswordEncoder passwordEncoder,
                                 WebClient.Builder webClientBuilder) {
        this.usuarioService = usuarioService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.webClientBuilder = webClientBuilder;
    }
 
    // DTO para login
    public static class LoginRequest {
        private String email;
        private String password;
 
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
 
    // DTO de respuesta enriquecida
    public static class UsuarioConMascotasResponse {
        private final Usuario usuario;
        private final MascotaDTO[] mascotas;
 
        public UsuarioConMascotasResponse(Usuario usuario, MascotaDTO[] mascotas) {
            this.usuario = usuario;
            this.mascotas = mascotas;
        }
 
        public Usuario getUsuario() { return usuario; }
        public MascotaDTO[] getMascotas() { return mascotas; }
    }
 
    // ─── Helpers privados ────────────────────────────────────────────────────
 
    /** Devuelve true si el usuario autenticado tiene rol ADMIN. */
    private boolean callerIsAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        // getAuthorities() nunca retorna null en Spring Security; se itera directamente
        for (GrantedAuthority ga : auth.getAuthorities()) {
            if (ROLE_ADMIN.equals(ga.getAuthority()) || "ADMIN".equals(ga.getAuthority())) {
                return true;
            }
        }
        return false;
    }
 
    /** Construye el mapa de respuesta con los datos públicos de un usuario. */
    private Map<String, Object> buildUserMap(Usuario u) {
        Map<String, Object> map = new HashMap<>();
        map.put("id",       u.getId());
        map.put("rut",      u.getRut());
        map.put("nombre",   u.getNombre());
        map.put("apellido", u.getApellido());
        map.put("email",    u.getEmail());
        map.put("telefono", u.getTelefono());
        map.put("activo",   u.getActivo());
        map.put("rol",      u.getRol());
        return map;
    }
 
    /** Obtiene las mascotas de un usuario desde el microservicio mascotas. */
    private MascotaDTO[] fetchMascotas(Long usuarioId) {
        try {
            return webClientBuilder.build()
                    .get()
                    .uri(mascotasServiceUrl + "/api/mascotas/usuario/" + usuarioId)
                    .retrieve()
                    .bodyToMono(MascotaDTO[].class)
                    .block();
        } catch (Exception ignored) {
            return new MascotaDTO[0];
        }
    }
 
    // ─── Endpoints ───────────────────────────────────────────────────────────
 
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest loginRequest) {
        try {
            Usuario usuario = usuarioService.buscarPorEmail(loginRequest.getEmail());
            if (usuario == null) {
                return ResponseEntity.status(401).body(Map.of(ERROR_KEY, "Usuario no encontrado"));
            }
            if (!passwordEncoder.matches(loginRequest.getPassword(), usuario.getPassword())) {
                return ResponseEntity.status(401).body(Map.of(ERROR_KEY, "Credenciales inválidas"));
            }
            String rol = usuario.getRol() == Usuario.Rol.ADMIN ? ROLE_ADMIN : ROLE_USER;
            String token = jwtUtil.generateToken(usuario.getEmail(), List.of(rol));
            Map<String, Object> resp = new HashMap<>();
            resp.put("token", token);
            resp.put("user", buildUserMap(usuario));
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of(ERROR_KEY, "Error en el login"));
        }
    }
 
    @PostMapping
    public ResponseEntity<Map<String, Object>> crear(@RequestBody CrearUsuarioDTO dto) {
        // Si el llamante no es admin o no especificó rol, forzar USER
        if (!callerIsAdmin() || dto.getRol() == null) {
            dto.setRol("USER");
        }
        try {
            String token = null;
            if (dto.getEmail() != null) {
                String rol = "ADMIN".equalsIgnoreCase(dto.getRol()) ? ROLE_ADMIN : ROLE_USER;
                token = jwtUtil.generateToken(dto.getEmail(), List.of(rol));
            }
            Usuario nuevoUsuario = usuarioService.crear(dto);
            Map<String, Object> resp = new HashMap<>();
            if (token != null) resp.put("token", token);
            resp.put("user", nuevoUsuario != null ? buildUserMap(nuevoUsuario) : null);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, ex.getMessage()));
        }
    }
 
    @GetMapping("/{id}")
    public ResponseEntity<Object> obtenerPorId(
            @PathVariable Long id,
            @RequestParam(value = "plain", required = false) Boolean plain) {
        try {
            Usuario existe = usuarioService.ObtenerPorId(id);
            if (existe == null) return ResponseEntity.notFound().build();
            if (Boolean.TRUE.equals(plain)) {
                return ResponseEntity.ok(buildUserMap(existe));
            }
            return ResponseEntity.ok(new UsuarioConMascotasResponse(existe, fetchMascotas(id)));
        } catch (RuntimeException ex) {
            return ResponseEntity.notFound().build();
        }
    }
 
    @GetMapping("/{id}/mascotas")
    public ResponseEntity<Usuario> obtenerUsuarioConMascotas(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(usuarioService.obtenerUsuarioConMascotas(id));
        } catch (ServicioNoDisponibleException ex) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE).build();
        } catch (RuntimeException ex) {
            return ResponseEntity.notFound().build();
        }
    }
 
    @GetMapping
    public ResponseEntity<List<UsuarioConMascotasResponse>> listarTodos() {
        List<UsuarioConMascotasResponse> result = usuarioService.listarTodos().stream()
                .map(u -> new UsuarioConMascotasResponse(u, fetchMascotas(u.getId())))
                .toList();
        return ResponseEntity.ok(result);
    }
 
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
 
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizar(@PathVariable Long id, @RequestBody ActualizarUsuarioDTO dto) {
        return ResponseEntity.ok(usuarioService.actualizar(id, dto));
    }
}
