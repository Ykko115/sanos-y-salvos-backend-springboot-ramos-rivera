package com.microservice.reportes.repository;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.TestPropertySource;

import com.microservice.reportes.entity.Reportes;
import com.microservice.reportes.entity.Reportes.Estado;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application.properties")
class ReportesRepositoryTest {

    @Autowired
    private ReportesRepository reportesRepository;

    private Reportes reporte1;
    private Reportes reporte2;

    @BeforeEach
    void setUp() {
        reportesRepository.deleteAll();

        reporte1 = new Reportes();
        reporte1.setNombre_mascota("Luna");
        reporte1.setUsuarioId(1L);
        reporte1.setMascotaId(10L);
        reporte1.setEstado(Estado.PERDIDO);
        reporte1.setFechaReporte(LocalDate.now());
        reporte1.setUbicacion(new Reportes.Ubicacion(-33.4, -70.6));

        reporte2 = new Reportes();
        reporte2.setNombre_mascota("Max");
        reporte2.setUsuarioId(2L);
        reporte2.setMascotaId(20L);
        reporte2.setEstado(Estado.ENCONTRADO);
        reporte2.setFechaReporte(LocalDate.now());
        reporte2.setUbicacion(new Reportes.Ubicacion(-33.5, -70.7));

        reportesRepository.save(reporte1);
        reportesRepository.save(reporte2);
    }

    @Test
    void findByEstado_debeFiltrarCorrectamente() {
        List<Reportes> perdidos = reportesRepository.findByEstado(Estado.PERDIDO);
        assertThat(perdidos).hasSize(1);
        assertThat(perdidos.get(0).getNombre_mascota()).isEqualTo("Luna");
    }

    @Test
    void findByUsuarioId_debeFiltrarCorrectamente() {
        List<Reportes> reportes = reportesRepository.findByUsuarioId(1L);
        assertThat(reportes).hasSize(1);
        assertThat(reportes.get(0).getNombre_mascota()).isEqualTo("Luna");
    }

    @Test
    void findByMascotaId_debeFiltrarCorrectamente() {
        List<Reportes> reportes = reportesRepository.findByMascotaId(20L);
        assertThat(reportes).hasSize(1);
        assertThat(reportes.get(0).getNombre_mascota()).isEqualTo("Max");
    }

    @Test
    void save_debeGenerarId() {
        Reportes nuevo = new Reportes();
        nuevo.setNombre_mascota("Coco");
        nuevo.setEstado(Estado.PERDIDO);
        Reportes guardado = reportesRepository.save(nuevo);
        assertThat(guardado.getId()).isNotNull();
    }

    @Test
    void deleteById_debeEliminarCorrectamente() {
        Long id = reporte1.getId();
        reportesRepository.deleteById(id);
        assertThat(reportesRepository.findById(id)).isEmpty();
    }
}