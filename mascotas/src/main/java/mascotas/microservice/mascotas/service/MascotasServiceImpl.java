package mascotas.microservice.mascotas.service;

import mascotas.microservice.mascotas.entity.Mascotas;
import mascotas.microservice.mascotas.repository.MascotasRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MascotasServiceImpl implements MascotasService {

    @Autowired
    private MascotasRepository mascotasRepository;

    @Override
    public Mascotas crearMascota(Mascotas mascota) {
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
            mascotaAActualizar.setNombre(mascota.getNombre());
            mascotaAActualizar.setEspecie(mascota.getEspecie());
            mascotaAActualizar.setRaza(mascota.getRaza());
            mascotaAActualizar.setEdad(mascota.getEdad());
            mascotaAActualizar.setDescripcion(mascota.getDescripcion());

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
    public List<Mascotas> obtenerMascotasPorEspecie(String especie) {
        return mascotasRepository.findByEspecie(especie);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Mascotas> obtenerMascotasPorRaza(String raza) {
        return mascotasRepository.findByRaza(raza);
    }

}
