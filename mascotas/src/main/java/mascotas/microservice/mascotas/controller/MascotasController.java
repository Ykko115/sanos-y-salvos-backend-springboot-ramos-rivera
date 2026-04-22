package mascotas.microservice.mascotas.controller;

import mascotas.microservice.mascotas.entity.Mascotas;
import mascotas.microservice.mascotas.service.MascotasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/mascotas")
public class MascotasController {

    @Autowired
    private MascotasService mascotasService;

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
    public ResponseEntity<Mascotas> obtenerMascotaPorId(@PathVariable Long id) {
        Optional<Mascotas> mascota = mascotasService.obtenerMascotaPorId(id);
        return mascota.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<List<Mascotas>> obtenerTodasLasMascotas() {
        try {
            List<Mascotas> mascotas = mascotasService.obtenerTodasLasMascotas();
            return new ResponseEntity<>(mascotas, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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

}
