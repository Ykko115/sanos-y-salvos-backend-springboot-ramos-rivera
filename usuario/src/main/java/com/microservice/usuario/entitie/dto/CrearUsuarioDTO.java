package com.microservice.usuario.entitie.dto;
 
/**
 * DTO para la creación de un nuevo usuario (POST /api/usuario).
 *
 * Solo expone los campos que un cliente externo puede enviar al registrarse.
 * Campos sensibles como {@code id} y {@code activo} no se aceptan desde la
 * petición: {@code activo} se inicializa siempre en {@code true}.
 * El {@code rol} solo es respetado si el llamante tiene ROLE_ADMIN;
 * de lo contrario el controller lo sobreescribe a USER.
 * Esto evita vulnerabilidades de Mass Assignment (java:S4684).
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
