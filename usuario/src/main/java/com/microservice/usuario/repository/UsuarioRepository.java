package com.microservice.usuario.repository;

import java.util.Optional;

import com.microservice.usuario.entitie.Usuario;

import org.springframework.data.repository.CrudRepository;

public interface UsuarioRepository extends CrudRepository<Usuario, Long>{
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByRut(String rut);

}
