package com.microservice.usuario.service;
import java.util.List;

import com.microservice.usuario.entitie.Usuario;
import com.microservice.usuario.entitie.dto.ActualizarUsuarioDTO;
import com.microservice.usuario.entitie.dto.CrearUsuarioDTO;

public interface UsuarioService {
    
    Usuario crear(CrearUsuarioDTO dto);
    boolean existsByEmail(String email);
    boolean existsByRut(String rut);
    Usuario ObtenerPorId(Long id);
    Usuario obtenerUsuarioConMascotas(Long id);
    List<Usuario> listarTodos();
    void eliminar(Long id);
    Usuario actualizar(Long id, ActualizarUsuarioDTO dto);
    Usuario buscarPorEmail(String email);
}

