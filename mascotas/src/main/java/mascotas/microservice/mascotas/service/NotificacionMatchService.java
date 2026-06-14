package mascotas.microservice.mascotas.service;

import mascotas.microservice.mascotas.dto.MatchResultDTO;
import mascotas.microservice.mascotas.entity.Mascotas;
import mascotas.microservice.mascotas.entity.NotificacionMatch;
import mascotas.microservice.mascotas.repository.NotificacionMatchRepository;
import mascotas.microservice.mascotas.service.KafkaProducerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificacionMatchService {

    private static final Logger logger = LoggerFactory.getLogger(NotificacionMatchService.class);

    @Autowired
    private NotificacionMatchRepository notificacionMatchRepository;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    /**
     * Guarda la coincidencia en notificaciones_match y publica en Kafka.
     * La notificación siempre se dirige al dueño de la mascota PERDIDA,
     * independientemente de cuál de las dos acaba de ser registrada.
     *
     * @param mascotaNueva mascota recién registrada o cuyo estado cambió
     * @param resultado    resultado del motor de coincidencias con alerta=true
     * @param candidata    la otra mascota de la comparación (estado opuesto)
     */
    public void notificarCoincidencia(Mascotas mascotaNueva, MatchResultDTO resultado, Mascotas candidata) {
        try {
            boolean nuevaEsPerdida = mascotaNueva.getEstado() == Mascotas.Estado.PERDIDO;
            Mascotas perdida   = nuevaEsPerdida ? mascotaNueva : candidata;
            Mascotas encontrada = nuevaEsPerdida ? candidata  : mascotaNueva;

            // No duplicar: si ya existe una coincidencia para este par, no se reinserta
            if (notificacionMatchRepository
                    .existsByMascotaIdPerdidaAndMascotaIdCandidata(perdida.getId(), encontrada.getId())) {
                logger.info("Coincidencia perdida={} candidata={} ya registrada, se omite",
                    perdida.getId(), encontrada.getId());
                return;
            }

            logger.info("COINCIDENCIA ENCONTRADA: perdida={} encontrada={} score={}%",
                perdida.getId(), encontrada.getId(), resultado.getPorcentaje());

            NotificacionMatch notif = new NotificacionMatch();
            notif.setUsuarioId(perdida.getUsuarioId());
            notif.setMascotaIdPerdida(perdida.getId());
            notif.setMascotaIdCandidata(encontrada.getId());
            notif.setScore(resultado.getScore());
            notif.setPorcentaje(resultado.getPorcentaje());
            notif.setMensaje(resultado.getMensaje());
            notif.setFechaNotificacion(LocalDateTime.now());
            notif.setLeida(false);

            notificacionMatchRepository.save(notif);

            kafkaProducerService.publicarCoincidenciaNueva(perdida, resultado);

        } catch (Exception e) {
            logger.error("Error al guardar notificación: {}", e.getMessage());
        }
    }

    public List<NotificacionMatch> obtenerPorUsuario(Long usuarioId) {
        return notificacionMatchRepository.findByUsuarioId(usuarioId);
    }

    public void marcarLeida(Long id) {
        notificacionMatchRepository.findById(id).ifPresent(n -> {
            n.setLeida(true);
            notificacionMatchRepository.save(n);
        });
    }

    public void notificarReunion(Mascotas mascota) {
        try {
            kafkaProducerService.publicarMascotaReunida(mascota);
            logger.info("[KAFKA] mascota reunida notificada id={}",
                mascota.getId());
        } catch (Exception e) {
            logger.warn("Error notificando reunion: {}", e.getMessage());
        }
    }
}
