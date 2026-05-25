package com.microservice.reportes.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.microservice.reportes.entity.Reportes;
import com.microservice.reportes.repository.ReportesRepository;
import com.microservice.reportes.exception.MascotaNoEncontradaException;
import com.microservice.reportes.exception.UsuarioNoEncontradoException;

@Service
@Transactional
public class ReportesServiceImpl  implements ReportesService{


    @Autowired
    private ReportesRepository reportesRepository;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Value("${servicio.mascotas.url:http://localhost:8082}")
    private String mascotasServiceUrl;

    @Value("${servicio.usuarios.url:http://localhost:8083}")
    private String usuariosServiceUrl;

    @Override

    public Reportes creaReporte(Reportes reportes){
        // Validar existencia de la mascota
        try {
            webClientBuilder.build()
                .get()
                .uri(mascotasServiceUrl + "/api/mascotas/" + reportes.getMascotaId())
                .retrieve()
                .bodyToMono(Object.class)
                .block();
        } catch (WebClientResponseException.NotFound e) {
            throw new MascotaNoEncontradaException("La mascota con id " + reportes.getMascotaId() + " no existe");
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Error al consultar el servicio de mascotas: " + e.getMessage());
        }

        // Validar existencia del usuario
        try {
            webClientBuilder.build()
                .get()
                .uri(usuariosServiceUrl + "/api/usuarios/" + reportes.getUsuarioId())
                .retrieve()
                .bodyToMono(Object.class)
                .block();
        } catch (WebClientResponseException.NotFound e) {
            throw new UsuarioNoEncontradoException("El usuario con id " + reportes.getUsuarioId() + " no existe");
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Error al consultar el servicio de usuarios: " + e.getMessage());
        }
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
