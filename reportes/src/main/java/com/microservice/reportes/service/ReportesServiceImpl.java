package com.microservice.reportes.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.microservice.reportes.entity.Reportes;
import com.microservice.reportes.repository.ReportesRepository;

@Service
@Transactional
public class ReportesServiceImpl  implements ReportesService{

    @Autowired
    private ReportesRepository reportesRepository;

    @Override
    public Reportes creaReporte(Reportes reportes){
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
            reporteActualizado.setCoordenadas(reportes.getCoordenadas());
            reporteActualizado.setEstado(reportes.getEstado());
            reporteActualizado.setImg(reportes.getImg());
            reporteActualizado.setMascotaId(reportes.getMascotaId());
            reporteActualizado.setUsuarioId(reportes.getUsuarioId());

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
