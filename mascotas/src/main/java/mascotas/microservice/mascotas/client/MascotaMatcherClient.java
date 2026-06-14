package mascotas.microservice.mascotas.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mascotas.microservice.mascotas.dto.MatchResultDTO;
import mascotas.microservice.mascotas.entity.Mascotas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
public class MascotaMatcherClient {

    private static final Logger logger = LoggerFactory.getLogger(MascotaMatcherClient.class);

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Value("${fastapi.url:http://localhost:8000}")
    private String fastapiUrl;

    // ── DTOs internos que mapean a MascotaInput y MatchRequest de FastAPI ──────

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MascotaInputDTO {
        private Long id;
        private String nombre;
        private String especie;
        private String estado;
        private String raza;
        private String color;
        private String tamano;
        private String pelaje;
        private Integer edad;
        private String rangoEdad;
        private List<String> senas;
        private String descripcion;
        private Long usuarioId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MatchRequestBody {
        @JsonProperty("mascota_perdida")
        private MascotaInputDTO mascotaPerdida;
        private List<MascotaInputDTO> candidatas;
    }

    // ── Conversión Mascotas → DTO ────────────────────────────────────────────

    private MascotaInputDTO toDTO(Mascotas m) {
        List<String> senas = m.getSenas() != null
            ? m.getSenas().stream().map(Enum::name).collect(Collectors.toList())
            : Collections.emptyList();
        return new MascotaInputDTO(
            m.getId(),
            m.getNombre(),
            m.getEspecie()    != null ? m.getEspecie().name()    : null,
            m.getEstado()     != null ? m.getEstado().name()     : null,
            m.getRaza(),
            m.getColor()      != null ? m.getColor().name()      : null,
            m.getTamano()     != null ? m.getTamano().name()     : null,
            m.getPelaje()     != null ? m.getPelaje().name()     : null,
            m.getEdad(),
            m.getRangoEdad()  != null ? m.getRangoEdad().name()  : null,
            senas,
            m.getDescripcion(),
            m.getUsuarioId()
        );
    }

    // ── Método principal ─────────────────────────────────────────────────────

    /**
     * Llama a POST /api/match en el microservicio FastAPI.
     * Si FastAPI no responde o hay error, retorna lista vacía sin romper el flujo.
     *
     * @param mascotaReportada mascota recién guardada en la DB
     * @param candidatas       mascotas con estado opuesto de la misma especie
     * @return resultados ordenados por score desc, o lista vacía ante cualquier fallo
     */
    public List<MatchResultDTO> buscarCoincidencias(Mascotas mascotaReportada,
                                                     List<Mascotas> candidatas) {
        if (candidatas == null || candidatas.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            MatchRequestBody body = new MatchRequestBody(
                toDTO(mascotaReportada),
                candidatas.stream().map(this::toDTO).collect(Collectors.toList())
            );

            List<MatchResultDTO> resultado = webClientBuilder.build()
                .post()
                .uri(fastapiUrl + "/api/match")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<MatchResultDTO>>() {})
                .timeout(Duration.ofSeconds(30))
                .onErrorResume(ex -> {
                    logger.error("FastAPI no respondió: {}", ex.getMessage());
                    return Mono.just(Collections.emptyList());
                })
                .block();

            return resultado != null ? resultado : Collections.emptyList();

        } catch (Exception e) {
            logger.error("Error al llamar al motor de coincidencias: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
