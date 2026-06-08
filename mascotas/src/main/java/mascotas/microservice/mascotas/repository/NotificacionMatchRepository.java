package mascotas.microservice.mascotas.repository;

import mascotas.microservice.mascotas.entity.NotificacionMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionMatchRepository extends JpaRepository<NotificacionMatch, Long> {

    List<NotificacionMatch> findByUsuarioId(Long usuarioId);

    List<NotificacionMatch> findByMascotaIdPerdida(Long mascotaId);

    List<NotificacionMatch> findByLeidaFalse();
}
