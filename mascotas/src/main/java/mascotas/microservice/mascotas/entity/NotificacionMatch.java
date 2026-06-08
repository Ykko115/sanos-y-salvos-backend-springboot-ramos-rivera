package mascotas.microservice.mascotas.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notificaciones_match")
public class NotificacionMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long usuarioId;

    private Long mascotaIdPerdida;

    private Long mascotaIdCandidata;

    private Double score;

    private Integer porcentaje;

    @Column(length = 500)
    private String mensaje;

    private LocalDateTime fechaNotificacion;

    private Boolean leida = false;
}
