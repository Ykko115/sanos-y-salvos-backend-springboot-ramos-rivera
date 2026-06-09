
package com.microservice.usuario.entitie.dto;
 
/**
 * DTO para la actualización de un usuario existente (PUT /api/usuario/{id}).
 *
 * Expone únicamente los campos que un usuario puede modificar sobre su propio
 * perfil. Campos como {@code id}, {@code activo} y {@code rol} quedan fuera
 * del contrato de la API para evitar escalada de privilegios y Mass Assignment
 * (java:S4684). Todos los campos son opcionales: solo los no-nulos se aplican
 * en el servicio (patch parcial).
 */
public class ActualizarUsuarioDTO extends UsuarioBaseDTO {
    // Hereda rut, nombre, apellido, email, telefono y password de UsuarioBaseDTO.
    // No incluye rol ni activo para evitar escalada de privilegios.
}
