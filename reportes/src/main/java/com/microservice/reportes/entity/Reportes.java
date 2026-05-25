package com.microservice.reportes.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Reportes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long usuarioId;
    private Long mascotaId;
    private String descripcion;
    private LocalDate fechaReporte;

    @Embedded
    private Ubicacion ubicacion;

    private String img;

    public enum Estado {
        ENCONTRADO, PERDIDO
    }

    @Enumerated(EnumType.STRING)
    private Estado estado;

    // --- Clase embebida para la ubicación ---
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Ubicacion {
        private String nombre;
        private Double latitude;
        private Double longitude;
    }

   @Override
    public String toString(){
    return "Reporte{id=" + id + ", descripcion='" + descripcion + "', ubicacion='" + ubicacion + "', fechaReporte=" + fechaReporte + "', img=" + img + "', usuarioId='" + usuarioId + "', mascotaId='" + mascotaId +"', estado='" + estado +"}";
        }
}