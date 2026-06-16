package com.microservice.reportes.dto;

import com.microservice.reportes.entity.Reportes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteBaseDTO {
    private Long usuarioId;
    private Long mascotaId;
    private String descripcion;
    private Integer telefono;
    private Reportes.Ubicacion ubicacion;
    private String img;
    private Reportes.Estado estado;
}