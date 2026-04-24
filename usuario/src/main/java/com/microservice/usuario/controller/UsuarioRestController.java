package com.microservice.usuario.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.gateway.apigateway.security.JwtUtil;
import com.microservice.usuario.service.UsuarioService;

import com.microservice.usuario.entitie.Usuario;




@RequestMapping("/api/usuario")
@RestController
public class UsuarioRestController {
    
    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private JwtUtil jwtUtil;

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

        if(!callerIsAdmin){
            usuario.setRol(Usuario.Rol.USER);
        } else{
            if (usuario.getRol() == null) usuario.setRol(Usuario.Rol.USER);
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
    public ResponseEntity<Usuario> ObtenerPorId(@PathVariable Long id){
        try {
            Usuario existe = usuarioService.obtenerUsuarioConMascotas(id);
            return ResponseEntity.ok(existe);
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
    public ResponseEntity<List<Usuario>> listarTodos(){
        List<Usuario> usuarios = usuarioService.listarTodos();
        return ResponseEntity.ok(usuarios);
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
