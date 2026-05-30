package com.microservice.reportes.dto;

import com.microservice.reportes.entity.Reportes;
import lombok.Data;

@Data
public class ReporteDetalleDTO {
    private Reportes reporte;
    private Object usuario; // Puede ser UsuarioDTO si se define igual
    private Object mascota; // Puede ser MascotaDTO si se define igual
}
