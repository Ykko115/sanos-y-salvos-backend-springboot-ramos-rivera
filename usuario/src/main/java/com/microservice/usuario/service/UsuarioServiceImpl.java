package com.microservice.usuario.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.microservice.usuario.entitie.Usuario;
import com.microservice.usuario.repository.UsuarioRepository;

@Service
public class UsuarioServiceImpl implements UsuarioService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Usuario crear(Usuario usuario){
        if(usuario.getEmail() != null && usuarioRepository.existsByEmail(usuario.getEmail())){
            throw new IllegalArgumentException("el Email ya ah sido registrado");
        }
        if(usuario.getRut() != null && usuarioRepository.existsByRut(usuario.getRut())) {
            throw new IllegalArgumentException("el Rut ya ah sido registrado");
        }
        if (usuario.getActivo() == null) {
            usuario.setActivo(true);
        }
        if(usuario.getPassword() != null && !usuario.getPassword().isBlank()){
            try {
                String hashed = passwordEncoder.encode(usuario.getPassword());
                usuario.setPassword(hashed);
            } catch (Exception ex){
                throw new RuntimeException("Error hashing password", ex);
            }
        }
        return usuarioRepository.save(usuario);
    }

    @Override
    public boolean existsByEmail(String email){
        if (email == null) return false;
        return usuarioRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByRut(String rut){
        if(rut == null) return false;
        return usuarioRepository.existsByRut(rut);
    }

    @Override
    public Usuario ObtenerPorId(Long id){
        return usuarioRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    } 

    @Override
    public List<Usuario> listarTodos(){
        return(List<Usuario>) usuarioRepository.findAll();
    }

    @Override
    public void eliminar(Long id){
        if(!usuarioRepository.existsById(id)){
            throw new RuntimeException("Usuario no encontrado");
        }
        usuarioRepository.deleteById(id);
    }

    @Override
    public Usuario actualizar(Long id, Usuario usuarioActualizado) {
        Usuario usuario = ObtenerPorId(id);
        if (usuario != null) {
            if (usuarioActualizado.getNombre() != null) usuario.setNombre(usuarioActualizado.getNombre());
            if (usuarioActualizado.getApellido() != null) usuario.setApellido(usuarioActualizado.getApellido());
            if (usuarioActualizado.getEmail() != null) usuario.setEmail(usuarioActualizado.getEmail());
            if (usuarioActualizado.getRut() != null) usuario.setRut(usuarioActualizado.getRut());
            if (usuarioActualizado.getTelefono() != 0) usuario.setTelefono(usuarioActualizado.getTelefono());
            if (usuarioActualizado.getPassword() != null) {
                try{
                    String hashed = passwordEncoder.encode(usuarioActualizado.getPassword());
                    usuario.setPassword(hashed);
                } catch (Exception ex) {
                    throw new RuntimeException("Error hashing password" , ex);
                }
            }
            if (usuarioActualizado.getRol() != null) usuario.setRol(usuarioActualizado.getRol());
            if (usuarioActualizado.getActivo() != null) usuario.setActivo(usuarioActualizado.getActivo());
            return usuarioRepository.save(usuario);
        }
        return null;
    }
}