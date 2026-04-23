package mascotas.microservice.mascotas.entity;

import jakarta.persistence.Entity;
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
    private String especie;
    private String raza;
    private int edad;
    private String descripcion;
    private Boolean activo = true;


    @Override
    public String toString() {
        return "Mascotas{id=" + id + ", nombre='" + nombre + "', especie='" + especie + "', raza='" + raza + "', edad=" + edad + ", descripcion='" + descripcion + "', activo=" + activo + "}";
    }

}

