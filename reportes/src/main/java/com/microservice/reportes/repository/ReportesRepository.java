package com.microservice.reportes.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.microservice.reportes.entity.Reportes;
import com.microservice.reportes.entity.Reportes.Estado;


public interface ReportesRepository extends CrudRepository<Reportes, Long> {
    
    List<Reportes> findByEstado(Estado estado);    
    List<Reportes> findByUsuarioId(Long usuarioId);
    List<Reportes> findByMascotaId(Long mascotaId);

}
