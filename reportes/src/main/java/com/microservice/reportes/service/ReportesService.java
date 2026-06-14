package com.microservice.reportes.service;

import java.util.List;
import java.util.Optional;

import com.microservice.reportes.entity.Reportes;

public interface ReportesService {

    Reportes creaReporte(Reportes reportes);
    Optional<Reportes>obtenerReportePorId(Long id);
    List<Reportes> obtenerTodosLosReportes();
    Reportes actualizarReporte(Long id, Reportes reportes);
    void eliminarReporte(Long id);
    List<Reportes> obtenerReportesPorEstado(Reportes.Estado estado);
    List<Reportes> obtenerReportesPorUsuario(Long usuarioId);
    List<Reportes> obtenerReportesPorMascota(Long mascotaId);

    void eliminarReportesPorMascotaId(Long mascotaId);

}
