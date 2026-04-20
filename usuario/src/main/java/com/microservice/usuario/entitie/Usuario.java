package com.microservice.usuario.entitie;

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
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String rut;
    private String nombre;
    private String apellido;
    private String email;
    private int telefono;
    private String password;
    private Boolean activo = true;
    private enum Rol{
        ADMIN, USER
    }

    @Enumerated (EnumType.STRING)
    private Rol rol;
    
    // Relación con mascotas ahora se gestiona por API y DTOs entre microservicios
    // private List<MascotaDTO> mascotas; // Solo si quieres exponer la lista recibida por API

    @Override
    public String toString(){
        return "Usuario{id=" + id +", rut='" + rut + "', nombre='" + nombre + "', apellido='" + apellido + "', email='" + email + "', telefono='" + telefono + "', password='" + password + "', activo='" + activo + "', rol='" + rol + "}";
    }

}
