package mascotas.microservice.mascotas.service;

import mascotas.microservice.mascotas.dto.MatchResultDTO;
import mascotas.microservice.mascotas.entity.Mascotas;
import mascotas.microservice.mascotas.entity.NotificacionMatch;
import mascotas.microservice.mascotas.repository.NotificacionMatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionMatchServiceTest {

    @Mock
    private NotificacionMatchRepository notificacionMatchRepository;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private NotificacionMatchService notificacionMatchService;

    private Mascotas mascotaPerdida;
    private Mascotas mascotaEncontrada;
    private MatchResultDTO resultado;

    @BeforeEach
    void setUp() {
        mascotaPerdida = new Mascotas();
        mascotaPerdida.setId(1L);
        mascotaPerdida.setNombre("Fido");
        mascotaPerdida.setEstado(Mascotas.Estado.PERDIDO);
        mascotaPerdida.setUsuarioId(10L);

        mascotaEncontrada = new Mascotas();
        mascotaEncontrada.setId(2L);
        mascotaEncontrada.setNombre("Max");
        mascotaEncontrada.setEstado(Mascotas.Estado.ENCONTRADO);
        mascotaEncontrada.setUsuarioId(20L);

        resultado = new MatchResultDTO();
        resultado.setMascotaId(2L);
        resultado.setScore(0.88);
        resultado.setPorcentaje(88);
        resultado.setMensaje("Coincidencia alta");
    }

    // ─── notificarCoincidencia ────────────────────────────────────────────────

    @Test
    void notificarCoincidencia_nuevaEsPerdida_debeGuardarNotificacionYPublicarKafka() {
        when(notificacionMatchRepository.existsByMascotaIdPerdidaAndMascotaIdCandidata(1L, 2L))
            .thenReturn(false);
        when(notificacionMatchRepository.save(any(NotificacionMatch.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        assertThatCode(() ->
            notificacionMatchService.notificarCoincidencia(mascotaPerdida, resultado, mascotaEncontrada))
            .doesNotThrowAnyException();

        ArgumentCaptor<NotificacionMatch> captor = ArgumentCaptor.forClass(NotificacionMatch.class);
        verify(notificacionMatchRepository).save(captor.capture());
        NotificacionMatch notif = captor.getValue();

        assertThat(notif.getUsuarioId()).isEqualTo(10L);
        assertThat(notif.getMascotaIdPerdida()).isEqualTo(1L);
        assertThat(notif.getMascotaIdCandidata()).isEqualTo(2L);
        assertThat(notif.getScore()).isEqualTo(0.88);
        assertThat(notif.getLeida()).isFalse();

        verify(kafkaProducerService).publicarCoincidenciaNueva(mascotaPerdida, resultado);
    }

    @Test
    void notificarCoincidencia_nuevaEsEncontrada_debeUsarPerdidaComoDestinatario() {
        when(notificacionMatchRepository.existsByMascotaIdPerdidaAndMascotaIdCandidata(1L, 2L))
            .thenReturn(false);
        when(notificacionMatchRepository.save(any(NotificacionMatch.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        // la mascotaEncontrada es la "nueva" y la perdida es la "candidata"
        notificacionMatchService.notificarCoincidencia(mascotaEncontrada, resultado, mascotaPerdida);

        ArgumentCaptor<NotificacionMatch> captor = ArgumentCaptor.forClass(NotificacionMatch.class);
        verify(notificacionMatchRepository).save(captor.capture());

        // el usuarioId debe ser el del dueño de la mascota PERDIDA
        assertThat(captor.getValue().getUsuarioId()).isEqualTo(10L);
        assertThat(captor.getValue().getMascotaIdPerdida()).isEqualTo(1L);
        assertThat(captor.getValue().getMascotaIdCandidata()).isEqualTo(2L);
    }

    @Test
    void notificarCoincidencia_yaExiste_noDebeGuardarNiPublicar() {
        when(notificacionMatchRepository.existsByMascotaIdPerdidaAndMascotaIdCandidata(1L, 2L))
            .thenReturn(true);

        notificacionMatchService.notificarCoincidencia(mascotaPerdida, resultado, mascotaEncontrada);

        verify(notificacionMatchRepository, never()).save(any());
        verify(kafkaProducerService, never()).publicarCoincidenciaNueva(any(), any());
    }

    // ─── notificarReunion ────────────────────────────────────────────────────

    @Test
    void notificarReunion_debePublicarKafkaMascotaReunida() {
        assertThatCode(() -> notificacionMatchService.notificarReunion(mascotaPerdida))
            .doesNotThrowAnyException();

        verify(kafkaProducerService).publicarMascotaReunida(mascotaPerdida);
    }

    @Test
    void notificarReunion_kafkaFalla_noLanzaExcepcion() {
        doThrow(new RuntimeException("Kafka caído"))
            .when(kafkaProducerService).publicarMascotaReunida(any());

        assertThatCode(() -> notificacionMatchService.notificarReunion(mascotaPerdida))
            .doesNotThrowAnyException();
    }

    // ─── obtenerPorUsuario ───────────────────────────────────────────────────

    @Test
    void obtenerPorUsuario_debeRetornarListaDelRepositorio() {
        NotificacionMatch notif = new NotificacionMatch();
        notif.setId(1L);
        notif.setUsuarioId(10L);

        when(notificacionMatchRepository.findByUsuarioId(10L)).thenReturn(List.of(notif));

        List<NotificacionMatch> notifs = notificacionMatchService.obtenerPorUsuario(10L);

        assertThat(notifs).hasSize(1);
        assertThat(notifs.get(0).getUsuarioId()).isEqualTo(10L);
    }

    @Test
    void obtenerPorUsuario_sinNotificaciones_debeRetornarListaVacia() {
        when(notificacionMatchRepository.findByUsuarioId(99L)).thenReturn(List.of());

        assertThat(notificacionMatchService.obtenerPorUsuario(99L)).isEmpty();
    }

    // ─── marcarLeida ────────────────────────────────────────────────────────

    @Test
    void marcarLeida_existente_debeActualizarFlag() {
        NotificacionMatch notif = new NotificacionMatch();
        notif.setId(1L);
        notif.setLeida(false);

        when(notificacionMatchRepository.findById(1L)).thenReturn(Optional.of(notif));
        when(notificacionMatchRepository.save(any(NotificacionMatch.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        notificacionMatchService.marcarLeida(1L);

        assertThat(notif.getLeida()).isTrue();
        verify(notificacionMatchRepository).save(notif);
    }

    @Test
    void marcarLeida_inexistente_noLanzaExcepcion() {
        when(notificacionMatchRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatCode(() -> notificacionMatchService.marcarLeida(999L))
            .doesNotThrowAnyException();

        verify(notificacionMatchRepository, never()).save(any());
    }
}
