package com.microservice.usuario.service;

import com.microservice.usuario.entitie.Usuario;
import com.microservice.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MascotasClientService mascotasClientService;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Juan");
        usuario.setApellido("Pérez");
        usuario.setEmail("juan@test.com");
        usuario.setRut("12345678-9");
        usuario.setTelefono(987654321);
        usuario.setPassword("password123");
        usuario.setRol(Usuario.Rol.USER);
        usuario.setActivo(true);
    }

    // ─── crear ────────────────────────────────────────────────────────────────

    @Test
    void crear_usuarioNuevo_debeGuardar() {
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(usuarioRepository.existsByRut(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        Usuario resultado = usuarioService.crear(usuario);

        assertThat(resultado.getNombre()).isEqualTo("Juan");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void crear_emailDuplicado_debeLanzarExcepcion() {
        when(usuarioRepository.existsByEmail("juan@test.com")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.crear(usuario))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Email");
    }

    @Test
    void crear_rutDuplicado_debeLanzarExcepcion() {
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(usuarioRepository.existsByRut("12345678-9")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.crear(usuario))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Rut");
    }

    // ─── ObtenerPorId ─────────────────────────────────────────────────────────

    @Test
    void obtenerPorId_existente_debeRetornar() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        Usuario resultado = usuarioService.ObtenerPorId(1L);

        assertThat(resultado.getEmail()).isEqualTo("juan@test.com");
    }

    @Test
    void obtenerPorId_inexistente_debeLanzarExcepcion() {
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.ObtenerPorId(999L))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("no encontrado");
    }

    // ─── listarTodos ──────────────────────────────────────────────────────────

    @Test
    void listarTodos_debeRetornarLista() {
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));

        assertThat(usuarioService.listarTodos()).hasSize(1);
    }

    // ─── eliminar ─────────────────────────────────────────────────────────────

    @Test
    void eliminar_existente_debeEliminar() {
        when(usuarioRepository.existsById(1L)).thenReturn(true);
        doNothing().when(usuarioRepository).deleteById(1L);

        assertThatCode(() -> usuarioService.eliminar(1L)).doesNotThrowAnyException();
        verify(usuarioRepository).deleteById(1L);
    }

    @Test
    void eliminar_inexistente_debeLanzarExcepcion() {
        when(usuarioRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> usuarioService.eliminar(999L))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("no encontrado");
    }

    // ─── actualizar ───────────────────────────────────────────────────────────

    @Test
    void actualizar_existente_debeActualizar() {
        Usuario actualizado = new Usuario();
        actualizado.setNombre("Carlos");
        actualizado.setApellido("López");
        actualizado.setEmail("carlos@test.com");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario resultado = usuarioService.actualizar(1L, actualizado);

        assertThat(resultado.getNombre()).isEqualTo("Carlos");
        assertThat(resultado.getApellido()).isEqualTo("López");
    }

    // ─── buscarPorEmail ───────────────────────────────────────────────────────

    @Test
    void buscarPorEmail_existente_debeRetornar() {
        when(usuarioRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(usuario));

        Usuario resultado = usuarioService.buscarPorEmail("juan@test.com");

        assertThat(resultado).isNotNull();
        assertThat(resultado.getEmail()).isEqualTo("juan@test.com");
    }

    @Test
    void buscarPorEmail_null_debeRetornarNull() {
        assertThat(usuarioService.buscarPorEmail(null)).isNull();
    }

    // ─── existsByEmail / existsByRut ──────────────────────────────────────────

    @Test
    void existsByEmail_existente_debeRetornarTrue() {
        when(usuarioRepository.existsByEmail("juan@test.com")).thenReturn(true);
        assertThat(usuarioService.existsByEmail("juan@test.com")).isTrue();
    }

    @Test
    void existsByRut_existente_debeRetornarTrue() {
        when(usuarioRepository.existsByRut("12345678-9")).thenReturn(true);
        assertThat(usuarioService.existsByRut("12345678-9")).isTrue();
    }
}