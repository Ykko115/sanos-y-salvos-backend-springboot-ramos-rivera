package com.microservice.usuario.controller;

import com.gateway.apigateway.security.JwtUtil;
import com.microservice.usuario.entitie.Usuario;
import com.microservice.usuario.entitie.dto.ActualizarUsuarioDTO;
import com.microservice.usuario.entitie.dto.CrearUsuarioDTO;
import com.microservice.usuario.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioRestController.class)
class UsuarioRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private WebClient.Builder webClientBuilder;

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
        usuario.setPassword("hashed");
        usuario.setRol(Usuario.Rol.USER);
        usuario.setActivo(true);
    }

    // ─── login ────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser
    void login_credencialesValidas_debeRetornarToken() throws Exception {
        when(usuarioService.buscarPorEmail("juan@test.com")).thenReturn(usuario);
        when(passwordEncoder.matches("password123", "hashed")).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyList())).thenReturn("token.jwt.test");

        mockMvc.perform(post("/api/usuario/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"juan@test.com\",\"password\":\"password123\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("token.jwt.test"));
    }

    @Test
    @WithMockUser
    void login_usuarioNoExiste_debeRetornar401() throws Exception {
        when(usuarioService.buscarPorEmail("noexiste@test.com")).thenReturn(null);

        mockMvc.perform(post("/api/usuario/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"noexiste@test.com\",\"password\":\"cualquiera\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void login_passwordIncorrecta_debeRetornar401() throws Exception {
        when(usuarioService.buscarPorEmail("juan@test.com")).thenReturn(usuario);
        when(passwordEncoder.matches("wrongpass", "hashed")).thenReturn(false);

        mockMvc.perform(post("/api/usuario/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"juan@test.com\",\"password\":\"wrongpass\"}"))
            .andExpect(status().isUnauthorized());
    }

    // ─── crear ────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser
    void crear_usuarioValido_debeRetornar200() throws Exception {
        when(jwtUtil.generateToken(anyString(), anyList())).thenReturn("token.jwt.test");
        when(usuarioService.crear(any(CrearUsuarioDTO.class))).thenReturn(usuario);

        mockMvc.perform(post("/api/usuario")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Juan\",\"email\":\"juan@test.com\",\"password\":\"pass\",\"rut\":\"12345678-9\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.user.nombre").value("Juan"));
    }

    @Test
    @WithMockUser
    void crear_emailDuplicado_debeRetornar400() throws Exception {
        when(jwtUtil.generateToken(anyString(), anyList())).thenReturn("token");
        when(usuarioService.crear(any(CrearUsuarioDTO.class)))
            .thenThrow(new IllegalArgumentException("el Email ya ah sido registrado"));

        mockMvc.perform(post("/api/usuario")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Juan\",\"email\":\"juan@test.com\",\"password\":\"pass\",\"rut\":\"12345678-9\"}"))
            .andExpect(status().isBadRequest());
    }

    // ─── obtenerPorId ─────────────────────────────────────────────────────────

    @Test
    @WithMockUser
    void obtenerPorId_existente_debeRetornar200() throws Exception {
        when(usuarioService.ObtenerPorId(1L)).thenReturn(usuario);

        // Mock WebClient para mascotas
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        when(responseSpec.bodyToMono(any(Class.class))).thenReturn(reactor.core.publisher.Mono.empty());
        @SuppressWarnings("rawtypes")
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        @SuppressWarnings("rawtypes")
        WebClient.RequestHeadersUriSpec uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        when(uriSpec.uri(anyString())).thenReturn(headersSpec);
        WebClient webClientMock = mock(WebClient.class);
        when(webClientMock.get()).thenReturn(uriSpec);
        when(webClientBuilder.build()).thenReturn(webClientMock);

        mockMvc.perform(get("/api/usuario/1"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void obtenerPorId_inexistente_debeRetornar404() throws Exception {
        when(usuarioService.ObtenerPorId(999L))
            .thenThrow(new RuntimeException("Usuario no encontrado"));

        mockMvc.perform(get("/api/usuario/999"))
            .andExpect(status().isNotFound());
    }

    // ─── listarTodos ──────────────────────────────────────────────────────────

    @Test
    @WithMockUser
    void listarTodos_debeRetornarLista() throws Exception {
        when(usuarioService.listarTodos()).thenReturn(List.of(usuario));

        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        when(responseSpec.bodyToMono(any(Class.class))).thenReturn(reactor.core.publisher.Mono.empty());
        @SuppressWarnings("rawtypes")
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        @SuppressWarnings("rawtypes")
        WebClient.RequestHeadersUriSpec uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        when(uriSpec.uri(anyString())).thenReturn(headersSpec);
        WebClient webClientMock = mock(WebClient.class);
        when(webClientMock.get()).thenReturn(uriSpec);
        when(webClientBuilder.build()).thenReturn(webClientMock);

        mockMvc.perform(get("/api/usuario"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    // ─── eliminar ─────────────────────────────────────────────────────────────

    @Test
    @WithMockUser
    void eliminar_existente_debeRetornar204() throws Exception {
        doNothing().when(usuarioService).eliminar(1L);

        mockMvc.perform(delete("/api/usuario/1").with(csrf()))
            .andExpect(status().isNoContent());
    }

    // ─── actualizar ───────────────────────────────────────────────────────────

    @Test
    @WithMockUser
    void actualizar_existente_debeRetornar200() throws Exception {
        when(usuarioService.actualizar(eq(1L), any(ActualizarUsuarioDTO.class))).thenReturn(usuario);

        mockMvc.perform(put("/api/usuario/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Carlos\"}"))
            .andExpect(status().isOk());
    }
}