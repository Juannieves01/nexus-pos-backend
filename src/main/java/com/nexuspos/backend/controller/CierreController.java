package com.nexuspos.backend.controller;

import com.nexuspos.backend.model.Cierre;
import com.nexuspos.backend.service.CierreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * CONTROLLER DE CIERRES - API REST
 */
@RestController
@RequestMapping("/api/cierres")
@RequiredArgsConstructor
@Slf4j
public class CierreController {

    private final CierreService cierreService;

    /**
     * GET /api/cierres
     * Obtiene todos los cierres
     */
    @GetMapping
    public ResponseEntity<List<Cierre>> getAllCierres() {
        log.info("GET /api/cierres");
        List<Cierre> cierres = cierreService.findAll();
        return ResponseEntity.ok(cierres);
    }

    /**
     * GET /api/cierres/{id}
     * Obtiene un cierre por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getCierreById(@PathVariable Long id) {
        log.info("GET /api/cierres/{}", id);

        return cierreService.findById(id)
            .map(cierre -> ResponseEntity.ok((Object) cierre))
            .orElseGet(() -> ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body((Object) Map.of("error", "Cierre no encontrado")));
    }

    /**
     * GET /api/cierres/rango?inicio=2024-01-01&fin=2024-01-31
     * Obtiene cierres por rango de fechas
     */
    @GetMapping("/rango")
    public ResponseEntity<List<Cierre>> getCierresByRango(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin
    ) {
        log.info("GET /api/cierres/rango?inicio={}&fin={}", inicio, fin);

        LocalDateTime inicioDateTime = inicio.atStartOfDay();
        LocalDateTime finDateTime = fin.atTime(LocalTime.MAX);

        List<Cierre> cierres = cierreService.findByRango(inicioDateTime, finDateTime);
        return ResponseEntity.ok(cierres);
    }

    /**
     * GET /api/cierres/ultimo
     * Obtiene el último cierre registrado
     */
    @GetMapping("/ultimo")
    public ResponseEntity<?> getUltimoCierre() {
        log.info("GET /api/cierres/ultimo");

        return cierreService.getUltimoCierre()
            .map(cierre -> ResponseEntity.ok((Object) cierre))
            .orElseGet(() -> ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body((Object) Map.of("error", "No hay cierres registrados")));
    }

    /**
     * GET /api/cierres/count
     * Cuenta total de cierres
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> countCierres() {
        log.info("GET /api/cierres/count");
        long total = cierreService.count();
        return ResponseEntity.ok(Map.of("total", total));
    }

    /**
     * GET /api/cierres/totales
     * Obtiene totales acumulados de todos los cierres
     */
    @GetMapping("/totales")
    public ResponseEntity<Map<String, Double>> getTotalesAcumulados() {
        log.info("GET /api/cierres/totales");

        Double totalVentas = cierreService.calcularTotalVentasAcumuladas();

        return ResponseEntity.ok(Map.of(
            "totalVentasAcumuladas", totalVentas
        ));
    }

    /**
     * DELETE /api/cierres/{id}
     * Elimina un cierre (solo para correcciones)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteCierre(@PathVariable Long id) {
        log.info("DELETE /api/cierres/{}", id);

        try {
            cierreService.delete(id);
            return ResponseEntity.ok(Map.of("message", "Cierre eliminado correctamente"));
        } catch (Exception e) {
            log.error("Error al eliminar cierre: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al eliminar cierre: " + e.getMessage()));
        }
    }
}
