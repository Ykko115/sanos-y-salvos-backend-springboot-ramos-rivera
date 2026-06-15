package mascotas.microservice.mascotas.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import mascotas.microservice.mascotas.client.MascotaMatcherClient;
import mascotas.microservice.mascotas.dto.MatchResultDTO;
import mascotas.microservice.mascotas.entity.Mascotas;
import mascotas.microservice.mascotas.repository.MascotasRepository;

@Service
@Transactional
public class MascotasServiceImpl implements MascotasService {

    private static final Logger logger = LoggerFactory.getLogger(MascotasServiceImpl.class);

    private final MascotasRepository mascotasRepository;
    private final WebClient.Builder webClientBuilder;
    private final MascotaMatcherClient mascotaMatcherClient;
    private final NotificacionMatchService notificacionMatchService;
    private final KafkaProducerService kafkaProducerService;

    @Value("${usuario.service.url:http://usuario:8081}")
    private String usuarioServiceUrl;

    @Value("${matcher.score.alerta:0.90}")
    private double matcherScoreAlerta;

    @Value("${reportes.service.url:http://reportes:8083}")
    private String reportesServiceUrl;

    public MascotasServiceImpl(MascotasRepository mascotasRepository,
                               WebClient.Builder webClientBuilder,
                               MascotaMatcherClient mascotaMatcherClient,
                               NotificacionMatchService notificacionMatchService,
                               KafkaProducerService kafkaProducerService) {
        this.mascotasRepository = mascotasRepository;
        this.webClientBuilder = webClientBuilder;
        this.mascotaMatcherClient = mascotaMatcherClient;
        this.notificacionMatchService = notificacionMatchService;
        this.kafkaProducerService = kafkaProducerService;
    }

    @Override
    public Mascotas crearMascota(Mascotas mascota) {
        // Validar usuario solo si el servicio responde — si no, se guarda igual
        if (mascota.getUsuarioId() != null) {
            try {
                if (!usuarioExiste(mascota.getUsuarioId())) {
                    logger.warn("Usuario {} no encontrado, se guarda la mascota de todas formas", mascota.getUsuarioId());
                }
            } catch (Exception e) {
                logger.warn("No se pudo validar usuario {}: {}", mascota.getUsuarioId(), e.getMessage());
            }
        }
        Mascotas guardada = mascotasRepository.save(mascota);

        if (guardada.getEstado() == Mascotas.Estado.PERDIDO) {
            kafkaProducerService.publicarMascotaPerdida(guardada);
        } else if (guardada.getEstado() == Mascotas.Estado.ENCONTRADO) {
            kafkaProducerService.publicarMascotaEncontrada(guardada);
        }

        // buscar coincidencias en segundo plano
        final Mascotas mascotaFinal = guardada;
        CompletableFuture.runAsync(() -> {
            try {
                buscarYNotificarCoincidencias(mascotaFinal);
            } catch (Exception e) {
                logger.warn("Error al buscar coincidencias (no afecta el registro): {}", e.getMessage());
            }
        });

        return guardada;
    }

