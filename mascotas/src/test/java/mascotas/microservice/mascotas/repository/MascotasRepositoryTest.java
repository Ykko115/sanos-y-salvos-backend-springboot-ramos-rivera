package mascotas.microservice.mascotas.repository;

import mascotas.microservice.mascotas.entity.Mascotas;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application.properties")
class MascotasRepositoryTest {

    @Autowired
    private MascotasRepository mascotasRepository;

    private Mascotas mascota1;
    private Mascotas mascota2;

    @BeforeEach
    void setUp() {
        mascotasRepository.deleteAll();

        mascota1 = new Mascotas();
        mascota1.setNombre("Fido");
        mascota1.setEspecie(Mascotas.Especie.PERRO);
        mascota1.setRaza("Labrador");
        mascota1.setEdad(3);
        mascota1.setUsuarioId(1L);

        mascota2 = new Mascotas();
        mascota2.setNombre("Luna");
        mascota2.setEspecie(Mascotas.Especie.GATO);
        mascota2.setRaza("Siamés");
        mascota2.setEdad(2);
        mascota2.setUsuarioId(2L);

        mascotasRepository.save(mascota1);
        mascotasRepository.save(mascota2);
    }

    @Test
    void findByNombre_debeRetornarMascota() {
        Optional<Mascotas> resultado = mascotasRepository.findByNombre("Fido");
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getRaza()).isEqualTo("Labrador");
    }

   @Test
    void findByEspecie_debeRetornarFiltradas() {
        List<Mascotas> perros = mascotasRepository.findByEspecie(Mascotas.Especie.PERRO);
        assertThat(perros).hasSize(1);
        assertThat(perros.get(0).getNombre()).isEqualTo("Fido");
    }

    @Test
    void findByRaza_debeRetornarFiltradas() {
        List<Mascotas> siames = mascotasRepository.findByRaza("Siamés");
        assertThat(siames).hasSize(1);
        assertThat(siames.get(0).getNombre()).isEqualTo("Luna");
    }

    @Test
    void findByUsuarioId_debeRetornarFiltradas() {
        List<Mascotas> mascotas = mascotasRepository.findByUsuarioId(1L);
        assertThat(mascotas).hasSize(1);
        assertThat(mascotas.get(0).getNombre()).isEqualTo("Fido");
    }

    @Test
    void save_debeGenerarId() {
        Mascotas nueva = new Mascotas();
        nueva.setNombre("Coco");
        Mascotas guardada = mascotasRepository.save(nueva);
        assertThat(guardada.getId()).isNotNull();
    }

    @Test
    void deleteById_debeEliminar() {
        Long id = mascota1.getId();
        mascotasRepository.deleteById(id);
        assertThat(mascotasRepository.findById(id)).isEmpty();
    }
}