package com.microservice.reportes.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.microservice.reportes.entity.Reportes;
import com.microservice.reportes.entity.Reportes.Estado;
import com.microservice.reportes.exception.MascotaNoEncontradaException;
import com.microservice.reportes.repository.ReportesRepository;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class ReportesServiceImplTest {

    @Mock
    private ReportesRepository reportesRepository;

    @Mock
    private WebClient.Builder webClientBuilder;

    @InjectMocks
    private ReportesServiceImpl reportesService;

    private Reportes reporte;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(reportesService, "mascotasServiceUrl", "http://mascotas-service");
        ReflectionTestUtils.setField(reportesService, "usuariosServiceUrl", "http://usuarios-service");

        reporte = new Reportes();
        reporte.setId(1L);
        reporte.setNombre_mascota("Fido");
        reporte.setNombre_usuario("Juan");
        reporte.setDescripcion("Perro perdido en el parque");
        reporte.setEstado(Estado.PERDIDO);
        reporte.setFechaReporte(LocalDate.now());
        reporte.setUbicacion(new Reportes.Ubicacion(-33.45, -70.66));
    }

    // ─── Helper: construye un WebClient mockeado que devuelve éxito ───────────

    private WebClient buildWebClientMockOk() {
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        when(responseSpec.bodyToMono(Object.class)).thenReturn(Mono.just(new Object()));

        // RequestHeadersSpec con raw type para evitar el problema de wildcards
        @SuppressWarnings("rawtypes")
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        when(headersSpec.retrieve()).thenReturn(responseSpec);

        @SuppressWarnings("rawtypes")
        WebClient.RequestHeadersUriSpec uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        when(uriSpec.uri(anyString())).thenReturn(headersSpec);

        WebClient webClient = mock(WebClient.class);
        when(webClient.get()).thenReturn(uriSpec);

        return webClient;
    }

    // ─── Helper: construye un WebClient mockeado que lanza 404 ───────────────

    private WebClient buildWebClientMockNotFound() {
    // Crear la excepción correcta: NotFound (404), no la genérica
        WebClientResponseException.NotFound notFoundEx = (WebClientResponseException.NotFound)
            WebClientResponseException.create(404, "Not Found", 
                org.springframework.http.HttpHeaders.EMPTY, 
                new byte[0], 
                java.nio.charset.StandardCharsets.UTF_8);

        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        when(responseSpec.bodyToMono(Object.class))
            .thenReturn(Mono.error(notFoundEx));

        @SuppressWarnings("rawtypes")
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        when(headersSpec.retrieve()).thenReturn(responseSpec);

        @SuppressWarnings("rawtypes")
        WebClient.RequestHeadersUriSpec uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        when(uriSpec.uri(anyString())).thenReturn(headersSpec);

        WebClient webClient = mock(WebClient.class);
        when(webClient.get()).thenReturn(uriSpec);

        return webClient;
    }   

    // ─── creaReporte ─────────────────────────────────────────────────────────

    @Test
    void creaReporte_sinIds_debeGuardarDirectamente() {
        reporte.setMascotaId(null);
        reporte.setUsuarioId(null);
        when(reportesRepository.save(reporte)).thenReturn(reporte);

        Reportes resultado = reportesService.creaReporte(reporte);

        assertThat(resultado.getNombre_mascota()).isEqualTo("Fido");
        verify(reportesRepository).save(reporte);
        verifyNoInteractions(webClientBuilder);
    }

    @Test
    void creaReporte_conMascotaValida_debeGuardar() {
        reporte.setMascotaId(10L);
        reporte.setUsuarioId(null);

        // 1. Primero construir el mock
        WebClient webClientMock = buildWebClientMockOk();
        // 2. Luego stubear
        when(webClientBuilder.build()).thenReturn(webClientMock);
        when(reportesRepository.save(reporte)).thenReturn(reporte);

        Reportes resultado = reportesService.creaReporte(reporte);

        assertThat(resultado).isNotNull();
        verify(reportesRepository).save(reporte);
    }

    @Test
    void creaReporte_conUsuarioValido_debeGuardar() {
        reporte.setMascotaId(null);
        reporte.setUsuarioId(5L);

        WebClient webClientMock = buildWebClientMockOk();
        when(webClientBuilder.build()).thenReturn(webClientMock);
        when(reportesRepository.save(reporte)).thenReturn(reporte);

        Reportes resultado = reportesService.creaReporte(reporte);

        assertThat(resultado).isNotNull();
        verify(reportesRepository).save(reporte);
    }

    @Test
    void creaReporte_mascotaNoExiste_debeLanzarExcepcion() {
        reporte.setMascotaId(99L);
        reporte.setUsuarioId(null);

        WebClient webClientMock = buildWebClientMockNotFound();
        when(webClientBuilder.build()).thenReturn(webClientMock);

        assertThatThrownBy(() -> reportesService.creaReporte(reporte))
            .isInstanceOf(MascotaNoEncontradaException.class);

        verify(reportesRepository, never()).save(any());
    }

    // ─── obtenerReportePorId ──────────────────────────────────────────────────

    @Test
    void obtenerReportePorId_existente_debeRetornarReporte() {
        when(reportesRepository.findById(1L)).thenReturn(Optional.of(reporte));

        Optional<Reportes> resultado = reportesService.obtenerReportePorId(1L);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(1L);
    }

    @Test
    void obtenerReportePorId_inexistente_debeRetornarVacio() {
        when(reportesRepository.findById(999L)).thenReturn(Optional.empty());

        assertThat(reportesService.obtenerReportePorId(999L)).isEmpty();
    }

    // ─── obtenerTodosLosReportes ──────────────────────────────────────────────

    @Test
    void obtenerTodosLosReportes_debeRetornarLista() {
        when(reportesRepository.findAll()).thenReturn(List.of(reporte));

        List<Reportes> resultado = reportesService.obtenerTodosLosReportes();

        assertThat(resultado).hasSize(1);
    }

    // ─── actualizarReporte ────────────────────────────────────────────────────

    @Test
    void actualizarReporte_existente_debeActualizar() {
        Reportes actualizado = new Reportes();
        actualizado.setDescripcion("Nueva descripción");
        actualizado.setEstado(Estado.ENCONTRADO);
        actualizado.setUbicacion(new Reportes.Ubicacion(-33.0, -70.0));

        when(reportesRepository.findById(1L)).thenReturn(Optional.of(reporte));
        when(reportesRepository.save(any(Reportes.class))).thenAnswer(inv -> inv.getArgument(0));

        Reportes resultado = reportesService.actualizarReporte(1L, actualizado);

        assertThat(resultado.getDescripcion()).isEqualTo("Nueva descripción");
        assertThat(resultado.getEstado()).isEqualTo(Estado.ENCONTRADO);
    }

    @Test
    void actualizarReporte_inexistente_debeLanzarExcepcion() {
        when(reportesRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportesService.actualizarReporte(999L, reporte))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("999");
    }

    // ─── eliminarReporte ──────────────────────────────────────────────────────

    @Test
    void eliminarReporte_existente_debeEliminar() {
        when(reportesRepository.existsById(1L)).thenReturn(true);
        doNothing().when(reportesRepository).deleteById(1L);

        assertThatCode(() -> reportesService.eliminarReporte(1L))
            .doesNotThrowAnyException();

        verify(reportesRepository).deleteById(1L);
    }

    @Test
    void eliminarReporte_inexistente_debeLanzarExcepcion() {
        when(reportesRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> reportesService.eliminarReporte(999L))
            .isInstanceOf(RuntimeException.class);
    }

    // ─── filtros ──────────────────────────────────────────────────────────────

    @Test
    void obtenerReportesPorEstado_debeRetornarFiltrados() {
        when(reportesRepository.findByEstado(Estado.PERDIDO)).thenReturn(List.of(reporte));

        assertThat(reportesService.obtenerReportesPorEstado(Estado.PERDIDO)).hasSize(1);
    }

    @Test
    void obtenerReportesPorUsuario_debeRetornarFiltrados() {
        reporte.setUsuarioId(5L);
        when(reportesRepository.findByUsuarioId(5L)).thenReturn(List.of(reporte));

        assertThat(reportesService.obtenerReportesPorUsuario(5L)).hasSize(1);
    }

    @Test
    void obtenerReportesPorMascota_debeRetornarFiltrados() {
        reporte.setMascotaId(3L);
        when(reportesRepository.findByMascotaId(3L)).thenReturn(List.of(reporte));

        assertThat(reportesService.obtenerReportesPorMascota(3L)).hasSize(1);
    }
}