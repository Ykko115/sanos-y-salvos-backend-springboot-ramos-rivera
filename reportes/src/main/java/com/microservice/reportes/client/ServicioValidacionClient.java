package com.microservice.reportes.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.microservice.reportes.exception.MascotaNoEncontradaException;
import com.microservice.reportes.exception.ServicioNoDisponibleException;
import com.microservice.reportes.exception.UsuarioNoEncontradoException;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

/**
 * Encapsula las llamadas HTTP del microservicio de reportes hacia los servicios
 * de mascotas y usuarios. Vive en un bean independiente (no en el Service) para
 * que el proxy AOP de Resilience4j intercepte las llamadas: la anotación
 * {@code @CircuitBreaker} NO funciona en auto-invocaciones dentro de la misma clase.
 *
 * Cada método está protegido por su propia instancia de Circuit Breaker. El
 * circuito se ABRE tras 5 errores de CONEXIÓN consecutivos; los errores de
 * negocio (404) o de estado HTTP no abren el circuito y se re-lanzan tal cual.
 */
@Component
public class ServicioValidacionClient {

    private static final Logger logger = LoggerFactory.getLogger(ServicioValidacionClient.class);

    private final WebClient.Builder webClientBuilder;

    @Value("${mascotas.service.url}")
    private String mascotasServiceUrl;

    @Value("${usuario.service.urls}")
    private String usuariosServiceUrl;

    public ServicioValidacionClient(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    // ───────────────────────────── Mascotas ─────────────────────────────

    @CircuitBreaker(name = "mascotasService", fallbackMethod = "validarMascotaFallback")
    public void validarMascotaExiste(Long mascotaId) {
        logger.info("Validando existencia de mascota con id: {}", mascotaId);
        try {
            webClientBuilder.build()
                .get()
                .uri(mascotasServiceUrl + "/api/mascotas/" + mascotaId)
                .retrieve()
                .bodyToMono(Object.class)
                .block();
            logger.info("Mascota encontrada correctamente");
        } catch (WebClientResponseException.NotFound e) {
            logger.warn("Mascota no encontrada: {}", mascotaId);
            throw new MascotaNoEncontradaException("La mascota con id " + mascotaId + " no existe");
        }
    }

    @SuppressWarnings("java:S1172")
    public void validarMascotaFallback(Long mascotaId, Throwable t) {
        // Errores de negocio / estado HTTP: la conexión funcionó, se re-lanzan.
        if (t instanceof MascotaNoEncontradaException) {
            throw (MascotaNoEncontradaException) t;
        }
        if (t instanceof WebClientResponseException) {
            throw new RuntimeException("Error al consultar el servicio de mascotas: " + t.getMessage());
        }
        // Fallo de conexión o Circuit Breaker ABIERTO: se falla rápido con 503.
        logger.error("Circuit breaker 'mascotasService' activo o servicio no disponible: {}", t.toString());
        throw new ServicioNoDisponibleException(
            "El servicio de mascotas no está disponible en este momento. Intenta más tarde.", t);
    }

    // ───────────────────────────── Usuarios ─────────────────────────────

    @CircuitBreaker(name = "usuarioService", fallbackMethod = "validarUsuarioFallback")
    public void validarUsuarioExiste(Long usuarioId) {
        logger.info("Validando existencia de usuario con id: {}", usuarioId);
        try {
            webClientBuilder.build()
                .get()
                .uri(usuariosServiceUrl + "/api/usuario/" + usuarioId)
                .retrieve()
                .bodyToMono(Object.class)
                .block();
            logger.info("Usuario encontrado correctamente");
        } catch (WebClientResponseException.NotFound e) {
            logger.warn("Usuario no encontrado: {}", usuarioId);
            throw new UsuarioNoEncontradoException("El usuario con id " + usuarioId + " no existe");
        }
    }

    @SuppressWarnings("java:S1172")
    public void validarUsuarioFallback(Long usuarioId, Throwable t) {
        if (t instanceof UsuarioNoEncontradoException) {
            throw (UsuarioNoEncontradoException) t;
        }
        if (t instanceof WebClientResponseException) {
            throw new RuntimeException("Error al consultar el servicio de usuarios: " + t.getMessage());
        }
        logger.error("Circuit breaker 'usuarioService' activo o servicio no disponible: {}", t.toString());
        throw new ServicioNoDisponibleException(
            "El servicio de usuarios no está disponible en este momento. Intenta más tarde.", t);
    }
}
