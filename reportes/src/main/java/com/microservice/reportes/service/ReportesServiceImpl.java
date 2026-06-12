package com.microservice.reportes.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.microservice.reportes.client.ServicioValidacionClient;
import com.microservice.reportes.entity.Reportes;
import com.microservice.reportes.repository.ReportesRepository;

@Service
@Transactional
public class ReportesServiceImpl  implements ReportesService{

    private static final Logger logger = LoggerFactory.getLogger(ReportesServiceImpl.class);


    private final ReportesRepository reportesRepository;
    private final ServicioValidacionClient servicioValidacionClient;

    public ReportesServiceImpl(ReportesRepository reportesRepository,
                               ServicioValidacionClient servicioValidacionClient) {
        this.reportesRepository = reportesRepository;
        this.servicioValidacionClient = servicioValidacionClient;
    }

    @Override
    public Reportes creaReporte(Reportes reportes) {

        logger.info("Iniciando creación de reporte: {}", reportes);

        // Solo validar mascota si viene un mascotaId (reporte autenticado).
        // La validación remota está protegida por un Circuit Breaker (Resilience4j).
        if (reportes.getMascotaId() != null) {
            servicioValidacionClient.validarMascotaExiste(reportes.getMascotaId());
        }

        // Solo validar usuario si viene un usuarioId (protegido por Circuit Breaker).
        if (reportes.getUsuarioId() != null) {
            servicioValidacionClient.validarUsuarioExiste(reportes.getUsuarioId());
        }

        logger.info("Guardando reporte en base de datos");
        return reportesRepository.save(reportes);
    }

    @Override
    @Transactional(readOnly= true)
    public Optional<Reportes> obtenerReportePorId(Long id){
        return reportesRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reportes> obtenerTodosLosReportes() {
        return (List<Reportes>) reportesRepository.findAll();
    }

    @Override
    public Reportes actualizarReporte(Long id, Reportes reportes) {
        Optional<Reportes> reporteExistente = reportesRepository.findById(id);
        
        if(reporteExistente.isPresent()){
            Reportes reporteActualizado = reporteExistente.get();
            reporteActualizado.setDescripcion(reportes.getDescripcion());
            reporteActualizado.setUbicacion(reportes.getUbicacion());
            reporteActualizado.setEstado(reportes.getEstado());
            reporteActualizado.setImg(reportes.getImg());
            reporteActualizado.setMascotaId(reportes.getMascotaId());
            reporteActualizado.setUsuarioId(reportes.getUsuarioId());
            reporteActualizado.setNombre_mascota(reportes.getNombre_mascota());
            reporteActualizado.setNombre_usuario(reportes.getNombre_usuario());
            reporteActualizado.setFechaReporte(reportes.getFechaReporte());
            reporteActualizado.setTelefono(reportes.getTelefono());

            return reportesRepository.save(reporteActualizado);
        }

        throw new RuntimeException("Reporte no encontrado por la id: "+ id);
    }

    @Override
    public void eliminarReporte(Long id){
        if(reportesRepository.existsById(id)){
            reportesRepository.deleteById(id);
        } else {
            throw new RuntimeException("Reporte no ha sido encontrado");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reportes> obtenerReportesPorEstado(Reportes.Estado estado){
        return reportesRepository.findByEstado(estado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reportes> obtenerReportesPorUsuario(Long usuarioId){
        return reportesRepository.findByUsuarioId(usuarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reportes> obtenerReportesPorMascota(Long mascotaId) {
        return reportesRepository.findByMascotaId(mascotaId);
    }

}
