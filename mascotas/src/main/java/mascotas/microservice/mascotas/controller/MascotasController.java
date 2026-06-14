package mascotas.microservice.mascotas.controller;

import mascotas.microservice.mascotas.dto.MascotaDTO;
import mascotas.microservice.mascotas.entity.Mascotas;
import mascotas.microservice.mascotas.entity.NotificacionMatch;
import mascotas.microservice.mascotas.service.MascotasService;
import mascotas.microservice.mascotas.service.NotificacionMatchService;
import mascotas.microservice.mascotas.dto.UsuarioDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/mascotas")
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:5174" })
public class MascotasController {

    @Autowired
    private MascotasService mascotasService;

    @Autowired
    private NotificacionMatchService notificacionMatchService;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Value("${usuario.service.urls:http://usuario:8081}")
    private String usuarioServiceUrls;

    @Value("${internal.jwt:}")
    private String internalJwt;

    private UsuarioDTO obtenerUsuarioDesdeCualquierUrl(Long usuarioId) {
        for (String baseUrl : Arrays.asList(usuarioServiceUrls.split(","))) {
            try {
                WebClient.RequestHeadersSpec<?> request = webClientBuilder.build()
                        .get()
                        .uri(baseUrl.trim() + "/api/usuario/" + usuarioId + "?plain=true");
                if (internalJwt != null && !internalJwt.isBlank()) {
                    request = request.header(HttpHeaders.AUTHORIZATION, internalJwt);
                }
                return request.retrieve().bodyToMono(UsuarioDTO.class).block();
            } catch (Exception ignored) {}
        }
        return null;
    }

    // ── GET /api/mascotas/enums ────────────────────────────────────
    @GetMapping("/enums")
    public ResponseEntity<Map<String, Object>> obtenerEnums() {
        return ResponseEntity.ok(Map.of(
            "especies",   Arrays.stream(Mascotas.Especie.values()).map(Enum::name).toList(),
            "estados",    Arrays.stream(Mascotas.Estado.values()).map(Enum::name).toList(),
            "colores",    Arrays.stream(Mascotas.Color.values()).map(Enum::name).toList(),
            "tamanos",    Arrays.stream(Mascotas.Tamano.values()).map(Enum::name).toList(),
            "pelajes",    Arrays.stream(Mascotas.Pelaje.values()).map(Enum::name).toList(),
            "rangosEdad", Arrays.stream(Mascotas.RangoEdad.values()).map(Enum::name).toList(),
            "senas",      Arrays.stream(Mascotas.Sena.values()).map(Enum::name).toList()
        ));
    }

    // ── POST /api/mascotas ─────────────────────────────────────────
    @PostMapping
    public ResponseEntity<Mascotas> crearMascota(@RequestBody MascotaDTO dto) {
        try {
            Mascotas creada = mascotasService.crearMascota(dto.toEntity());
            return new ResponseEntity<>(creada, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ── GET /api/mascotas ──────────────────────────────────────────
    @GetMapping
    public ResponseEntity<?> obtenerTodasLasMascotas(
            @RequestParam(required = false) String estado) {
        try {
            List<Mascotas> mascotas;
            if (estado != null && !estado.isBlank()) {
                Mascotas.Estado estadoEnum = Mascotas.Estado.valueOf(estado.toUpperCase());
                mascotas = mascotasService.obtenerMascotasPorEstado(estadoEnum);
            } else {
                mascotas = mascotasService.obtenerTodasLasMascotas();
            }
            List<MascotaConUsuarioResponse> result = mascotas.stream().map(m -> {
                UsuarioDTO usuario = m.getUsuarioId() != null
                    ? obtenerUsuarioDesdeCualquierUrl(m.getUsuarioId()) : null;
                return new MascotaConUsuarioResponse(m, usuario);
            }).toList();
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Estado inválido: " + estado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ── GET /api/mascotas/{id} ─────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerMascotaPorId(@PathVariable Long id) {
        Optional<Mascotas> mascotaOpt = mascotasService.obtenerMascotaPorId(id);
        if (mascotaOpt.isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        Mascotas mascota = mascotaOpt.get();
        UsuarioDTO usuario = mascota.getUsuarioId() != null
            ? obtenerUsuarioDesdeCualquierUrl(mascota.getUsuarioId()) : null;
        return ResponseEntity.ok(new MascotaConUsuarioResponse(mascota, usuario));
    }

    // ── GET /api/mascotas/usuario/{usuarioId} ──────────────────────
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Mascotas>> obtenerMascotasPorUsuarioId(@PathVariable Long usuarioId) {
        try {
            return new ResponseEntity<>(mascotasService.obtenerMascotasPorUsuarioId(usuarioId), HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ── PUT /api/mascotas/{id} ─────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<Mascotas> actualizarMascota(@PathVariable Long id, @RequestBody MascotaDTO dto) {
        try {
            Mascotas actualizada = mascotasService.actualizarMascota(id, dto.toEntity());
            return new ResponseEntity<>(actualizada, HttpStatus.OK);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ── PUT /api/mascotas/{id}/estado ──────────────────────────────
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            String estadoStr = body.get("estado");
            if (estadoStr == null) return ResponseEntity.badRequest().body("Falta el campo 'estado'");
            Mascotas.Estado estado = Mascotas.Estado.valueOf(estadoStr.toUpperCase());
            Mascotas actualizada = mascotasService.actualizarEstado(id, estado);
            return ResponseEntity.ok(actualizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Estado inválido: " + body.get("estado"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // ── DELETE /api/mascotas/{id} ──────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMascota(@PathVariable Long id) {
        try {
            mascotasService.eliminarMascota(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // ── Notificaciones de coincidencias por usuario ────────────────
    @GetMapping("/notificaciones/{usuarioId}")
    public ResponseEntity<List<NotificacionMatch>> obtenerNotificaciones(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(notificacionMatchService.obtenerPorUsuario(usuarioId));
    }

    @PutMapping("/notificaciones/{id}/leida")
    public ResponseEntity<Void> marcarNotificacionLeida(@PathVariable Long id) {
        notificacionMatchService.marcarLeida(id);
        return ResponseEntity.ok().build();
    }

    // ── Endpoints heredados ────────────────────────────────────────
    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<Mascotas> obtenerMascotaPorNombre(@PathVariable String nombre) {
        Optional<Mascotas> mascota = mascotasService.obtenerMascotaPorNombre(nombre);
        return mascota.map(v -> new ResponseEntity<>(v, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/especie/{especie}")
    public ResponseEntity<List<Mascotas>> obtenerMascotasPorEspecie(@PathVariable String especie) {
        try {
            return new ResponseEntity<>(mascotasService.obtenerMascotasPorEspecie(especie), HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/raza/{raza}")
    public ResponseEntity<List<Mascotas>> obtenerMascotasPorRaza(@PathVariable String raza) {
        try {
            return new ResponseEntity<>(mascotasService.obtenerMascotasPorRaza(raza), HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ── DTO de respuesta enriquecida ───────────────────────────────
    public static class MascotaConUsuarioResponse {
        private final Mascotas mascota;
        private final UsuarioDTO usuario;

        public MascotaConUsuarioResponse(Mascotas mascota, UsuarioDTO usuario) {
            this.mascota = mascota;
            this.usuario = usuario;
        }

        public Mascotas getMascota() { return mascota; }
        public UsuarioDTO getUsuario() { return usuario; }
    }
}
