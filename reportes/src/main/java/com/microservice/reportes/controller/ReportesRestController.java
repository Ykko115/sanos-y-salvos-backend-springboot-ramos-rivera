 package com.microservice.reportes.controller;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@RestController
@RequestMapping("/api/reportes")
public class ReportesRestController {

    @Autowired
    private ReportesService reporteService;

    @Value("${cloudflare.r2.access-key}")
    private String r2AccessKey;

    @Value("${cloudflare.r2.secret-key}")
    private String r2SecretKey;

    @Value("${cloudflare.r2.bucket}")
    private String r2Bucket;

    @Value("${cloudflare.r2.endpoint}")
    private String r2Endpoint;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Value("${mascotas.service.url}")
    private String mascotasServiceUrl;

    @Value("${usuario.service.urls}")
    private String usuariosServiceUrl;

    @PostMapping
    public ResponseEntity<Reportes> crearReporte(@RequestBody Reportes reportes) {
        try {
            Reportes reporteCreado = reporteService.creaReporte(reportes);
            return new ResponseEntity<>(reporteCreado, HttpStatus.CREATED);
        } catch (MascotaNoEncontradaException | UsuarioNoEncontradoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
    public ResponseEntity<Reportes> actualizarReporte(@PathVariable Long id, @RequestBody Reportes reporteActualizado){
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


    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No file uploaded"));
        }
        try {
            AwsBasicCredentials creds = AwsBasicCredentials.create(r2AccessKey, r2SecretKey);
            S3Client s3 = S3Client.builder()
                    .endpointOverride(URI.create(r2Endpoint))
                    .region(Region.US_EAST_1)
                    .credentialsProvider(StaticCredentialsProvider.create(creds))
                    .build();

            String key = "imagenes/" + file.getOriginalFilename();

                s3.putObject(
                    PutObjectRequest.builder()
                        .bucket(r2Bucket)
                        .key(key)
                        .contentType(file.getContentType())
                        .build(),
                    software.amazon.awssdk.core.sync.RequestBody.fromBytes(file.getBytes())
                );

                // Usar la URL pública del bucket R2
                String publicUrl = "https://pub-daffd7e8a85c4df4a96cdc1e8f6b61e8.r2.dev/" + key;
                return ResponseEntity.ok(java.util.Map.of("url", publicUrl));
        } catch (S3Exception e) {
            return ResponseEntity.status(500).body(java.util.Map.of("error", "R2 error", "details", e.awsErrorDetails().errorMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(java.util.Map.of("error", "Internal server error", "details", e.getMessage()));
        }
    }

    @GetMapping("/detalle/{id}")
    public ResponseEntity<?> obtenerReporteDetalle(@PathVariable Long id) {
        Optional<Reportes> reporteOpt = reporteService.obtenerReportePorId(id);
        if (reporteOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Reporte no encontrado"));
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

}
