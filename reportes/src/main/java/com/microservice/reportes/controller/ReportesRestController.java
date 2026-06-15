 package com.microservice.reportes.controller;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import com.microservice.reportes.dto.ReporteDetalleDTO;
import com.microservice.reportes.entity.Reportes;
import com.microservice.reportes.exception.MascotaNoEncontradaException;
import com.microservice.reportes.exception.UsuarioNoEncontradoException;
import com.microservice.reportes.service.ReportesService;
import com.microservice.reportes.dto.CrearReporteDTO;
import com.microservice.reportes.dto.ActualizarReporteDTO;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@RestController
@RequestMapping("/api/reportes")
public class ReportesRestController {

    private static final Logger logger = LoggerFactory.getLogger(ReportesRestController.class);
    private static final String ERROR_KEY = "error";

    private final ReportesService reporteService;
    private final WebClient.Builder webClientBuilder;

    @Value("${cloudflare.r2.access-key}")
    private String r2AccessKey;

    @Value("${cloudflare.r2.secret-key}")
    private String r2SecretKey;

    @Value("${cloudflare.r2.bucket}")
    private String r2Bucket;

    @Value("${cloudflare.r2.endpoint}")
    private String r2Endpoint;

    @Value("${mascotas.service.url}")
    private String mascotasServiceUrl;

    @Value("${usuario.service.urls}")
    private String usuariosServiceUrl;

    public ReportesRestController(ReportesService reporteService, WebClient.Builder webClientBuilder) {
        this.reporteService = reporteService;
        this.webClientBuilder = webClientBuilder;
    }

