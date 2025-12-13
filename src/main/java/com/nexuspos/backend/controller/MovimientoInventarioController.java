package com.nexuspos.backend.controller;

import com.nexuspos.backend.model.MovimientoInventario;
import com.nexuspos.backend.model.TipoMovimiento;
import com.nexuspos.backend.service.MovimientoInventarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CONTROLADOR MOVIMIENTO INVENTARIO
 *
 * Endpoints REST para gestión de movimientos de inventario
 */
@RestController
@RequestMapping("/api/movimientos")
public class MovimientoInventarioController {

    @Autowired
    private MovimientoInventarioService movimientoService;

    /**
     * GET /api/movimientos - Obtener todos los movimientos
     */
    @GetMapping
    public ResponseEntity<List<MovimientoInventario>> getAll() {
        return ResponseEntity.ok(movimientoService.findAll());
    }

    /**
     * GET /api/movimientos/ultimos - Últimos 10 movimientos
     */
    @GetMapping("/ultimos")
    public ResponseEntity<List<MovimientoInventario>> getUltimos() {
        return ResponseEntity.ok(movimientoService.getUltimosMovimientos());
    }

    /**
     * GET /api/movimientos/producto/{id} - Movimientos de un producto
     */
    @GetMapping("/producto/{id}")
    public ResponseEntity<List<MovimientoInventario>> getByProducto(@PathVariable Long id) {
        return ResponseEntity.ok(movimientoService.findByProducto(id));
    }

    /**
     * GET /api/movimientos/tipo/{tipo} - Movimientos por tipo
     */
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<MovimientoInventario>> getByTipo(@PathVariable TipoMovimiento tipo) {
        return ResponseEntity.ok(movimientoService.findByTipo(tipo));
    }

    /**
     * GET /api/movimientos/rango?inicio=...&fin=... - Movimientos en rango
     */
    @GetMapping("/rango")
    public ResponseEntity<List<MovimientoInventario>> getByRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin
    ) {
        return ResponseEntity.ok(movimientoService.findByRango(inicio, fin));
    }

    /**
     * POST /api/movimientos/entrada - Registrar entrada de inventario
     */
    @PostMapping("/entrada")
    public ResponseEntity<?> registrarEntrada(@RequestBody Map<String, Object> datos) {
        try {
            Long productoId = Long.valueOf(datos.get("productoId").toString());
            Integer cantidad = Integer.valueOf(datos.get("cantidad").toString());
            Double costoUnitario = datos.get("costoUnitario") != null ?
                    Double.valueOf(datos.get("costoUnitario").toString()) : null;
            String motivo = datos.get("motivo") != null ? datos.get("motivo").toString() : null;
            String usuario = datos.get("usuario") != null ? datos.get("usuario").toString() : null;

            MovimientoInventario movimiento = movimientoService.registrarEntrada(
                    productoId, cantidad, costoUnitario, motivo, usuario
            );

            return ResponseEntity.ok(movimiento);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/movimientos/salida - Registrar salida de inventario
     */
    @PostMapping("/salida")
    public ResponseEntity<?> registrarSalida(@RequestBody Map<String, Object> datos) {
        try {
            Long productoId = Long.valueOf(datos.get("productoId").toString());
            Integer cantidad = Integer.valueOf(datos.get("cantidad").toString());
            String motivo = datos.get("motivo") != null ? datos.get("motivo").toString() : null;
            String usuario = datos.get("usuario") != null ? datos.get("usuario").toString() : null;

            MovimientoInventario movimiento = movimientoService.registrarSalida(
                    productoId, cantidad, motivo, usuario
            );

            return ResponseEntity.ok(movimiento);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/movimientos/ajuste - Registrar ajuste de inventario
     */
    @PostMapping("/ajuste")
    public ResponseEntity<?> registrarAjuste(@RequestBody Map<String, Object> datos) {
        try {
            Long productoId = Long.valueOf(datos.get("productoId").toString());
            Integer cantidad = Integer.valueOf(datos.get("cantidad").toString());
            Boolean esPositivo = Boolean.valueOf(datos.get("esPositivo").toString());
            String motivo = datos.get("motivo") != null ? datos.get("motivo").toString() : null;
            String usuario = datos.get("usuario") != null ? datos.get("usuario").toString() : null;

            MovimientoInventario movimiento = movimientoService.registrarAjuste(
                    productoId, cantidad, esPositivo, motivo, usuario
            );

            return ResponseEntity.ok(movimiento);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/movimientos/stats - Estadísticas de movimientos
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("entradas", movimientoService.contarPorTipo(TipoMovimiento.ENTRADA));
        stats.put("salidas", movimientoService.contarPorTipo(TipoMovimiento.SALIDA));
        stats.put("ajustesPositivos", movimientoService.contarPorTipo(TipoMovimiento.AJUSTE_POSITIVO));
        stats.put("ajustesNegativos", movimientoService.contarPorTipo(TipoMovimiento.AJUSTE_NEGATIVO));
        stats.put("devoluciones", movimientoService.contarPorTipo(TipoMovimiento.DEVOLUCION));
        return ResponseEntity.ok(stats);
    }
}
