package com.microservice.usuario.service;
import java.util.List;
import com.microservice.usuario.entitie.Usuario;

public interface UsuarioService {

    Usuario crear(Usuario usuario);
    boolean existsByEmail(String email);
    boolean existsByRut(String rut);
    Usuario ObtenerPorId(Long id);
    Usuario obtenerUsuarioConMascotas(Long id);
    List<Usuario> listarTodos();
    void eliminar(Long id);
    Usuario actualizar(Long id, Usuario usuarioActualizado);
}

