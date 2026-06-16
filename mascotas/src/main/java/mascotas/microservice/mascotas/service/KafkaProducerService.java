package mascotas.microservice.mascotas.service;

import mascotas.microservice.mascotas.entity.Mascotas;
import mascotas.microservice.mascotas.dto.MatchResultDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

@Service
public class KafkaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    private static final String MASCOTA_ID = "mascotaId";
    private static final String USUARIO_ID = "usuarioId";
    private static final String TIMESTAMP   = "timestamp";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publicarMascotaPerdida(Mascotas mascota) {
        try {
            Map<String, Object> msg = new HashMap<>();
            msg.put("tipo", "MASCOTA_PERDIDA");
            msg.put(MASCOTA_ID, mascota.getId());
            msg.put("nombre", mascota.getNombre());
            msg.put("especie", mascota.getEspecie() != null ? mascota.getEspecie().name() : null);
            msg.put("raza", mascota.getRaza());
            msg.put("color", mascota.getColor() != null ? mascota.getColor().name() : null);
            msg.put("tamano", mascota.getTamano() != null ? mascota.getTamano().name() : null);
            msg.put("lat", mascota.getLat());
            msg.put("lng", mascota.getLng());
            msg.put(USUARIO_ID, mascota.getUsuarioId());
            msg.put(TIMESTAMP, LocalDateTime.now(ZoneId.systemDefault()).toString());
            kafkaTemplate.send("mascota.perdida", msg);
            logger.info("[KAFKA] publicado mascota.perdida id={}", mascota.getId());
        } catch (Exception e) {
            logger.warn("[KAFKA] error mascota.perdida: {} | causa: {}", e.getMessage(),
                e.getCause() != null ? e.getCause().getMessage() : "n/a");
        }
    }

    public void publicarMascotaEncontrada(Mascotas mascota) {
        try {
            Map<String, Object> msg = new HashMap<>();
            msg.put("tipo", "MASCOTA_ENCONTRADA");
            msg.put(MASCOTA_ID, mascota.getId());
            msg.put("especie", mascota.getEspecie() != null ? mascota.getEspecie().name() : null);
            msg.put("raza", mascota.getRaza());
            msg.put("color", mascota.getColor() != null ? mascota.getColor().name() : null);
            msg.put("tamano", mascota.getTamano() != null ? mascota.getTamano().name() : null);
            msg.put("lat", mascota.getLat());
            msg.put("lng", mascota.getLng());
            msg.put(TIMESTAMP, LocalDateTime.now(ZoneId.systemDefault()).toString());
            kafkaTemplate.send("mascota.encontrada", msg);
            logger.info("[KAFKA] publicado mascota.encontrada id={}", mascota.getId());
        } catch (Exception e) {
            logger.warn("[KAFKA] error mascota.encontrada: {} | causa: {}", e.getMessage(),
                e.getCause() != null ? e.getCause().getMessage() : "n/a");
        }
    }

    public void publicarCoincidenciaNueva(Mascotas mascota, MatchResultDTO match) {
        try {
            Map<String, Object> msg = new HashMap<>();
            msg.put("tipo", "COINCIDENCIA_NUEVA");
            msg.put("mascotaIdPerdida", mascota.getId());
            msg.put("mascotaIdCandidata", match.getMascotaId());
            msg.put("score", match.getScore());
            msg.put("porcentaje", match.getPorcentaje());
            msg.put("mensaje", match.getMensaje());
            msg.put(USUARIO_ID, mascota.getUsuarioId());
            msg.put(TIMESTAMP, LocalDateTime.now(ZoneId.systemDefault()).toString());
            kafkaTemplate.send("coincidencia.nueva", msg);
            logger.info("[KAFKA] publicado coincidencia.nueva score={}%", match.getPorcentaje());
        } catch (Exception e) {
            logger.warn("[KAFKA] error coincidencia.nueva: {} | causa: {}", e.getMessage(),
                e.getCause() != null ? e.getCause().getMessage() : "n/a");
        }
    }

    public void publicarMascotaReunida(Mascotas mascota) {
        try {
            Map<String, Object> msg = new HashMap<>();
            msg.put("tipo", "MASCOTA_REUNIDA");
            msg.put(MASCOTA_ID, mascota.getId());
            msg.put("nombre", mascota.getNombre());
            msg.put(USUARIO_ID, mascota.getUsuarioId());
            msg.put(TIMESTAMP, LocalDateTime.now(ZoneId.systemDefault()).toString());
            kafkaTemplate.send("mascota.reunida", msg);
            logger.info("[KAFKA] publicado mascota.reunida id={}", mascota.getId());
        } catch (Exception e) {
            logger.warn("[KAFKA] error mascota.reunida: {} | causa: {}", e.getMessage(),
                e.getCause() != null ? e.getCause().getMessage() : "n/a");
        }
    }
}