    @PostMapping
    public ResponseEntity<Reportes> crearReporte(@RequestBody CrearReporteDTO dto) {
        try {
            Reportes reportes = new Reportes();
            reportes.setUsuarioId(dto.getUsuarioId());
            reportes.setMascotaId(dto.getMascotaId());
            reportes.setDescripcion(dto.getDescripcion());
            reportes.setTelefono(dto.getTelefono());
            reportes.setUbicacion(dto.getUbicacion());
            reportes.setEstado(dto.getEstado());
            reportes.setFechaReporte(dto.getFechaReporte());

            // Vincular fotoUrl de la mascota al img del reporte si no viene en el DTO
            String img = dto.getImg();
            if ((img == null || img.isBlank()) && dto.getMascotaId() != null) {
                img = obtenerFotoUrlMascota(dto.getMascotaId());
            }
            reportes.setImg(img);

            Reportes reporteCreado = reporteService.creaReporte(reportes);
            return new ResponseEntity<>(reporteCreado, HttpStatus.CREATED);
        } catch (MascotaNoEncontradaException | UsuarioNoEncontradoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Error al crear reporte: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String obtenerFotoUrlMascota(Long mascotaId) {
        return "/api/mascotas/" + mascotaId + "/foto";
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reportes> obtenerReportePorId(@PathVariable Long id) {
        Optional<Reportes> reporte = reporteService.obtenerReportePorId(id);
        return reporte.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    @GetMapping
    public ResponseEntity<List<Reportes>> obtenerTodosLosReportes(){
        List<Reportes> reportes = reporteService.obtenerTodosLosReportes();
        return ResponseEntity.ok(reportes);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Reportes> actualizarReporte(@PathVariable Long id, @RequestBody ActualizarReporteDTO dto) {
        Reportes reporteActualizado = new Reportes();
        reporteActualizado.setUsuarioId(dto.getUsuarioId());
        reporteActualizado.setMascotaId(dto.getMascotaId());
        reporteActualizado.setDescripcion(dto.getDescripcion());
        reporteActualizado.setTelefono(dto.getTelefono());
        reporteActualizado.setUbicacion(dto.getUbicacion());
        reporteActualizado.setImg(dto.getImg());
        reporteActualizado.setEstado(dto.getEstado());
        Reportes reportes = reporteService.actualizarReporte(id, reporteActualizado);
        return ResponseEntity.ok(reportes);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> eliminarReporte(@PathVariable Long id){
       try {
            reporteService.eliminarReporte(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
       } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
       } catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
       }
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<Reportes>> obtenerReportesPorEstado(@PathVariable Reportes.Estado estado){
        try{
            List<Reportes> reportes = reporteService.obtenerReportesPorEstado(estado);
            return new ResponseEntity<>(reportes, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Reportes>> obtenerReportesPorUsuario(@PathVariable Long usuarioId){
        try {
            List<Reportes> reportes = reporteService.obtenerReportesPorUsuario(usuarioId);
            return new ResponseEntity<>(reportes, HttpStatus.OK);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/mascota/{mascotaId}")
    public ResponseEntity<List<Reportes>> obtenerReportesPorMascota(@PathVariable Long mascotaId){
        try {
            List<Reportes> reportes = reporteService.obtenerReportesPorMascota(mascotaId);
            return new ResponseEntity<>(reportes, HttpStatus.OK);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/mascota/{mascotaId}")
    public ResponseEntity<Map<String, Object>> eliminarReportesPorMascota(@PathVariable Long mascotaId) {
        try {
            reporteService.eliminarReportesPorMascotaId(mascotaId);
            return ResponseEntity.ok(Map.of("mensaje", "Reportes eliminados para mascotaId=" + mascotaId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(ERROR_KEY, e.getMessage()));
        }
    }


    @PostMapping("/upload-image")
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, "No file uploaded"));
        }
        try {
            AwsBasicCredentials creds = AwsBasicCredentials.create(r2AccessKey, r2SecretKey);
            S3Client s3 = S3Client.builder()
                    .endpointOverride(URI.create(r2Endpoint))
                    .region(Region.US_EAST_1)
                    .credentialsProvider(StaticCredentialsProvider.create(creds))
                    .build();

            String ext = "";
            String original = file.getOriginalFilename();
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf('.'));
            }
            String key = "imagenes/" + java.util.UUID.randomUUID() + ext;

                s3.putObject(
                    PutObjectRequest.builder()
                        .bucket(r2Bucket)
                        .key(key)
                        .contentType(file.getContentType())
                        .build(),
                    software.amazon.awssdk.core.sync.RequestBody.fromBytes(file.getBytes())
                );

                String publicUrl = "https://pub-daffd7e8a85c4df4a96cdc1e8f6b61e8.r2.dev/" + key;
                return ResponseEntity.ok(Map.of("url", publicUrl));
        } catch (S3Exception e) {
            return ResponseEntity.status(500).body(Map.of(ERROR_KEY, "R2 error", "details", e.awsErrorDetails().errorMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(ERROR_KEY, "Internal server error", "details", e.getMessage()));
        }
    }

    @GetMapping("/detalle/{id}")
    public ResponseEntity<Object> obtenerReporteDetalle(@PathVariable Long id) {
        Optional<Reportes> reporteOpt = reporteService.obtenerReportePorId(id);
        if (reporteOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(ERROR_KEY, "Reporte no encontrado"));
        }
        Reportes reporte = reporteOpt.get();
        Object usuario = null;
        Object mascota = null;
        // Consultar usuario si existe usuarioId
        if (reporte.getUsuarioId() != null) {
            try {
                usuario = webClientBuilder.build()
                        .get()
                        .uri(usuariosServiceUrl + "/api/usuario/" + reporte.getUsuarioId())
                        .retrieve()
                        .bodyToMono(Object.class)
                        .block();
            } catch (Exception e) {
                usuario = null;
            }
        }
        // Consultar mascota si existe mascotaId
        if (reporte.getMascotaId() != null) {
            try {
                mascota = webClientBuilder.build()
                        .get()
                        .uri(mascotasServiceUrl + "/api/mascotas/" + reporte.getMascotaId())
                        .retrieve()
                        .bodyToMono(Object.class)
                        .block();
            } catch (Exception e) {
                mascota = null;
            }
        }
        ReporteDetalleDTO dto = new ReporteDetalleDTO();
        dto.setReporte(reporte);
        dto.setUsuario(usuario);
        dto.setMascota(mascota);
        return ResponseEntity.ok(dto);
    }

    // Nota: la colección de detalles se obtiene usando el endpoint existente `/api/reportes`
    // y el endpoint `/detalle/{id}` devuelve el detalle de un reporte por id.

}
