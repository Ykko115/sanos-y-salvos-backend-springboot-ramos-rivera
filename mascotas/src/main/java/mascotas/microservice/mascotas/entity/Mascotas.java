package mascotas.microservice.mascotas.entity;

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
public class Mascotas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String raza;
    private int edad;
    private String descripcion;
    private Long usuarioId;
    public enum Estado {
        ENCONTRADO, PERDIDO
    }
    public enum Especie{
        PERRO, GATO, HURON, ROEDOR, OTRO
    }

    @Enumerated (EnumType.STRING)
    private Especie especie;

    @Enumerated (EnumType.STRING)
    private Estado estado;


    @Override
    public String toString() {
        return "Mascotas{id=" + id + ", nombre='" + nombre + "', especie='" + especie + "', raza='" + raza + "', edad=" + edad + ", descripcion='" + descripcion + "', estado=" + estado + ", usuarioId=" + usuarioId + "}";
    }

}

