package mascotas.microservice.mascotas.service;

import mascotas.microservice.mascotas.dto.MatchResultDTO;
import mascotas.microservice.mascotas.entity.Mascotas;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;

@ExtendWith(MockitoExtension.class)
class KafkaProducerServiceTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private KafkaProducerService kafkaProducerService;

    private Mascotas mascota;
    private MatchResultDTO match;

    @BeforeEach
    void setUp() {
        mascota = new Mascotas();
        mascota.setId(1L);
        mascota.setNombre("Fido");
        mascota.setEspecie(Mascotas.Especie.PERRO);
        mascota.setRaza("Labrador");
        mascota.setColor(Mascotas.Color.CAFE);
        mascota.setTamano(Mascotas.Tamano.GRANDE);
        mascota.setLat(-33.45);
        mascota.setLng(-70.66);
        mascota.setUsuarioId(5L);

        match = new MatchResultDTO();
        match.setMascotaId(2L);
        match.setScore(0.92);
        match.setPorcentaje(92);
        match.setMensaje("Alta coincidencia");

        // lenient: los tests de error sobreescriben este stub
        lenient().when(kafkaTemplate.send(any(String.class), any())).thenReturn(new CompletableFuture<>());
    }

    @Test
    void publicarMascotaPerdida_debeEnviarAlTopicCorrecto() {
        assertThatCode(() -> kafkaProducerService.publicarMascotaPerdida(mascota))
            .doesNotThrowAnyException();

        verify(kafkaTemplate).send(eq("mascota.perdida"), any());
    }

    @Test
    void publicarMascotaPerdida_conEnumsNull_noLanzaExcepcion() {
        mascota.setEspecie(null);
        mascota.setColor(null);
        mascota.setTamano(null);

        assertThatCode(() -> kafkaProducerService.publicarMascotaPerdida(mascota))
            .doesNotThrowAnyException();
    }

    @Test
    void publicarMascotaEncontrada_debeEnviarAlTopicCorrecto() {
        assertThatCode(() -> kafkaProducerService.publicarMascotaEncontrada(mascota))
            .doesNotThrowAnyException();

        verify(kafkaTemplate).send(eq("mascota.encontrada"), any());
    }

    @Test
    void publicarMascotaEncontrada_conEnumsNull_noLanzaExcepcion() {
        mascota.setEspecie(null);
        mascota.setColor(null);
        mascota.setTamano(null);

        assertThatCode(() -> kafkaProducerService.publicarMascotaEncontrada(mascota))
            .doesNotThrowAnyException();
    }

    @Test
    void publicarCoincidenciaNueva_debeEnviarAlTopicCorrecto() {
        assertThatCode(() -> kafkaProducerService.publicarCoincidenciaNueva(mascota, match))
            .doesNotThrowAnyException();

        verify(kafkaTemplate).send(eq("coincidencia.nueva"), any());
    }

    @Test
    void publicarMascotaReunida_debeEnviarAlTopicCorrecto() {
        assertThatCode(() -> kafkaProducerService.publicarMascotaReunida(mascota))
            .doesNotThrowAnyException();

        verify(kafkaTemplate).send(eq("mascota.reunida"), any());
    }

    @Test
    void publicarMascotaPerdida_kafkaError_noLanzaExcepcion() {
        when(kafkaTemplate.send(any(String.class), any()))
            .thenThrow(new RuntimeException("Kafka no disponible"));

        assertThatCode(() -> kafkaProducerService.publicarMascotaPerdida(mascota))
            .doesNotThrowAnyException();
    }

    @Test
    void publicarMascotaEncontrada_kafkaError_noLanzaExcepcion() {
        when(kafkaTemplate.send(any(String.class), any()))
            .thenThrow(new RuntimeException("Kafka no disponible"));

        assertThatCode(() -> kafkaProducerService.publicarMascotaEncontrada(mascota))
            .doesNotThrowAnyException();
    }

    @Test
    void publicarCoincidenciaNueva_kafkaError_noLanzaExcepcion() {
        when(kafkaTemplate.send(any(String.class), any()))
            .thenThrow(new RuntimeException("Kafka no disponible"));

        assertThatCode(() -> kafkaProducerService.publicarCoincidenciaNueva(mascota, match))
            .doesNotThrowAnyException();
    }

    @Test
    void publicarMascotaReunida_kafkaError_noLanzaExcepcion() {
        when(kafkaTemplate.send(any(String.class), any()))
            .thenThrow(new RuntimeException("Kafka no disponible"));

        assertThatCode(() -> kafkaProducerService.publicarMascotaReunida(mascota))
            .doesNotThrowAnyException();
    }
}
