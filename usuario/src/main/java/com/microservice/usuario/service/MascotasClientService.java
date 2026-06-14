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

    private static final String CB_MASCOTAS = "mascotasService";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${mascotas.service.url:http://mascotas:8082}")
    private String mascotasServiceUrl;

    @CircuitBreaker(name = CB_MASCOTAS, fallbackMethod = "obtenerMascotasPorUsuarioIdFallback")
    public List<MascotaDTO> obtenerMascotasPorUsuarioId(Long usuarioId) {
        String url = mascotasServiceUrl + "/api/mascotas/usuario/" + usuarioId;
        MascotaDTO[] mascotas = restTemplate.getForObject(url, MascotaDTO[].class);
        if (mascotas == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(mascotas);
    }

    
    @SuppressWarnings("java:S1172")
    public List<MascotaDTO> obtenerMascotasPorUsuarioIdFallback(Long usuarioId, Throwable t) {
        if (t instanceof RestClientResponseException) {
            throw (RestClientResponseException) t;
        }
        logger.error("Circuit breaker '{}' activo o fallo de conexión con el servicio de mascotas: {}",
            CB_MASCOTAS, t.getMessage());
        throw new ServicioNoDisponibleException(
            "El servicio de mascotas no está disponible en este momento.", t);
    }
}
