package com.nexuspos.backend.controller;

import com.nexuspos.backend.model.Caja;
import com.nexuspos.backend.model.Turno;
import com.nexuspos.backend.service.CajaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CONTROLLER DE CAJAS - API REST
 *
 * Maneja múltiples cajas y turnos
 */
@RestController
@RequestMapping("/api/cajas")
@RequiredArgsConstructor
@Slf4j
public class CajaController {

    private final CajaService cajaService;

    /**
     * GET /api/cajas
     * Obtiene todas las cajas (abiertas y cerradas)
     */
    @GetMapping
    public ResponseEntity<List<Caja>> getAll() {
        log.info("GET /api/cajas");
        return ResponseEntity.ok(cajaService.getAllCajas());
    }

    /**
     * GET /api/cajas/abiertas
     * Obtiene solo las cajas abiertas
     */
    @GetMapping("/abiertas")
    public ResponseEntity<List<Caja>> getCajasAbiertas() {
        log.info("GET /api/cajas/abiertas");
        return ResponseEntity.ok(cajaService.getCajasAbiertas());
    }

    /**
     * GET /api/cajas/{id}
     * Obtiene una caja específica por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getCajaById(@PathVariable Long id) {
        log.info("GET /api/cajas/{}", id);

        return cajaService.getCajaById(id)
            .map(caja -> ResponseEntity.ok((Object) caja))
            .orElseGet(() -> ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body((Object) Map.of("error", "Caja no encontrada")));
    }

    /**
     * GET /api/cajas/numero/{numero}/abierta
     * Obtiene la caja abierta de un número específico
     */
    @GetMapping("/numero/{numero}/abierta")
    public ResponseEntity<?> getCajaAbiertaPorNumero(@PathVariable Integer numero) {
        log.info("GET /api/cajas/numero/{}/abierta", numero);

        return cajaService.getCajaAbiertaPorNumero(numero)
            .map(caja -> ResponseEntity.ok((Object) caja))
            .orElseGet(() -> ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body((Object) Map.of("error", "No hay caja abierta con ese número")));
    }

    /**
     * POST /api/cajas/abrir
     * Abre una nueva caja
     *
     * Body:
     * {
     *   "numeroCaja": 1,
     *   "turno": "MAÑANA",
     *   "montoInicial": 100000,
     *   "usuario": "admin"
     * }
     */
    @PostMapping("/abrir")
    public ResponseEntity<?> abrirCaja(@RequestBody Map<String, Object> body) {
        Integer numeroCaja = (Integer) body.get("numeroCaja");
        String turnoStr = (String) body.get("turno");
        Double montoInicial = body.get("montoInicial") != null ?
                ((Number) body.get("montoInicial")).doubleValue() : 0.0;
        String usuario = (String) body.get("usuario");

        log.info("POST /api/cajas/abrir - Caja: {}, Turno: {}, Usuario: {}",
                numeroCaja, turnoStr, usuario);

        try {
            Turno turno = turnoStr != null ? Turno.valueOf(turnoStr.toUpperCase()) : null;
            Caja caja = cajaService.abrirCaja(numeroCaja, turno, montoInicial, usuario);
            return ResponseEntity.ok(caja);
        } catch (IllegalArgumentException e) {
            log.error("Turno inválido: {}", turnoStr);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Turno inválido. Valores válidos: MAÑANA, TARDE, NOCHE"));
        } catch (IllegalStateException e) {
            log.error("Error: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/cajas/{id}/cerrar
     * Cierra una caja específica
     *
     * Body:
     * {
     *   "usuario": "admin"
     * }
     */
    @PostMapping("/{id}/cerrar")
    public ResponseEntity<?> cerrarCaja(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        String usuario = body.get("usuario");
        log.info("POST /api/cajas/{}/cerrar - Usuario: {}", id, usuario);

        try {
            Caja caja = cajaService.cerrarCaja(id, usuario);
            return ResponseEntity.ok(caja);
        } catch (IllegalStateException e) {
            log.error("Error: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PATCH /api/cajas/{id}/saldo-por-cobrar
     * Actualiza el saldo por cobrar de una caja
     *
     * Body:
     * {
     *   "saldo": 50000
     * }
     */
    @PatchMapping("/{id}/saldo-por-cobrar")
    public ResponseEntity<?> actualizarSaldoPorCobrar(
            @PathVariable Long id,
            @RequestBody Map<String, Double> body
    ) {
        Double saldo = body.get("saldo");
        log.info("PATCH /api/cajas/{}/saldo-por-cobrar - Nuevo saldo: {}", id, saldo);

        try {
            Caja caja = cajaService.actualizarSaldoPorCobrar(id, saldo);
            return ResponseEntity.ok(caja);
        } catch (IllegalStateException e) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/cajas/numero/{numero}/historial
     * Obtiene el historial de una caja específica
     */
    @GetMapping("/numero/{numero}/historial")
    public ResponseEntity<List<Caja>> getHistorialCaja(@PathVariable Integer numero) {
        log.info("GET /api/cajas/numero/{}/historial", numero);
        return ResponseEntity.ok(cajaService.getHistorialCaja(numero));
    }

    /**
     * GET /api/cajas/stats
     * Estadísticas de cajas
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        log.info("GET /api/cajas/stats");

        List<Caja> cajasAbiertas = cajaService.getCajasAbiertas();
        long totalCajas = cajaService.getAllCajas().size();

        Map<String, Object> stats = new HashMap<>();
        stats.put("cajasAbiertas", cajasAbiertas.size());
        stats.put("totalCajas", totalCajas);
        stats.put("cajasCerradas", totalCajas - cajasAbiertas.size());

        return ResponseEntity.ok(stats);
    }
}
