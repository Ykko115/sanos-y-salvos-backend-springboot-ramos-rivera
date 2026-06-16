package mascotas.microservice.mascotas.controller;

import mascotas.microservice.mascotas.entity.Mascotas;
import mascotas.microservice.mascotas.service.MascotasService;
import mascotas.microservice.mascotas.service.NotificacionMatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MascotasController.class)
class MascotasControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MascotasService mascotasService;

    @MockitoBean
    private NotificacionMatchService notificacionMatchService;

    @MockitoBean
    private WebClient.Builder webClientBuilder;

    private Mascotas mascota;

    @BeforeEach
    void setUp() {
        mascota = new Mascotas();
        mascota.setId(1L);
        mascota.setNombre("Fido");
        mascota.setEspecie(Mascotas.Especie.PERRO);
        mascota.setRaza("Labrador");
        mascota.setEdad(3);
        mascota.setDescripcion("Perro amigable");
        mascota.setUsuarioId(1L);
    }

    private String toJson(Mascotas m) {
        return String.format(
            "{\"id\":%d,\"nombre\":\"%s\",\"especie\":\"%s\",\"raza\":\"%s\"," +
            "\"edad\":%d,\"descripcion\":\"%s\",\"usuarioId\":%d}",
            m.getId(), m.getNombre(), m.getEspecie(), m.getRaza(),
            m.getEdad(), m.getDescripcion(), m.getUsuarioId()
        );
    }

    @Test
    @WithMockUser
    void crearMascota_debeRetornar201() throws Exception {
        when(mascotasService.crearMascota(any(Mascotas.class))).thenReturn(mascota);

        mockMvc.perform(post("/api/mascotas")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(mascota)))
            .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser
    void obtenerMascotaPorId_existente_debeRetornar200() throws Exception {
        when(mascotasService.obtenerMascotaPorId(1L)).thenReturn(Optional.of(mascota));

        // Simular WebClient para enriquecer con usuario
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        when(responseSpec.bodyToMono(any(Class.class))).thenReturn(reactor.core.publisher.Mono.empty());
        @SuppressWarnings("rawtypes")
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(headersSpec.header(anyString(), anyString())).thenReturn(headersSpec);
        @SuppressWarnings("rawtypes")
        WebClient.RequestHeadersUriSpec uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        when(uriSpec.uri(anyString())).thenReturn(headersSpec);
        WebClient webClientMock = mock(WebClient.class);
        when(webClientMock.get()).thenReturn(uriSpec);
        when(webClientBuilder.build()).thenReturn(webClientMock);

        mockMvc.perform(get("/api/mascotas/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mascota.nombre").value("Fido"));
    }

    @Test
    @WithMockUser
    void obtenerMascotaPorId_inexistente_debeRetornar404() throws Exception {
        when(mascotasService.obtenerMascotaPorId(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/mascotas/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void obtenerTodasLasMascotas_debeRetornarLista() throws Exception {
        when(mascotasService.obtenerTodasLasMascotas()).thenReturn(List.of(mascota));

        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        when(responseSpec.bodyToMono(any(Class.class))).thenReturn(reactor.core.publisher.Mono.empty());
        @SuppressWarnings("rawtypes")
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(headersSpec.header(anyString(), anyString())).thenReturn(headersSpec);
        @SuppressWarnings("rawtypes")
        WebClient.RequestHeadersUriSpec uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        when(uriSpec.uri(anyString())).thenReturn(headersSpec);
        WebClient webClientMock = mock(WebClient.class);
        when(webClientMock.get()).thenReturn(uriSpec);
        when(webClientBuilder.build()).thenReturn(webClientMock);

        mockMvc.perform(get("/api/mascotas"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser
    void eliminarMascota_existente_debeRetornar204() throws Exception {
        doNothing().when(mascotasService).eliminarMascota(1L);

        mockMvc.perform(delete("/api/mascotas/1").with(csrf()))
            .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void eliminarMascota_inexistente_debeRetornar404() throws Exception {
        doThrow(new RuntimeException("No encontrada"))
            .when(mascotasService).eliminarMascota(999L);

        mockMvc.perform(delete("/api/mascotas/999").with(csrf()))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void obtenerMascotasPorEspecie_debeRetornarLista() throws Exception {
        when(mascotasService.obtenerMascotasPorEspecie(Mascotas.Especie.PERRO)).thenReturn(List.of(mascota));

        mockMvc.perform(get("/api/mascotas/especie/PERRO"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser
    void obtenerMascotasPorUsuarioId_debeRetornarLista() throws Exception {
        when(mascotasService.obtenerMascotasPorUsuarioId(1L)).thenReturn(List.of(mascota));

        mockMvc.perform(get("/api/mascotas/usuario/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser
    void actualizarMascota_existente_debeRetornar200() throws Exception {
        when(mascotasService.actualizarMascota(eq(1L), any(Mascotas.class))).thenReturn(mascota);

        mockMvc.perform(put("/api/mascotas/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(mascota)))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void actualizarMascota_inexistente_debeRetornar404() throws Exception {
        when(mascotasService.actualizarMascota(eq(999L), any(Mascotas.class)))
            .thenThrow(new RuntimeException("no encontrada"));

        mockMvc.perform(put("/api/mascotas/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(mascota)))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void actualizarEstado_valido_debeRetornar200() throws Exception {
        mascota.setEstado(Mascotas.Estado.ENCONTRADO);
        when(mascotasService.actualizarEstado(1L, Mascotas.Estado.ENCONTRADO)).thenReturn(mascota);

        mockMvc.perform(put("/api/mascotas/1/estado")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"estado\":\"ENCONTRADO\"}"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void actualizarEstado_invalido_debeRetornar400() throws Exception {
        mockMvc.perform(put("/api/mascotas/1/estado")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"estado\":\"INVALIDO\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void actualizarEstado_sinCampo_debeRetornar400() throws Exception {
        mockMvc.perform(put("/api/mascotas/1/estado")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void obtenerMascotaPorNombre_existente_debeRetornar200() throws Exception {
        when(mascotasService.obtenerMascotaPorNombre("Fido")).thenReturn(Optional.of(mascota));

        mockMvc.perform(get("/api/mascotas/nombre/Fido"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void obtenerMascotaPorNombre_inexistente_debeRetornar404() throws Exception {
        when(mascotasService.obtenerMascotaPorNombre("Desconocido")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/mascotas/nombre/Desconocido"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void obtenerMascotasPorRaza_debeRetornarLista() throws Exception {
        when(mascotasService.obtenerMascotasPorRaza("Labrador")).thenReturn(List.of(mascota));

        mockMvc.perform(get("/api/mascotas/raza/Labrador"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser
    void obtenerEnums_debeRetornar200() throws Exception {
        mockMvc.perform(get("/api/mascotas/enums"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void obtenerTodasConEstado_valido_debeRetornarFiltradas() throws Exception {
        mascota.setEstado(Mascotas.Estado.PERDIDO);
        when(mascotasService.obtenerMascotasPorEstado(Mascotas.Estado.PERDIDO))
            .thenReturn(List.of(mascota));

        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        when(responseSpec.bodyToMono(any(Class.class))).thenReturn(reactor.core.publisher.Mono.empty());
        @SuppressWarnings("rawtypes")
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(headersSpec.header(anyString(), anyString())).thenReturn(headersSpec);
        @SuppressWarnings("rawtypes")
        WebClient.RequestHeadersUriSpec uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        when(uriSpec.uri(anyString())).thenReturn(headersSpec);
        WebClient webClientMock = mock(WebClient.class);
        when(webClientMock.get()).thenReturn(uriSpec);
        when(webClientBuilder.build()).thenReturn(webClientMock);

        mockMvc.perform(get("/api/mascotas").param("estado", "PERDIDO"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void obtenerTodasConEstado_invalido_debeRetornar400() throws Exception {
        mockMvc.perform(get("/api/mascotas").param("estado", "INVALIDO"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void obtenerFoto_mascotaConFoto_debeRetornar200() throws Exception {
        // PNG 1x1 en base64 para simular una foto real
        String base64Png = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";
        mascota.setFotoUrl("data:image/png;base64," + base64Png);
        when(mascotasService.obtenerMascotaPorId(1L)).thenReturn(Optional.of(mascota));

        mockMvc.perform(get("/api/mascotas/1/foto"))
            .andExpect(status().isOk())
            .andExpect(result -> {
                String contentType = result.getResponse().getContentType();
                assert contentType != null && contentType.contains("image/png");
            });
    }

    @Test
    @WithMockUser
    void obtenerFoto_mascotaInexistente_debeRetornar404() throws Exception {
        when(mascotasService.obtenerMascotaPorId(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/mascotas/999/foto"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void obtenerFoto_mascotaSinFoto_debeRetornar404() throws Exception {
        mascota.setFotoUrl(null);
        when(mascotasService.obtenerMascotaPorId(1L)).thenReturn(Optional.of(mascota));

        mockMvc.perform(get("/api/mascotas/1/foto"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void obtenerFoto_mascotaConFotoNoBase64_debeRetornar404() throws Exception {
        mascota.setFotoUrl("https://cdn.example.com/foto.jpg");
        when(mascotasService.obtenerMascotaPorId(1L)).thenReturn(Optional.of(mascota));

        mockMvc.perform(get("/api/mascotas/1/foto"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void obtenerFoto_base64Invalido_debeRetornar400() throws Exception {
        mascota.setFotoUrl("data:image/png;base64,!!!base64invalido!!!");
        when(mascotasService.obtenerMascotaPorId(1L)).thenReturn(Optional.of(mascota));

        mockMvc.perform(get("/api/mascotas/1/foto"))
            .andExpect(status().isBadRequest());
    }
}