package mascotas.microservice.mascotas.client;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mascotas.microservice.mascotas.dto.MatchResultDTO;
import mascotas.microservice.mascotas.entity.Mascotas;
import mascotas.microservice.mascotas.exception.ServicioNoDisponibleException;

@Service
public class MascotaMatcherClient {

    private static final Logger logger = LoggerFactory.getLogger(MascotaMatcherClient.class);

    /** Nombre de la instancia de Circuit Breaker (ver application.properties). */
    private static final String CB_FASTAPI = "fastapiMatcher";

    private final WebClient.Builder webClientBuilder;

    @Value("${fastapi.url:http://localhost:8000}")
    private String fastapiUrl;

    public MascotaMatcherClient(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    // ── DTOs internos que mapean a MascotaInput y MatchRequest de FastAPI ──────

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MascotaInputDTO {
        private Long id;
        private String nombre;
        private String raza;
        private Integer edad;
        private String descripcion;
        private Long usuarioId;
        private String especie;
        private String estado;
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
        return new MascotaInputDTO(
            m.getId(),
            m.getNombre(),
            m.getRaza(),
            m.getEdad(),
            m.getDescripcion(),
            m.getUsuarioId(),
            m.getEspecie()  != null ? m.getEspecie().name()  : null,
            m.getEstado()   != null ? m.getEstado().name()   : null
        );
    }

    // ── Método principal ─────────────────────────────────────────────────────

    /**
     * Llama a POST /api/match en el microservicio FastAPI.
     */
    @CircuitBreaker(name = CB_FASTAPI, fallbackMethod = "buscarCoincidenciasFallback")
    public List<MatchResultDTO> buscarCoincidencias(Mascotas mascotaReportada,
                                                    List<Mascotas> candidatas) {
        if (candidatas == null || candidatas.isEmpty()) {
            return Collections.emptyList();
        }

        MatchRequestBody body = new MatchRequestBody(
            toDTO(mascotaReportada),
            candidatas.stream().map(this::toDTO).toList()
        );

        List<MatchResultDTO> resultado = webClientBuilder.build()
            .post()
            .uri(fastapiUrl + "/api/match")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<List<MatchResultDTO>>() {})
            .timeout(Duration.ofSeconds(30))
            .block();

        return resultado != null ? resultado : Collections.emptyList();
    }

    @SuppressWarnings("java:S1172")
    public List<MatchResultDTO> buscarCoincidenciasFallback(Mascotas mascotaReportada,
                                                            List<Mascotas> candidatas,
                                                            Throwable t) {
        logger.error("Circuit breaker '{}' activo o fallo de conexión con el motor de "
            + "coincidencias FastAPI: {}", CB_FASTAPI, t.getMessage());
        throw new ServicioNoDisponibleException(
            "El motor de coincidencias (FastAPI) no está disponible en este momento.", t);
    }
}
