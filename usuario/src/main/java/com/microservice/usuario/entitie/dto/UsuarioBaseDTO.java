package com.microservice.usuario.entitie.dto;
 
/**
 * Clase base con los campos comunes a CrearUsuarioDTO y ActualizarUsuarioDTO.
 * Centraliza los getters/setters para evitar duplicación de código (java:S1192).
 */
public abstract class UsuarioBaseDTO {
 
    private String rut;
    private String nombre;
    private String apellido;
    private String email;
    private Integer telefono;
    private String password;
 
    public String getRut() { return rut; }
    public void setRut(String rut) { this.rut = rut; }
 
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
 
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
 
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
 
    public Integer getTelefono() { return telefono; }
    public void setTelefono(Integer telefono) { this.telefono = telefono; }
 
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}

