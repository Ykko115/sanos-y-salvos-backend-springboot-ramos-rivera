package com.gateway.apigateway.controller;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Destino de los {@code fallbackUri} configurados en los filtros CircuitBreaker
 * del Gateway. Cuando el circuito de una ruta está ABIERTO (o el backend no
 * responde), la petición se reenvía aquí y se devuelve un 503 limpio en lugar
 * de propagar el error del backend caído.
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    private ResponseEntity<Map<String, Object>> serviceUnavailable(String servicio) {
        Map<String, Object> body = Map.of(
            "timestamp", Instant.now().toString(),
            "status", HttpStatus.SERVICE_UNAVAILABLE.value(),
            "error", "Service Unavailable",
            "service", servicio,
            "message", "El servicio '" + servicio + "' no está disponible en este momento "
                + "Por favor, intenta nuevamente más tarde."
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }

    @RequestMapping("/usuario")
    public ResponseEntity<Map<String, Object>> usuarioFallback() {
        return serviceUnavailable("usuario");
    }

    @RequestMapping("/mascotas")
    public ResponseEntity<Map<String, Object>> mascotasFallback() {
        return serviceUnavailable("mascotas");
    }

    @RequestMapping("/reportes")
    public ResponseEntity<Map<String, Object>> reportesFallback() {
        return serviceUnavailable("reportes");
    }
}
