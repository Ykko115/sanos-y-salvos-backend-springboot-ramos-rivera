package mascotas.microservice.mascotas.controller;

import mascotas.microservice.mascotas.entity.Mascotas;
import mascotas.microservice.mascotas.service.MascotasService;
import mascotas.microservice.mascotas.dto.UsuarioDTO;
import org.springframework.beans.factory.annotation.Value;
import java.util.Arrays;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/mascotas")
public class MascotasController {

    @Autowired
    private MascotasService mascotasService;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Value("${usuario.service.urls:http://usuario:8081}")
    private String usuarioServiceUrls;

    @Value("${internal.jwt:}")
    private String internalJwt;

    // ✅ Método extraído correctamente fuera de cualquier lambda
    private UsuarioDTO obtenerUsuarioDesdeCualquierUrl(Long usuarioId) {
        for (String baseUrl : Arrays.asList(usuarioServiceUrls.split(","))) {
            try {
                WebClient.RequestHeadersSpec<?> request = webClientBuilder.build()
                        .get()
                        .uri(baseUrl.trim() + "/api/usuario/" + usuarioId + "?plain=true");
                if (internalJwt != null && !internalJwt.isBlank()) {
                    request = request.header(HttpHeaders.AUTHORIZATION, internalJwt);
                }
                return request.retrieve()
                        .bodyToMono(UsuarioDTO.class)
                        .block();
            } catch (Exception ignored) {}
        }
        return null;
    }

    @PostMapping
    public ResponseEntity<Mascotas> crearMascota(@RequestBody Mascotas mascota) {
        try {
            Mascotas mascotaCreada = mascotasService.crearMascota(mascota);
            return new ResponseEntity<>(mascotaCreada, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerMascotaPorId(@PathVariable Long id) {
        Optional<Mascotas> mascotaOpt = mascotasService.obtenerMascotaPorId(id);
        if (mascotaOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Mascotas mascota = mascotaOpt.get();
        UsuarioDTO usuario = null;
        if (mascota.getUsuarioId() != null) {
            usuario = obtenerUsuarioDesdeCualquierUrl(mascota.getUsuarioId());
        }
        return ResponseEntity.ok(new MascotaConUsuarioResponse(mascota, usuario));
    }

    @GetMapping
    public ResponseEntity<?> obtenerTodasLasMascotas() {
        try {
            List<Mascotas> mascotas = mascotasService.obtenerTodasLasMascotas();
            // ✅ Lambda limpia, sin método adentro
            List<MascotaConUsuarioResponse> result = mascotas.stream().map(mascota -> {
                UsuarioDTO usuario = null;
                if (mascota.getUsuarioId() != null) {
                    usuario = obtenerUsuarioDesdeCualquierUrl(mascota.getUsuarioId());
                }
                return new MascotaConUsuarioResponse(mascota, usuario);
            }).toList();
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // DTO de respuesta enriquecida
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

    @PutMapping("/{id}")
    public ResponseEntity<Mascotas> actualizarMascota(@PathVariable Long id, @RequestBody Mascotas mascota) {
        try {
            Mascotas mascotaActualizada = mascotasService.actualizarMascota(id, mascota);
            return new ResponseEntity<>(mascotaActualizada, HttpStatus.OK);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> eliminarMascota(@PathVariable Long id) {
        try {
            mascotasService.eliminarMascota(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<Mascotas> obtenerMascotaPorNombre(@PathVariable String nombre) {
        Optional<Mascotas> mascota = mascotasService.obtenerMascotaPorNombre(nombre);
        return mascota.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/especie/{especie}")
    public ResponseEntity<List<Mascotas>> obtenerMascotasPorEspecie(@PathVariable String especie) {
        try {
            List<Mascotas> mascotas = mascotasService.obtenerMascotasPorEspecie(especie);
            return new ResponseEntity<>(mascotas, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/raza/{raza}")
    public ResponseEntity<List<Mascotas>> obtenerMascotasPorRaza(@PathVariable String raza) {
        try {
            List<Mascotas> mascotas = mascotasService.obtenerMascotasPorRaza(raza);
            return new ResponseEntity<>(mascotas, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Mascotas>> obtenerMascotasPorUsuarioId(@PathVariable Long usuarioId) {
        try {
            List<Mascotas> mascotas = mascotasService.obtenerMascotasPorUsuarioId(usuarioId);
            return new ResponseEntity<>(mascotas, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}