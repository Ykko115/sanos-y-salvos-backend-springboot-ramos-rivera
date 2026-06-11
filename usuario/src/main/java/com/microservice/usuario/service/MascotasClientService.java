package com.microservice.usuario.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import com.microservice.usuario.entitie.dto.MascotaDTO;
import com.microservice.usuario.exception.ServicioNoDisponibleException;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Service
public class MascotasClientService {

    private static final Logger logger = LoggerFactory.getLogger(MascotasClientService.class);

    /** Nombre de la instancia de Circuit Breaker (ver application.properties). */
    private static final String CB_MASCOTAS = "mascotasService";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${mascotas.service.url:http://mascotas:8082}")
    private String mascotasServiceUrl;

    /**
     * Consulta las mascotas de un usuario en el microservicio "mascotas".
     *
     * Protegido por el Circuit Breaker "mascotasService": tras 5 errores de
     * conexión consecutivos el circuito se ABRE y las siguientes llamadas fallan
     * de inmediato vía {@link #obtenerMascotasPorUsuarioIdFallback}. Los errores
     * de conexión se propagan (ya no se silencian) para que el Circuit Breaker
     * pueda contabilizarlos.
     */
    @CircuitBreaker(name = CB_MASCOTAS, fallbackMethod = "obtenerMascotasPorUsuarioIdFallback")
    public List<MascotaDTO> obtenerMascotasPorUsuarioId(Long usuarioId) {
        String url = mascotasServiceUrl + "/api/mascotas/usuario/" + usuarioId;
        MascotaDTO[] mascotas = restTemplate.getForObject(url, MascotaDTO[].class);
        if (mascotas == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(mascotas);
    }

    /**
     * Método de respaldo invocado por Resilience4j cuando el circuito está
     * ABIERTO o hay un error de conexión con el servicio de mascotas.
     * Falla rápido con un 503; un error HTTP (4xx/5xx) sí se re-lanza tal cual.
     */
    public List<MascotaDTO> obtenerMascotasPorUsuarioIdFallback(Long usuarioId, Throwable t) {
        // Un error de estado HTTP significa que la conexión sí funcionó: se re-lanza.
        if (t instanceof RestClientResponseException) {
            throw (RestClientResponseException) t;
        }
        logger.error("Circuit breaker '{}' activo o fallo de conexión con el servicio "
            + "de mascotas: {}", CB_MASCOTAS, t.toString());
        throw new ServicioNoDisponibleException(
            "El servicio de mascotas no está disponible en este momento.", t);
    }
}
