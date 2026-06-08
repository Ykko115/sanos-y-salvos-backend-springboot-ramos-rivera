package com.microservice.reportes.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.web.reactive.function.client.WebClient;

import com.microservice.reportes.entity.Reportes;
import com.microservice.reportes.entity.Reportes.Estado;
import com.microservice.reportes.service.ReportesService;

@WebMvcTest(ReportesRestController.class)
class ReportesRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportesService reportesService;

    @MockitoBean
    private WebClient.Builder webClientBuilder;

    private Reportes reporte;

    // Serialización manual para evitar el problema del ObjectMapper
    private String toJson(Reportes r) {
        return String.format(
            "{\"id\":%d,\"nombre_mascota\":\"%s\",\"nombre_usuario\":\"%s\"," +
            "\"descripcion\":\"%s\",\"estado\":\"%s\",\"fechaReporte\":\"%s\"," +
            "\"ubicacion\":{\"latitude\":%s,\"longitude\":%s}}",
            r.getId(), r.getNombre_mascota(), r.getNombre_usuario(),
            r.getDescripcion(), r.getEstado(), r.getFechaReporte(),
            r.getUbicacion().getLatitude(), r.getUbicacion().getLongitude()
        );
    }

    @BeforeEach
    void setUp() {
        reporte = new Reportes();
        reporte.setId(1L);
        reporte.setNombre_mascota("Fido");
        reporte.setNombre_usuario("Juan");
        reporte.setDescripcion("Perro perdido");
        reporte.setEstado(Estado.PERDIDO);
        reporte.setFechaReporte(LocalDate.now());
        reporte.setUbicacion(new Reportes.Ubicacion(-33.45, -70.66));
    }

    @Test
    @WithMockUser
    void crearReporte_debeRetornar201() throws Exception {
        when(reportesService.creaReporte(any(Reportes.class))).thenReturn(reporte);

        mockMvc.perform(post("/api/reportes")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(reporte)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nombre_mascota").value("Fido"));
    }

    @Test
    @WithMockUser
    void obtenerReportePorId_existente_debeRetornar200() throws Exception {
        when(reportesService.obtenerReportePorId(1L)).thenReturn(Optional.of(reporte));

        mockMvc.perform(get("/api/reportes/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.estado").value("PERDIDO"));
    }

    @Test
    @WithMockUser
    void obtenerReportePorId_inexistente_debeRetornar404() throws Exception {
        when(reportesService.obtenerReportePorId(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/reportes/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void obtenerTodosLosReportes_debeRetornarLista() throws Exception {
        when(reportesService.obtenerTodosLosReportes()).thenReturn(List.of(reporte));

        mockMvc.perform(get("/api/reportes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser
    void eliminarReporte_existente_debeRetornar204() throws Exception {
        doNothing().when(reportesService).eliminarReporte(1L);

        mockMvc.perform(delete("/api/reportes/1").with(csrf()))
            .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void eliminarReporte_inexistente_debeRetornar404() throws Exception {
        doThrow(new RuntimeException("No encontrado"))
            .when(reportesService).eliminarReporte(999L);

        mockMvc.perform(delete("/api/reportes/999").with(csrf()))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void obtenerReportesPorEstado_debeRetornarFiltrados() throws Exception {
        when(reportesService.obtenerReportesPorEstado(Estado.PERDIDO))
            .thenReturn(List.of(reporte));

        mockMvc.perform(get("/api/reportes/estado/PERDIDO"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].estado").value("PERDIDO"));
    }

    @Test
    @WithMockUser
    void obtenerReportesPorUsuario_debeRetornarLista() throws Exception {
        reporte.setUsuarioId(5L);
        when(reportesService.obtenerReportesPorUsuario(5L)).thenReturn(List.of(reporte));

        mockMvc.perform(get("/api/reportes/usuario/5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser
    void obtenerReportesPorMascota_debeRetornarLista() throws Exception {
        reporte.setMascotaId(3L);
        when(reportesService.obtenerReportesPorMascota(3L)).thenReturn(List.of(reporte));

        mockMvc.perform(get("/api/reportes/mascota/3"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }
}