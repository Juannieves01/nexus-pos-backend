package com.nexuspos.backend.controller;

import com.nexuspos.backend.model.Gasto;
import com.nexuspos.backend.service.GastoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * CONTROLLER DE GASTO - API REST
 */
@RestController
@RequestMapping("/api/gastos")
@RequiredArgsConstructor
@Slf4j
public class GastoController {

    private final GastoService gastoService;

    /**
     * GET /api/gastos
     * Obtiene todos los gastos
     */
    @GetMapping
    public ResponseEntity<List<Gasto>> getAllGastos() {
        log.info("GET /api/gastos");
        List<Gasto> gastos = gastoService.findAll();
        return ResponseEntity.ok(gastos);
    }

    /**
     * GET /api/gastos/{id}
     * Obtiene un gasto por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getGastoById(@PathVariable Long id) {
        log.info("GET /api/gastos/{}", id);

        return gastoService.findById(id)
            .map(gasto -> ResponseEntity.ok((Object) gasto))
            .orElseGet(() -> ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body((Object) "Gasto no encontrado"));
    }

    /**
     * POST /api/gastos
     * Crea un nuevo gasto y lo descuenta de la caja
     *
     * Body:
     * {
     *   "gasto": {
     *     "concepto": "Compra de tomates",
     *     "monto": 25000,
     *     "tipoPago": "efectivo",
     *     "categoria": "Compras"
     *   },
     *   "cajaId": 1
     * }
     */
    @PostMapping
    public ResponseEntity<?> createGasto(@Valid @RequestBody Map<String, Object> body) {
        // Extraer gasto del body
        @SuppressWarnings("unchecked")
        Map<String, Object> gastoMap = (Map<String, Object>) body.get("gasto");
        Long cajaId = body.get("cajaId") != null ? ((Number) body.get("cajaId")).longValue() : null;

        // Si viene el objeto gasto directamente (compatibilidad hacia atrás)
        if (gastoMap == null && body.containsKey("concepto")) {
            gastoMap = body;
        }

        // Construir objeto Gasto
        Gasto gasto = new Gasto();
        gasto.setConcepto((String) gastoMap.get("concepto"));
        gasto.setMonto(((Number) gastoMap.get("monto")).doubleValue());
        gasto.setTipoPago((String) gastoMap.get("tipoPago"));
        gasto.setCategoria((String) gastoMap.get("categoria"));
        gasto.setPeriodo((String) gastoMap.get("periodo"));

        log.info("POST /api/gastos - Concepto: {}, Monto: {}, Caja ID: {}",
                gasto.getConcepto(), gasto.getMonto(), cajaId);

        try {
            Gasto created = gastoService.create(gasto, cajaId);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Error: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/gastos/{id}
     * Elimina un gasto
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGasto(@PathVariable Long id) {
        log.info("DELETE /api/gastos/{}", id);

        try {
            gastoService.delete(id);
            return ResponseEntity.ok(Map.of(
                "message", "Gasto eliminado",
                "id", id
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/gastos/tipo-pago/{tipoPago}
     * Obtiene gastos por tipo de pago
     */
    @GetMapping("/tipo-pago/{tipoPago}")
    public ResponseEntity<List<Gasto>> getGastosByTipoPago(@PathVariable String tipoPago) {
        log.info("GET /api/gastos/tipo-pago/{}", tipoPago);
        List<Gasto> gastos = gastoService.findByTipoPago(tipoPago);
        return ResponseEntity.ok(gastos);
    }

    /**
     * GET /api/gastos/periodo/{periodo}
     * Obtiene gastos por período
     */
    @GetMapping("/periodo/{periodo}")
    public ResponseEntity<List<Gasto>> getGastosByPeriodo(@PathVariable String periodo) {
        log.info("GET /api/gastos/periodo/{}", periodo);
        List<Gasto> gastos = gastoService.findByPeriodo(periodo);
        return ResponseEntity.ok(gastos);
    }

    /**
     * GET /api/gastos/totales
     * Obtiene totales de gastos
     */
    @GetMapping("/totales")
    public ResponseEntity<Map<String, Double>> getTotales() {
        log.info("GET /api/gastos/totales");

        Double total = gastoService.calcularTotal();
        Double efectivo = gastoService.calcularTotalPorTipoPago("efectivo");
        Double transferencias = gastoService.calcularTotalPorTipoPago("transferencia");

        return ResponseEntity.ok(Map.of(
            "total", total,
            "efectivo", efectivo,
            "transferencias", transferencias
        ));
    }

    /**
     * GET /api/gastos/count
     * Cuenta total de gastos
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> countGastos() {
        log.info("GET /api/gastos/count");
        long total = gastoService.count();
        return ResponseEntity.ok(Map.of("total", total));
    }
}
