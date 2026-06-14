package mascotas.microservice.mascotas.repository;

import mascotas.microservice.mascotas.entity.Mascotas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MascotasRepository extends JpaRepository<Mascotas, Long> {

    Optional<Mascotas> findByNombre(String nombre);

    List<Mascotas> findByEspecie(Mascotas.Especie especie);

    List<Mascotas> findByRaza(String raza);

    List<Mascotas> findByUsuarioId(Long usuarioId);

    List<Mascotas> findByEstado(Mascotas.Estado estado);

    List<Mascotas> findByEspecieAndEstado(Mascotas.Especie especie, Mascotas.Estado estado);

}
