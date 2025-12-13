package com.nexuspos.backend.controller;

import com.nexuspos.backend.dto.CompraDTO;
import com.nexuspos.backend.model.Compra;
import com.nexuspos.backend.service.CompraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * CONTROLLER COMPRA - API REST
 */
@RestController
@RequestMapping("/api/compras")
@RequiredArgsConstructor
@Slf4j
public class CompraController {

    private final CompraService compraService;

    /**
     * POST /api/compras/registrar
     * Registra una nueva compra (actualiza inventario automáticamente)
     */
    @PostMapping("/registrar")
    public ResponseEntity<?> registrarCompra(@Valid @RequestBody CompraDTO compraDTO) {
        log.info("POST /api/compras/registrar - Documento: {}", compraDTO.getNumeroDocumento());
        try {
            Compra compra = compraService.registrarCompra(compraDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(compra);
        } catch (IllegalArgumentException e) {
            log.error("Error al registrar compra: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/compras
     * Obtiene todas las compras
     */
    @GetMapping
    public ResponseEntity<List<Compra>> getAll() {
        log.info("GET /api/compras");
        return ResponseEntity.ok(compraService.findAll());
    }

    /**
     * GET /api/compras/{id}
     * Obtiene una compra por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        log.info("GET /api/compras/{}", id);
        return compraService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    /**
     * GET /api/compras/proveedor/{proveedorId}
     * Obtiene compras de un proveedor
     */
    @GetMapping("/proveedor/{proveedorId}")
    public ResponseEntity<?> getByProveedor(@PathVariable Long proveedorId) {
        log.info("GET /api/compras/proveedor/{}", proveedorId);
        try {
            List<Compra> compras = compraService.findByProveedor(proveedorId);
            return ResponseEntity.ok(compras);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/compras/rango?inicio={fecha}&fin={fecha}
     * Obtiene compras en un rango de fechas
     */
    @GetMapping("/rango")
    public ResponseEntity<List<Compra>> getByRango(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin
    ) {
        log.info("GET /api/compras/rango?inicio={}&fin={}", inicio, fin);
        return ResponseEntity.ok(compraService.findByRangoFechas(inicio, fin));
    }

    /**
     * GET /api/compras/metodo-pago/{metodoPago}
     * Obtiene compras por método de pago
     */
    @GetMapping("/metodo-pago/{metodoPago}")
    public ResponseEntity<List<Compra>> getByMetodoPago(@PathVariable String metodoPago) {
        log.info("GET /api/compras/metodo-pago/{}", metodoPago);
        return ResponseEntity.ok(compraService.findByMetodoPago(metodoPago));
    }

    /**
     * GET /api/compras/ultimas
     * Obtiene últimas 10 compras
     */
    @GetMapping("/ultimas")
    public ResponseEntity<List<Compra>> getUltimas() {
        log.info("GET /api/compras/ultimas");
        return ResponseEntity.ok(compraService.findUltimas());
    }

    /**
     * GET /api/compras/buscar?documento={numero}
     * Busca compras por número de documento
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<Compra>> search(
        @RequestParam(required = false, defaultValue = "") String documento
    ) {
        log.info("GET /api/compras/buscar?documento={}", documento);
        return ResponseEntity.ok(compraService.searchByDocumento(documento));
    }

    /**
     * GET /api/compras/estadisticas/total?inicio={fecha}&fin={fecha}
     * Calcula total de compras en un periodo
     */
    @GetMapping("/estadisticas/total")
    public ResponseEntity<Map<String, Double>> getTotalCompras(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin
    ) {
        log.info("GET /api/compras/estadisticas/total");
        Double total = compraService.calcularTotalCompras(inicio, fin);
        return ResponseEntity.ok(Map.of("total", total));
    }

    /**
     * GET /api/compras/count
     * Cuenta total de compras
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> count() {
        return ResponseEntity.ok(Map.of("total", compraService.count()));
    }
}
