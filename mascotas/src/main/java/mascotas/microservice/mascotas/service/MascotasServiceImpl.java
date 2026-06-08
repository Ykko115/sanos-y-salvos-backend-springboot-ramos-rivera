package mascotas.microservice.mascotas.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import mascotas.microservice.mascotas.entity.Mascotas;
import mascotas.microservice.mascotas.entity.Mascotas.Especie;
import mascotas.microservice.mascotas.repository.MascotasRepository;

@Service
@Transactional
public class MascotasServiceImpl implements MascotasService {

    @Autowired
    private MascotasRepository mascotasRepository;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Value("${usuario.service.url:http://usuario:8081}")
    private String usuarioServiceUrl;

    @Override
    public Mascotas crearMascota(Mascotas mascota) {
        // Validar que el usuario existe en el microservicio usuario
        if (mascota.getUsuarioId() != null && !usuarioExiste(mascota.getUsuarioId())) {
            throw new RuntimeException("Usuario no encontrado con id: " + mascota.getUsuarioId());
        }
        return mascotasRepository.save(mascota);
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
    public Mascotas actualizarMascota(Long id, Mascotas mascota) {
        Optional<Mascotas> mascotaExistente = mascotasRepository.findById(id);

        if (mascotaExistente.isPresent()) {
            Mascotas mascotaAActualizar = mascotaExistente.get();
            // Validar que el usuario existe en el microservicio usuario si cambia el usuarioId
            if (mascota.getUsuarioId() != null && !mascota.getUsuarioId().equals(mascotaAActualizar.getUsuarioId())) {
                if (!usuarioExiste(mascota.getUsuarioId())) {
                    throw new RuntimeException("Usuario no encontrado con id: " + mascota.getUsuarioId());
                }
            }
            mascotaAActualizar.setNombre(mascota.getNombre());
            mascotaAActualizar.setEspecie(mascota.getEspecie());
            mascotaAActualizar.setRaza(mascota.getRaza());
            mascotaAActualizar.setEdad(mascota.getEdad());
            mascotaAActualizar.setDescripcion(mascota.getDescripcion());
            mascotaAActualizar.setUsuarioId(mascota.getUsuarioId());
            return mascotasRepository.save(mascotaAActualizar);
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
