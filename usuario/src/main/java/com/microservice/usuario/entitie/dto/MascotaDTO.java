package com.microservice.usuario.entitie.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MascotaDTO {
    private Long id;
    private String nombre;
    private String raza;
    private int edad;
    private String descripcion;
    private String especie;
    private String estado;
    private Long usuarioId;
}
