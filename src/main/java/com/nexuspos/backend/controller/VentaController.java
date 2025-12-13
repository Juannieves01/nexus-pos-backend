package com.nexuspos.backend.controller;

import com.nexuspos.backend.model.Venta;
import com.nexuspos.backend.service.VentaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * CONTROLLER DE VENTA - API REST
 */
@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
@Slf4j
public class VentaController {

    private final VentaService ventaService;

    /**
     * POST /api/ventas/cerrar-mesa
     * Cierra una mesa y crea la venta
     *
     * Body:
     * {
     *   "mesaId": 1,
     *   "efectivo": 50000,
     *   "transferencias": 0,
     *   "cajaId": 1  (opcional, usa la primera caja abierta si no se especifica)
     * }
     */
    @PostMapping("/cerrar-mesa")
    public ResponseEntity<?> cerrarMesaConVenta(@RequestBody Map<String, Object> body) {
        Long mesaId = Long.valueOf(body.get("mesaId").toString());
        Double efectivo = Double.valueOf(body.getOrDefault("efectivo", 0).toString());
        Double transferencias = Double.valueOf(body.getOrDefault("transferencias", 0).toString());
        Long cajaId = body.get("cajaId") != null ? Long.valueOf(body.get("cajaId").toString()) : null;

        log.info("POST /api/ventas/cerrar-mesa - Mesa: {}, Efectivo: {}, Transferencias: {}, Caja: {}",
            mesaId, efectivo, transferencias, cajaId);

        try {
            Venta venta = ventaService.cerrarMesaConVenta(mesaId, efectivo, transferencias, cajaId);
            return ResponseEntity.status(HttpStatus.CREATED).body(venta);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Error: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/ventas
     * Obtiene todas las ventas
     */
    @GetMapping
    public ResponseEntity<List<Venta>> getAllVentas() {
        log.info("GET /api/ventas");
        List<Venta> ventas = ventaService.findAll();
        return ResponseEntity.ok(ventas);
    }

    /**
     * GET /api/ventas/{id}
     * Obtiene una venta por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getVentaById(@PathVariable Long id) {
        log.info("GET /api/ventas/{}", id);

        return ventaService.findById(id)
            .map(venta -> ResponseEntity.ok((Object) venta))
            .orElseGet(() -> ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body((Object) "Venta no encontrada"));
    }

    /**
     * GET /api/ventas/totales
     * Obtiene los totales de ventas
     */
    @GetMapping("/totales")
    public ResponseEntity<Map<String, Double>> getTotales() {
        log.info("GET /api/ventas/totales");

        Double total = ventaService.calcularTotalVentas();
        Double efectivo = ventaService.calcularTotalEfectivo();
        Double transferencias = ventaService.calcularTotalTransferencias();

        return ResponseEntity.ok(Map.of(
            "total", total,
            "efectivo", efectivo,
            "transferencias", transferencias
        ));
    }

    /**
     * GET /api/ventas/count
     * Cuenta total de ventas
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> countVentas() {
        log.info("GET /api/ventas/count");
        long total = ventaService.count();
        return ResponseEntity.ok(Map.of("total", total));
    }

    /**
     * GET /api/ventas/estadisticas/diarias?dias=7
     * Obtiene estadísticas de ventas de los últimos N días
     * Por defecto: últimos 7 días
     */
    @GetMapping("/estadisticas/diarias")
    public ResponseEntity<Map<String, Object>> getEstadisticasDiarias(
        @RequestParam(defaultValue = "7") int dias
    ) {
        log.info("GET /api/ventas/estadisticas/diarias?dias={}", dias);

        try {
            Map<String, Object> estadisticas = ventaService.obtenerEstadisticasDiarias(dias);
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            log.error("Error al obtener estadísticas diarias: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al calcular estadísticas: " + e.getMessage()));
        }
    }

    /**
     * GET /api/ventas/productos-mas-vendidos?limite=10
     * Obtiene los productos más vendidos
     */
    @GetMapping("/productos-mas-vendidos")
    public ResponseEntity<?> getProductosMasVendidos(
        @RequestParam(defaultValue = "10") int limite
    ) {
        log.info("GET /api/ventas/productos-mas-vendidos?limite={}", limite);

        try {
            List<Map<String, Object>> productos = ventaService.obtenerProductosMasVendidos(limite);
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            log.error("Error al obtener productos más vendidos: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al obtener productos: " + e.getMessage()));
        }
    }

    /**
     * GET /api/ventas/por-categoria
     * Obtiene ventas agrupadas por categoría
     */
    @GetMapping("/por-categoria")
    public ResponseEntity<?> getVentasPorCategoria() {
        log.info("GET /api/ventas/por-categoria");

        try {
            Map<String, Map<String, Object>> categorias = ventaService.obtenerVentasPorCategoria();
            return ResponseEntity.ok(categorias);
        } catch (Exception e) {
            log.error("Error al obtener ventas por categoría: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al obtener estadísticas: " + e.getMessage()));
        }
    }

    /**
     * GET /api/ventas/dashboard
     * Obtiene resumen completo para el dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> getResumenDashboard() {
        log.info("GET /api/ventas/dashboard");

        try {
            Map<String, Object> resumen = ventaService.obtenerResumenDashboard();
            return ResponseEntity.ok(resumen);
        } catch (Exception e) {
            log.error("Error al obtener resumen del dashboard: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al obtener resumen: " + e.getMessage()));
        }
    }
}
