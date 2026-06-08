package mascotas.microservice.mascotas.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mascotas")
public class Mascotas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @Enumerated(EnumType.STRING)
    private Especie especie;

    private String raza;

    @Enumerated(EnumType.STRING)
    private Color color;

    @Enumerated(EnumType.STRING)
    private Tamano tamano;

    @Enumerated(EnumType.STRING)
    private Pelaje pelaje;

    private Integer edad;

    @Enumerated(EnumType.STRING)
    private RangoEdad rangoEdad;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "mascota_senas", joinColumns = @JoinColumn(name = "mascota_id"))
    @Column(name = "sena")
    private List<Sena> senas = new ArrayList<>();

    private String descripcion;

    @Column(columnDefinition = "TEXT")
    private String fotoUrl;

    private Double lat;
    private Double lng;

    @Enumerated(EnumType.STRING)
    private Estado estado;

    private Long usuarioId;

    @CreationTimestamp
    private LocalDateTime fechaReporte;

    // ── Enums ─────────────────────────────────────────────────────

    public enum Especie {
        PERRO, GATO, HURON, ROEDOR, OTRO
    }

    public enum Estado {
        PERDIDO, ENCONTRADO, REUNIDO
    }

    public enum Color {
        NEGRO, BLANCO, CAFE, DORADO, GRIS,
        NARANJA, BEIGE, ROJIZO, MANCHADO, OTRO
    }

    public enum Tamano {
        PEQUENO, MEDIANO, GRANDE
    }

    public enum Pelaje {
        CORTO, LARGO, RIZADO, SIN_PELO
    }

    public enum RangoEdad {
        CACHORRO, JOVEN, ADULTO, MAYOR, NO_SE
    }

    public enum Sena {
        TIENE_COLLAR, TIENE_MANCHAS, COJEA,
        OJOS_DISTINTOS, CICATRIZ, MUY_SOCIABLE,
        ASUSTADIZO, USA_ACCESORIO
    }
}
