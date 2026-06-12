package mascotas.microservice.mascotas.service;

import java.util.List;
import java.util.Optional;

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

    @Value("${usuario.service.url:http://usuario:8081}")
    private String usuarioServiceUrl;

    @Value("${matcher.score.alerta:0.90}")
    private double matcherScoreAlerta;

    public MascotasServiceImpl(MascotasRepository mascotasRepository,
                               WebClient.Builder webClientBuilder,
                               MascotaMatcherClient mascotaMatcherClient,
                               NotificacionMatchService notificacionMatchService) {
        this.mascotasRepository = mascotasRepository;
        this.webClientBuilder = webClientBuilder;
        this.mascotaMatcherClient = mascotaMatcherClient;
        this.notificacionMatchService = notificacionMatchService;
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

        // Buscar coincidencias en segundo plano — si falla no interrumpe el flujo
        try {
            buscarYNotificarCoincidencias(guardada);
        } catch (Exception e) {
            logger.warn("Error al buscar coincidencias (no afecta el registro): {}", e.getMessage());
        }

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
            .filter(r -> r.getAlerta() != null && r.getAlerta())
            .forEach(r -> notificacionMatchService.notificarCoincidencia(mascotaNueva, r));
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
        try { buscarYNotificarCoincidencias(guardada); } catch (Exception e) {
            logger.warn("Error al buscar coincidencias tras cambio de estado: {}", e.getMessage());
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

        throw new RuntimeException("Mascota no encontrada con id: " + id);
    }

    @Override
    public void eliminarMascota(Long id) {
        if (mascotasRepository.existsById(id)) {
            mascotasRepository.deleteById(id);
        } else {
            throw new RuntimeException("Mascota no encontrada con id: " + id);
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
        } catch (WebClientResponseException.NotFound e) {
            return false;
        } catch (Exception e) {
            // Si hay error de red, se puede considerar como no encontrado o lanzar excepción
            return false;
        }
    }

}
