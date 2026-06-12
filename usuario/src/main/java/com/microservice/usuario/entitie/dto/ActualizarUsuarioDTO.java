
package com.microservice.usuario.entitie.dto;
 
/**
 * DTO para la actualización de un usuario existente (PUT /api/usuario/{id}).
 */
public class ActualizarUsuarioDTO extends UsuarioBaseDTO {
    // Hereda rut, nombre, apellido, email, telefono y password de UsuarioBaseDTO.
    // No incluye rol ni activo para evitar escalada de privilegios.
}