    private void buscarYNotificarCoincidencias(Mascotas mascotaNueva) {
        if (mascotaNueva.getEspecie() == null || mascotaNueva.getEstado() == null) {
            return;
        }
        // Buscar mascotas con estado opuesto y la misma especie
        Mascotas.Estado estadoOpuesto = mascotaNueva.getEstado() == Mascotas.Estado.PERDIDO
            ? Mascotas.Estado.ENCONTRADO
            : Mascotas.Estado.PERDIDO;

        List<Mascotas> candidatas = mascotasRepository
            .findByEspecieAndEstado(mascotaNueva.getEspecie(), estadoOpuesto);

        if (candidatas.isEmpty()) {
            return;
        }

        logger.info("Buscando coincidencias para mascota {} entre {} candidatas",
            mascotaNueva.getId(), candidatas.size());

        List<MatchResultDTO> resultados =
            mascotaMatcherClient.buscarCoincidencias(mascotaNueva, candidatas);

        resultados.stream()
            .filter(r -> (r.getAlerta() != null && r.getAlerta())
                      || (r.getScore() != null && r.getScore() >= matcherScoreAlerta))
            .forEach(r -> {
                Mascotas candidata = candidatas.stream()
                    .filter(c -> c.getId().equals(r.getMascotaId()))
                    .findFirst()
                    .orElse(null);
                if (candidata != null) {
                    notificacionMatchService.notificarCoincidencia(mascotaNueva, r, candidata);
                } else {
                    logger.warn("[MATCH] Candidata id={} no encontrada en lista local", r.getMascotaId());
                }
            });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Mascotas> obtenerMascotaPorId(Long id) {
        return mascotasRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Mascotas> obtenerTodasLasMascotas() {
        return mascotasRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Mascotas> obtenerMascotasPorEstado(Mascotas.Estado estado) {
        return mascotasRepository.findByEstado(estado);
    }

    @Override
    public Mascotas actualizarEstado(Long id, Mascotas.Estado estado) {
        Mascotas mascota = mascotasRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Mascota no encontrada con id: " + id));
        mascota.setEstado(estado);
        Mascotas guardada = mascotasRepository.save(mascota);
        if (estado == Mascotas.Estado.PERDIDO) {
            kafkaProducerService.publicarMascotaPerdida(guardada);
        } else if (estado == Mascotas.Estado.ENCONTRADO) {
            kafkaProducerService.publicarMascotaEncontrada(guardada);
        } else if (estado == Mascotas.Estado.REUNIDO) {
            if (guardada.getUsuarioId() != null) {
                notificacionMatchService.notificarReunion(guardada);
            }
            // eliminar el reporte asociado en el servicio de reportes
            try {
                webClientBuilder.build()
                    .delete()
                    .uri(reportesServiceUrl + "/api/reportes/mascota/" + id)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .subscribe(
                        v -> logger.info("Reporte eliminado para mascota id={}", id),
                        e -> logger.warn("No se pudo eliminar reporte para mascota id={}: {}", id, e.getMessage())
                    );
            } catch (Exception e) {
                logger.warn("Error al llamar a reportes para eliminar reporte de mascota {}: {}", id, e.getMessage());
            }
        }

        // Una mascota REUNIDA ya no participa en el matching — buscar
        // coincidencias para ella generaría avisos espurios.
        if (estado != Mascotas.Estado.REUNIDO) {
            final Mascotas mascotaActualizada = guardada;
            CompletableFuture.runAsync(() -> {
                try {
                    buscarYNotificarCoincidencias(mascotaActualizada);
                } catch (Exception e) {
                    logger.warn("Error al buscar coincidencias tras cambio de estado: {}", e.getMessage());
                }
            });
        }
        return guardada;
    }

    @Override
    public Mascotas actualizarMascota(Long id, Mascotas mascota) {
        Optional<Mascotas> mascotaExistente = mascotasRepository.findById(id);

        if (mascotaExistente.isPresent()) {
            Mascotas m = mascotaExistente.get();
            // Validación de usuario no bloquea el guardado si el servicio no responde
            m.setNombre(mascota.getNombre());
            m.setEspecie(mascota.getEspecie());
            m.setRaza(mascota.getRaza());
            m.setColor(mascota.getColor());
            m.setTamano(mascota.getTamano());
            m.setPelaje(mascota.getPelaje());
            m.setEdad(mascota.getEdad());
            m.setRangoEdad(mascota.getRangoEdad());
            if (mascota.getSenas() != null) m.setSenas(mascota.getSenas());
            m.setDescripcion(mascota.getDescripcion());
            m.setFotoUrl(mascota.getFotoUrl());
            m.setLat(mascota.getLat());
            m.setLng(mascota.getLng());
            if (mascota.getEstado() != null) m.setEstado(mascota.getEstado());
            if (mascota.getUsuarioId() != null) m.setUsuarioId(mascota.getUsuarioId());
            return mascotasRepository.save(m);
        }

        throw new NoSuchElementException("Mascota no encontrada con id: " + id);
    }

    @Override
    public void eliminarMascota(Long id) {
        if (mascotasRepository.existsById(id)) {
            mascotasRepository.deleteById(id);
        } else {
            throw new NoSuchElementException("Mascota no encontrada con id: " + id);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Mascotas> obtenerMascotaPorNombre(String nombre) {
        return mascotasRepository.findByNombre(nombre);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Mascotas> obtenerMascotasPorEspecie(Mascotas.Especie especie) {
        return mascotasRepository.findByEspecie(especie);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Mascotas> obtenerMascotasPorRaza(String raza) {
        return mascotasRepository.findByRaza(raza);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Mascotas> obtenerMascotasPorUsuarioId(Long usuarioId) {
        return mascotasRepository.findByUsuarioId(usuarioId);
    }

    // Método auxiliar para validar existencia de usuario
    private boolean usuarioExiste(Long usuarioId) {
        try {
            WebClient webClient = webClientBuilder.build();
            // Llama a /api/usuario/{id} en el microservicio usuario
            webClient.get()
                .uri(usuarioServiceUrl + "/api/usuario/" + usuarioId)
                .retrieve()
                .bodyToMono(Object.class)
                .block();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
