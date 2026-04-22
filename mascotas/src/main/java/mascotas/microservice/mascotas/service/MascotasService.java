package mascotas.microservice.mascotas.service;

import mascotas.microservice.mascotas.entity.Mascotas;

import java.util.List;
import java.util.Optional;

public interface MascotasService {

    Mascotas crearMascota(Mascotas mascota);
    Optional<Mascotas> obtenerMascotaPorId(Long id);
    List<Mascotas> obtenerTodasLasMascotas();
    Mascotas actualizarMascota(Long id, Mascotas mascota);
    void eliminarMascota(Long id);
    Optional<Mascotas> obtenerMascotaPorNombre(String nombre);
    List<Mascotas> obtenerMascotasPorEspecie(String especie);
    List<Mascotas> obtenerMascotasPorRaza(String raza);

}
