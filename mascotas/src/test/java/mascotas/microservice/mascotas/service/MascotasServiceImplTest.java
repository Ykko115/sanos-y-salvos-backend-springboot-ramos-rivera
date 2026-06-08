package mascotas.microservice.mascotas.service;

import mascotas.microservice.mascotas.entity.Mascotas;
import mascotas.microservice.mascotas.repository.MascotasRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.*;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MascotasServiceImplTest {

    @Mock
    private MascotasRepository mascotasRepository;

    @Mock
    private WebClient.Builder webClientBuilder;

    @InjectMocks
    private MascotasServiceImpl mascotasService;

    private Mascotas mascota;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mascotasService, "usuarioServiceUrl", "http://localhost:8081");

        mascota = new Mascotas();
        mascota.setId(1L);
        mascota.setNombre("Fido");
        mascota.setEspecie(Mascotas.Especie.PERRO);
        mascota.setRaza("Labrador");
        mascota.setEdad(3);
        mascota.setDescripcion("Perro amigable");
        mascota.setUsuarioId(1L);
    }

    // ─── Helpers WebClient ────────────────────────────────────────────────────

    private WebClient buildWebClientMockOk() {
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        when(responseSpec.bodyToMono(Object.class)).thenReturn(Mono.just(new Object()));

        @SuppressWarnings("rawtypes")
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        when(headersSpec.retrieve()).thenReturn(responseSpec);

        @SuppressWarnings("rawtypes")
        WebClient.RequestHeadersUriSpec uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        when(uriSpec.uri(anyString())).thenReturn(headersSpec);

        WebClient webClient = mock(WebClient.class);
        when(webClient.get()).thenReturn(uriSpec);
        return webClient;
    }

    private WebClient buildWebClientMockError() {
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        when(responseSpec.bodyToMono(Object.class))
            .thenReturn(Mono.error(WebClientResponseException.create(
                404, "Not Found",
                org.springframework.http.HttpHeaders.EMPTY,
                new byte[0],
                java.nio.charset.StandardCharsets.UTF_8)));

        @SuppressWarnings("rawtypes")
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        when(headersSpec.retrieve()).thenReturn(responseSpec);

        @SuppressWarnings("rawtypes")
        WebClient.RequestHeadersUriSpec uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        when(uriSpec.uri(anyString())).thenReturn(headersSpec);

        WebClient webClient = mock(WebClient.class);
        when(webClient.get()).thenReturn(uriSpec);
        return webClient;
    }

    // ─── crearMascota ─────────────────────────────────────────────────────────

    @Test
    void crearMascota_sinUsuarioId_debeGuardarDirectamente() {
        mascota.setUsuarioId(null);
        when(mascotasRepository.save(mascota)).thenReturn(mascota);

        Mascotas resultado = mascotasService.crearMascota(mascota);

        assertThat(resultado.getNombre()).isEqualTo("Fido");
        verify(mascotasRepository).save(mascota);
        verifyNoInteractions(webClientBuilder);
    }

    @Test
    void crearMascota_conUsuarioValido_debeGuardar() {
        WebClient mockOk = buildWebClientMockOk();
        when(webClientBuilder.build()).thenReturn(mockOk);
        when(mascotasRepository.save(mascota)).thenReturn(mascota);

        Mascotas resultado = mascotasService.crearMascota(mascota);

        assertThat(resultado).isNotNull();
        verify(mascotasRepository).save(mascota);
    }

    @Test
    void crearMascota_usuarioNoExiste_debeLanzarExcepcion() {
        WebClient mockError = buildWebClientMockError();
        when(webClientBuilder.build()).thenReturn(mockError);

        assertThatThrownBy(() -> mascotasService.crearMascota(mascota))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Usuario no encontrado");

        verify(mascotasRepository, never()).save(any());
    }

    // ─── obtenerMascotaPorId ──────────────────────────────────────────────────

    @Test
    void obtenerMascotaPorId_existente_debeRetornar() {
        when(mascotasRepository.findById(1L)).thenReturn(Optional.of(mascota));

        Optional<Mascotas> resultado = mascotasService.obtenerMascotaPorId(1L);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNombre()).isEqualTo("Fido");
    }

    @Test
    void obtenerMascotaPorId_inexistente_debeRetornarVacio() {
        when(mascotasRepository.findById(999L)).thenReturn(Optional.empty());

        assertThat(mascotasService.obtenerMascotaPorId(999L)).isEmpty();
    }

    // ─── obtenerTodasLasMascotas ──────────────────────────────────────────────

    @Test
    void obtenerTodasLasMascotas_debeRetornarLista() {
        when(mascotasRepository.findAll()).thenReturn(List.of(mascota));

        assertThat(mascotasService.obtenerTodasLasMascotas()).hasSize(1);
    }

    // ─── actualizarMascota ────────────────────────────────────────────────────

    @Test
    void actualizarMascota_existente_debeActualizar() {
        Mascotas actualizada = new Mascotas();
        actualizada.setNombre("Max");
        actualizada.setEspecie(Mascotas.Especie.GATO);
        actualizada.setRaza("Siamés");
        actualizada.setEdad(2);
        actualizada.setDescripcion("Gato tranquilo");
        actualizada.setUsuarioId(1L); // mismo usuarioId, no llama WebClient

        when(mascotasRepository.findById(1L)).thenReturn(Optional.of(mascota));
        when(mascotasRepository.save(any(Mascotas.class))).thenAnswer(inv -> inv.getArgument(0));

        Mascotas resultado = mascotasService.actualizarMascota(1L, actualizada);

        assertThat(resultado.getNombre()).isEqualTo("Max");
        assertThat(resultado.getEspecie()).isEqualTo(Mascotas.Especie.GATO);
    }

    @Test
    void actualizarMascota_inexistente_debeLanzarExcepcion() {
        when(mascotasRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mascotasService.actualizarMascota(999L, mascota))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("999");
    }

    // ─── eliminarMascota ──────────────────────────────────────────────────────

    @Test
    void eliminarMascota_existente_debeEliminar() {
        when(mascotasRepository.existsById(1L)).thenReturn(true);
        doNothing().when(mascotasRepository).deleteById(1L);

        assertThatCode(() -> mascotasService.eliminarMascota(1L)).doesNotThrowAnyException();
        verify(mascotasRepository).deleteById(1L);
    }

    @Test
    void eliminarMascota_inexistente_debeLanzarExcepcion() {
        when(mascotasRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> mascotasService.eliminarMascota(999L))
            .isInstanceOf(RuntimeException.class);
    }

    // ─── filtros ──────────────────────────────────────────────────────────────

    @Test
    void obtenerMascotaPorNombre_debeRetornar() {
        when(mascotasRepository.findByNombre("Fido")).thenReturn(Optional.of(mascota));

        assertThat(mascotasService.obtenerMascotaPorNombre("Fido")).isPresent();
    }

    @Test
    void obtenerMascotasPorEspecie_debeRetornarFiltradas() {
        when(mascotasRepository.findByEspecie(Mascotas.Especie.PERRO)).thenReturn(List.of(mascota));

        assertThat(mascotasService.obtenerMascotasPorEspecie(Mascotas.Especie.PERRO)).hasSize(1);
    }

    @Test
    void obtenerMascotasPorRaza_debeRetornarFiltradas() {
        when(mascotasRepository.findByRaza("Labrador")).thenReturn(List.of(mascota));

        assertThat(mascotasService.obtenerMascotasPorRaza("Labrador")).hasSize(1);
    }

    @Test
    void obtenerMascotasPorUsuarioId_debeRetornarFiltradas() {
        when(mascotasRepository.findByUsuarioId(1L)).thenReturn(List.of(mascota));

        assertThat(mascotasService.obtenerMascotasPorUsuarioId(1L)).hasSize(1);
    }
}