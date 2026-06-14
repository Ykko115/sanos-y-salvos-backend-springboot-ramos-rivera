package com.microservice.reportes.client;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.microservice.reportes.exception.MascotaNoEncontradaException;
import com.microservice.reportes.exception.ServicioNoDisponibleException;
import com.microservice.reportes.exception.UsuarioNoEncontradoException;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class ServicioValidacionClientTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @InjectMocks
    private ServicioValidacionClient servicioValidacionClient;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(servicioValidacionClient, "mascotasServiceUrl", "http://localhost:8082");
        ReflectionTestUtils.setField(servicioValidacionClient, "usuariosServiceUrl", "http://localhost:8081");
    }

    private void mockWebClientOk() {
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        when(responseSpec.bodyToMono(Object.class)).thenReturn(Mono.just(new Object()));

        @SuppressWarnings("rawtypes")
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        when(headersSpec.retrieve()).thenReturn(responseSpec);

        @SuppressWarnings("rawtypes")
        WebClient.RequestHeadersUriSpec uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        when(uriSpec.uri(anyString())).thenReturn(headersSpec);

        WebClient webClient = mock(WebClient.class);
        when(webClient.get()).thenReturn(uriSpec);
        when(webClientBuilder.build()).thenReturn(webClient);
    }

    private void mockWebClientNotFound() {
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        when(responseSpec.bodyToMono(Object.class)).thenReturn(
            Mono.error(WebClientResponseException.create(
                404, "Not Found", HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8)));

        @SuppressWarnings("rawtypes")
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        when(headersSpec.retrieve()).thenReturn(responseSpec);

        @SuppressWarnings("rawtypes")
        WebClient.RequestHeadersUriSpec uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        when(uriSpec.uri(anyString())).thenReturn(headersSpec);

        WebClient webClient = mock(WebClient.class);
        when(webClient.get()).thenReturn(uriSpec);
        when(webClientBuilder.build()).thenReturn(webClient);
    }

    // ─── validarMascotaExiste ─────────────────────────────────────────────────

    @Test
    void validarMascotaExiste_existente_noLanzaExcepcion() {
        mockWebClientOk();

        assertThatCode(() -> servicioValidacionClient.validarMascotaExiste(1L))
            .doesNotThrowAnyException();
    }

    @Test
    void validarMascotaExiste_noEncontrada_lanzaMascotaNoEncontrada() {
        mockWebClientNotFound();

        assertThatThrownBy(() -> servicioValidacionClient.validarMascotaExiste(99L))
            .isInstanceOf(MascotaNoEncontradaException.class)
            .hasMessageContaining("99");
    }

    // ─── validarMascotaFallback ───────────────────────────────────────────────

    @Test
    void validarMascotaFallback_conMascotaNoEncontrada_reLanza() {
        MascotaNoEncontradaException ex = new MascotaNoEncontradaException("no existe");

        assertThatThrownBy(() -> servicioValidacionClient.validarMascotaFallback(1L, ex))
            .isInstanceOf(MascotaNoEncontradaException.class);
    }

    @Test
    void validarMascotaFallback_conWebClientError_lanzaRuntimeException() {
        WebClientResponseException ex = WebClientResponseException.create(
            503, "Service Unavailable", HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8);

        assertThatThrownBy(() -> servicioValidacionClient.validarMascotaFallback(1L, ex))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Error al consultar el servicio de mascotas");
    }

    @Test
    void validarMascotaFallback_conErrorDeConexion_lanzaServicioNoDisponible() {
        RuntimeException ex = new RuntimeException("connection refused");

        assertThatThrownBy(() -> servicioValidacionClient.validarMascotaFallback(1L, ex))
            .isInstanceOf(ServicioNoDisponibleException.class);
    }

    // ─── validarUsuarioExiste ─────────────────────────────────────────────────

    @Test
    void validarUsuarioExiste_existente_noLanzaExcepcion() {
        mockWebClientOk();

        assertThatCode(() -> servicioValidacionClient.validarUsuarioExiste(1L))
            .doesNotThrowAnyException();
    }

    @Test
    void validarUsuarioExiste_noEncontrado_lanzaUsuarioNoEncontrado() {
        mockWebClientNotFound();

        assertThatThrownBy(() -> servicioValidacionClient.validarUsuarioExiste(99L))
            .isInstanceOf(UsuarioNoEncontradoException.class)
            .hasMessageContaining("99");
    }

    // ─── validarUsuarioFallback ───────────────────────────────────────────────

    @Test
    void validarUsuarioFallback_conUsuarioNoEncontrado_reLanza() {
        UsuarioNoEncontradoException ex = new UsuarioNoEncontradoException("no existe");

        assertThatThrownBy(() -> servicioValidacionClient.validarUsuarioFallback(1L, ex))
            .isInstanceOf(UsuarioNoEncontradoException.class);
    }

    @Test
    void validarUsuarioFallback_conWebClientError_lanzaRuntimeException() {
        WebClientResponseException ex = WebClientResponseException.create(
            503, "Service Unavailable", HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8);

        assertThatThrownBy(() -> servicioValidacionClient.validarUsuarioFallback(1L, ex))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Error al consultar el servicio de usuarios");
    }

    @Test
    void validarUsuarioFallback_conErrorDeConexion_lanzaServicioNoDisponible() {
        RuntimeException ex = new RuntimeException("connection refused");

        assertThatThrownBy(() -> servicioValidacionClient.validarUsuarioFallback(1L, ex))
            .isInstanceOf(ServicioNoDisponibleException.class);
    }
}
