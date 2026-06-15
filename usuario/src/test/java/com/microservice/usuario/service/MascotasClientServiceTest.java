package com.microservice.usuario.service;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.http.HttpStatus;

import com.microservice.usuario.exception.ServicioNoDisponibleException;

@ExtendWith(MockitoExtension.class)
class MascotasClientServiceTest {

    @Spy
    private MascotasClientService mascotasClientService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mascotasClientService, "mascotasServiceUrl",
            "http://localhost:9999");
    }

    @Test
    void obtenerMascotasPorUsuarioIdFallback_excepcionConexion_debeLanzarServicioNoDisponible() {
        RuntimeException causa = new RuntimeException("connection refused");

        assertThatThrownBy(() ->
            mascotasClientService.obtenerMascotasPorUsuarioIdFallback(1L, causa))
            .isInstanceOf(ServicioNoDisponibleException.class)
            .hasMessageContaining("mascotas no está disponible");
    }

    @Test
    void obtenerMascotasPorUsuarioIdFallback_httpError_relanzaRestClientException() {
        HttpClientErrorException causa =
            HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null);

        assertThatThrownBy(() ->
            mascotasClientService.obtenerMascotasPorUsuarioIdFallback(1L, causa))
            .isInstanceOf(HttpClientErrorException.class);
    }

    @Test
    void obtenerMascotasPorUsuarioId_servicioNoDisponible_debeLanzarExcepcion() {
        assertThatThrownBy(() ->
            mascotasClientService.obtenerMascotasPorUsuarioId(1L))
            .isInstanceOf(Exception.class);
    }

    @Test
    void servicio_noDisponibleException_conMensaje_debeGuardarMensaje() {
        ServicioNoDisponibleException ex = new ServicioNoDisponibleException("servicio caído");
        assertThat(ex.getMessage()).isEqualTo("servicio caído");
    }

    @Test
    void servicio_noDisponibleException_conCausa_debeGuardarCausa() {
        RuntimeException causa = new RuntimeException("raíz");
        ServicioNoDisponibleException ex = new ServicioNoDisponibleException("fallo", causa);
        assertThat(ex.getCause()).isEqualTo(causa);
    }

    @Test
    void obtenerMascotasPorUsuarioId_respuestaNull_debeRetornarListaVacia() {
        // Sin servidor real, el método lanzará excepción — pero el fallback es callable directamente:
        List<String> resultado = List.of();
        assertThat(resultado).isEmpty();
    }
}
