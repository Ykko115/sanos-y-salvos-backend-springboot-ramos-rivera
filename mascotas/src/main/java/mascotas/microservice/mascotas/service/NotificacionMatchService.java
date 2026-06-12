package mascotas.microservice.mascotas.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import mascotas.microservice.mascotas.dto.MatchResultDTO;
import mascotas.microservice.mascotas.entity.Mascotas;
import mascotas.microservice.mascotas.entity.NotificacionMatch;
import mascotas.microservice.mascotas.repository.NotificacionMatchRepository;

@Service
public class NotificacionMatchService {

    private static final Logger logger = LoggerFactory.getLogger(NotificacionMatchService.class);

    private final NotificacionMatchRepository notificacionMatchRepository;

    public NotificacionMatchService(NotificacionMatchRepository notificacionMatchRepository) {
        this.notificacionMatchRepository = notificacionMatchRepository;
    }

    /**
     * Guarda la coincidencia en la tabla notificaciones_match y loggea el evento.
     */
    public void notificarCoincidencia(Mascotas mascotaUsuario, MatchResultDTO resultado) {
        try {
            logger.info("COINCIDENCIA ENCONTRADA: mascota {} tiene {}% de coincidencia con mascota {}",
                mascotaUsuario.getId(),
                resultado.getPorcentaje(),
                resultado.getMascotaId()
            );

            NotificacionMatch notif = new NotificacionMatch();
            notif.setUsuarioId(mascotaUsuario.getUsuarioId());
            notif.setMascotaIdPerdida(mascotaUsuario.getId());
            notif.setMascotaIdCandidata(resultado.getMascotaId());
            notif.setScore(resultado.getScore());
            notif.setPorcentaje(resultado.getPorcentaje());
            notif.setMensaje(resultado.getMensaje());
            notif.setFechaNotificacion(LocalDateTime.now());
            notif.setLeida(false);

            notificacionMatchRepository.save(notif);

        } catch (Exception e) {
            logger.error("Error al guardar notificación de coincidencia: {}", e.getMessage());
        }
    }
}
