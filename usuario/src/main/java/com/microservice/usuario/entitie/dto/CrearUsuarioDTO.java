package com.microservice.usuario.entitie.dto;
 
/**
 * DTO para la creación de un nuevo usuario (POST /api/usuario).
 */
public class CrearUsuarioDTO extends UsuarioBaseDTO {
 
    /**
     * Solo los administradores pueden especificar este campo;
     * el controller lo ignora si el llamante no tiene ROLE_ADMIN.
     */
    private String rol;
 
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}
