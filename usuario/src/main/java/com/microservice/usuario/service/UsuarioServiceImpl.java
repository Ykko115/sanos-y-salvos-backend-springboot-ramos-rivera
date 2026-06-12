package com.microservice.usuario.service;
 
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
 
import com.microservice.usuario.entitie.Usuario;
import com.microservice.usuario.entitie.dto.ActualizarUsuarioDTO;
import com.microservice.usuario.entitie.dto.CrearUsuarioDTO;
import com.microservice.usuario.entitie.dto.MascotaDTO;
import com.microservice.usuario.repository.UsuarioRepository;
 
@Service
public class UsuarioServiceImpl implements UsuarioService {
    
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final MascotasClientService mascotasClientService;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository,
                              PasswordEncoder passwordEncoder,
                              MascotasClientService mascotasClientService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.mascotasClientService = mascotasClientService;
    }
 
    @Override
    public Usuario crear(CrearUsuarioDTO dto) {
        if (dto.getEmail() != null && usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("el Email ya ah sido registrado");
        }
        if (dto.getRut() != null && usuarioRepository.existsByRut(dto.getRut())) {
            throw new IllegalArgumentException("el Rut ya ah sido registrado");
        }
 
        Usuario usuario = new Usuario();
        usuario.setRut(dto.getRut());
        usuario.setNombre(dto.getNombre());
        usuario.setApellido(dto.getApellido());
        usuario.setEmail(dto.getEmail());
        usuario.setTelefono(dto.getTelefono());
        usuario.setActivo(true); // siempre activo al crear
        // El rol se asigna desde el controller según permisos del llamante
        if (dto.getRol() != null) {
            try {
                usuario.setRol(Usuario.Rol.valueOf(dto.getRol().toUpperCase()));
            } catch (IllegalArgumentException ignored) {
                usuario.setRol(Usuario.Rol.USER);
            }
        } else {
            usuario.setRol(Usuario.Rol.USER);
        }
 
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            try {
                String hashed = passwordEncoder.encode(dto.getPassword());
                usuario.setPassword(hashed);
            } catch (Exception ex) {
                throw new IllegalStateException("Error hashing password", ex);
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
        .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
    } 
 
    @Override
    public Usuario obtenerUsuarioConMascotas(Long id) {
        Usuario usuario = ObtenerPorId(id);
        List<MascotaDTO> mascotas = mascotasClientService.obtenerMascotasPorUsuarioId(id);
        usuario.setMascotas(mascotas);
        return usuario;
    }
 
    @Override
    public List<Usuario> listarTodos(){
        return(List<Usuario>) usuarioRepository.findAll();
    }
 
    @Override
    public void eliminar(Long id){
        if(!usuarioRepository.existsById(id)){
            throw new NoSuchElementException("Usuario no encontrado");
        }
        usuarioRepository.deleteById(id);
    }
 
    /** Codifica el password del DTO y lo aplica al usuario si viene informado. */
    private void aplicarPasswordSiPresente(ActualizarUsuarioDTO dto, Usuario usuario) {
        if (dto.getPassword() == null) return;
        try {
            usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        } catch (Exception ex) {
            throw new IllegalStateException("Error hashing password", ex);
        }
    }
 
    @Override
    public Usuario actualizar(Long id, ActualizarUsuarioDTO dto) {
        Usuario usuario = ObtenerPorId(id);
        if (usuario == null) return null;
 
        if (dto.getNombre() != null)    usuario.setNombre(dto.getNombre());
        if (dto.getApellido() != null)  usuario.setApellido(dto.getApellido());
        if (dto.getEmail() != null)     usuario.setEmail(dto.getEmail());
        if (dto.getRut() != null)       usuario.setRut(dto.getRut());
        if (dto.getTelefono() != null && dto.getTelefono() != 0) usuario.setTelefono(dto.getTelefono());
 
        aplicarPasswordSiPresente(dto, usuario);

        if (dto.getRol() != null) {
            try {
                usuario.setRol(Usuario.Rol.valueOf(dto.getRol().toUpperCase()));
            } catch (IllegalArgumentException ignored) { /* rol inválido → no cambiar */ }
        }
        if (dto.getActivo() != null) {
            usuario.setActivo(dto.getActivo());
        }

        return usuarioRepository.save(usuario);
    }
 
    @Override
    public Usuario buscarPorEmail(String email) {
        if (email == null) return null;
        return usuarioRepository.findByEmail(email).orElse(null);
    }
}
