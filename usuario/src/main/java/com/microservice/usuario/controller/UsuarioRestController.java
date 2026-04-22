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
            Usuario nuevoUsuario = usuarioService.crear(usuario);
            if(nuevoUsuario !=null) nuevoUsuario.setPassword(null);
            Map<String, Object> resp = new HashMap<>();
            if(nuevoUsuario != null && nuevoUsuario.getEmail() != null){
                String rol = nuevoUsuario.getRol() == Usuario.Rol.ADMIN ? "ROLE_ADMIN" : "ROLE_USER";
                String token = jwtUtil.generateToken(nuevoUsuario.getEmail(), List.of(rol));
                resp.put("token", token);
            }
            resp.put("user", nuevoUsuario);
            return ResponseEntity.ok(resp);            
        }catch (IllegalArgumentException ex) {
            Map<String, String> err = new HashMap<>();
            err.put("error", ex.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> ObtenerPorId(@PathVariable Long id){
        Usuario existe = usuarioService.ObtenerPorId(id);
        return ResponseEntity.ok(existe);
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
