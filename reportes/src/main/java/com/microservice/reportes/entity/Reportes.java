package com.microservice.reportes.entity;

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
    private String descripcion;
    private String coordenadas;
    private String img;
    private Long usuarioId;
    private Long mascotaId;
    public enum Estado {
        ENCONTRADO, PERDIDO
    }

    @Enumerated (EnumType.STRING)
    private Estado estado;

    @Override
    public String toString(){
        return "Reporte{id=" + id + ", descripcion='" + descripcion + "', coordenadas='" + coordenadas + "', img=" + img + "', usuarioId='" + usuarioId + "', mascotaId='" + mascotaId +"', estado='" + estado +"}";
    }

}
