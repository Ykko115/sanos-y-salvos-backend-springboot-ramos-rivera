
package com.microservice.usuario.entitie.dto;
 
/**
 * DTO para la actualización de un usuario existente (PUT /api/usuario/{id}).
 */
public class ActualizarUsuarioDTO extends UsuarioBaseDTO {
    // Hereda rut, nombre, apellido, email, telefono y password de UsuarioBaseDTO.
    // rol y activo solo se aplican si el llamante tiene rol ADMIN (validado en el controller).
    private String rol;
    private Boolean activo;

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
