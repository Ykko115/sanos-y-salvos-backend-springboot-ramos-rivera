package mascotas.microservice.mascotas.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchResultDTO {

    @JsonProperty("mascota_id")
    private Long mascotaId;

    private Double score;

    private Integer porcentaje;

    private Map<String, Object> detalle;

    private Boolean alerta;

    private String mensaje;
}
