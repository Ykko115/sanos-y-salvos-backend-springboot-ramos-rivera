package mascotas.microservice.mascotas.service;

import mascotas.microservice.mascotas.entity.Mascotas;

import java.util.List;
import java.util.Optional;

public interface MascotasService {

    Mascotas crearMascota(Mascotas mascota);
    Optional<Mascotas> obtenerMascotaPorId(Long id);
    List<Mascotas> obtenerTodasLasMascotas();
    List<Mascotas> obtenerMascotasPorEstado(Mascotas.Estado estado);
    Mascotas actualizarMascota(Long id, Mascotas mascota);
    Mascotas actualizarEstado(Long id, Mascotas.Estado estado);
    void eliminarMascota(Long id);
    Optional<Mascotas> obtenerMascotaPorNombre(String nombre);
    List<Mascotas> obtenerMascotasPorEspecie(String especie);
    List<Mascotas> obtenerMascotasPorRaza(String raza);
    List<Mascotas> obtenerMascotasPorUsuarioId(Long usuarioId);

}