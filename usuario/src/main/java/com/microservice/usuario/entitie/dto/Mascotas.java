package com.microservice.usuario.entitie.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mascotas {

    private Long id;
    private String nombre;
    private String raza;
    private int edad;
    private String descripcion;
    private Especie especie;
    private Estado estado;
    private Long usuarioId;

    public enum Estado {
        ENCONTRADO, PERDIDO
    }

    public enum Especie {
        PERRO, GATO, HURON, ROEDOR, OTRO
    }



}
