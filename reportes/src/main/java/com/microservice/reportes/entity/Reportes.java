package com.microservice.reportes.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Reportes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre_mascota;
    private String nombre_usuario;
    private Long usuarioId;
    private Long mascotaId;
    private String descripcion;
    private LocalDate fechaReporte;
    private Integer telefono;

    @Embedded
    private Ubicacion ubicacion;

    @Column(columnDefinition = "TEXT")
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
        private Double latitude;
        private Double longitude;
    }

   @Override
    public String toString(){
    return "Reporte{id=" + id + ", nombre_usuario='" + nombre_usuario + ", nombre_mascota='" + nombre_mascota + ", descripcion='" + descripcion + ", telefono='" + telefono + "', ubicacion='" + ubicacion + "', fechaReporte=" + fechaReporte + "', img=" + img + "', usuarioId='" + usuarioId + "', mascotaId='" + mascotaId +"', estado='" + estado +"}";
        }
}