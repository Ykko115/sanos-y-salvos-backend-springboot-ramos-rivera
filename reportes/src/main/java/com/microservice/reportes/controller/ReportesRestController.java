package com.microservice.reportes.controller;

import com.microservice.reportes.entity.Reportes;
import com.microservice.reportes.service.ReportesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reportes")
public class ReportesRestController {

    @Autowired
    private ReportesService reporteService;

    @PostMapping
    public ResponseEntity<Reportes> crearReporte(@RequestBody Reportes reportes) {
        try{
            Reportes reporteCreado = reporteService.creaReporte(reportes);
            return new ResponseEntity<>(reporteCreado, HttpStatus.CREATED);
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
    
}
