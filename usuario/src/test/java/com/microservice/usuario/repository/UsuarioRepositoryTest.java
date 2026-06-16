package com.microservice.usuario.repository;

import com.microservice.usuario.entitie.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application.properties")
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();

        usuario = new Usuario();
        usuario.setNombre("Juan");
        usuario.setApellido("Pérez");
        usuario.setEmail("juan@test.com");
        usuario.setRut("12345678-9");
        usuario.setTelefono(987654321);
        usuario.setPassword("hashed");
        usuario.setRol(Usuario.Rol.USER);
        usuario.setActivo(true);

        usuarioRepository.save(usuario);
    }

    @Test
    void findByEmail_existente_debeRetornar() {
        Optional<Usuario> resultado = usuarioRepository.findByEmail("juan@test.com");
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNombre()).isEqualTo("Juan");
    }

    @Test
    void findByEmail_inexistente_debeRetornarVacio() {
        assertThat(usuarioRepository.findByEmail("noexiste@test.com")).isEmpty();
    }

    @Test
    void existsByEmail_existente_debeRetornarTrue() {
        assertThat(usuarioRepository.existsByEmail("juan@test.com")).isTrue();
    }

    @Test
    void existsByEmail_inexistente_debeRetornarFalse() {
        assertThat(usuarioRepository.existsByEmail("noexiste@test.com")).isFalse();
    }

    @Test
    void existsByRut_existente_debeRetornarTrue() {
        assertThat(usuarioRepository.existsByRut("12345678-9")).isTrue();
    }

    @Test
    void existsByRut_inexistente_debeRetornarFalse() {
        assertThat(usuarioRepository.existsByRut("00000000-0")).isFalse();
    }

    @Test
    void save_debeGenerarId() {
        Usuario nuevo = new Usuario();
        nuevo.setEmail("nuevo@test.com");
        nuevo.setRut("99999999-9");
        Usuario guardado = usuarioRepository.save(nuevo);
        assertThat(guardado.getId()).isNotNull();
    }

    @Test
    void deleteById_debeEliminar() {
        Long id = usuario.getId();
        usuarioRepository.deleteById(id);
        assertThat(usuarioRepository.findById(id)).isEmpty();
    }
}