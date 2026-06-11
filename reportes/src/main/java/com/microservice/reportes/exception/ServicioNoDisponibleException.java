package com.microservice.reportes.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Se lanza cuando un servicio externo no está disponible, ya sea por fallos de
 * conexión o porque el Circuit Breaker de Resilience4j se encuentra ABIERTO
 * tras 5 errores de conexión consecutivos. Se traduce a un HTTP 503.
 */
@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class ServicioNoDisponibleException extends RuntimeException {

    public ServicioNoDisponibleException(String mensaje) {
        super(mensaje);
    }

    public ServicioNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
