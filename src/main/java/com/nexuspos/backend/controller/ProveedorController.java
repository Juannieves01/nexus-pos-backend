package com.nexuspos.backend.controller;

import com.nexuspos.backend.model.Proveedor;
import com.nexuspos.backend.service.ProveedorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * CONTROLLER PROVEEDOR - API REST
 */
@RestController
@RequestMapping("/api/proveedores")
@RequiredArgsConstructor
@Slf4j
public class ProveedorController {

    private final ProveedorService proveedorService;

    /**
     * GET /api/proveedores
     * Obtiene todos los proveedores
     */
    @GetMapping
    public ResponseEntity<List<Proveedor>> getAll() {
        log.info("GET /api/proveedores");
        return ResponseEntity.ok(proveedorService.findAll());
    }

    /**
     * GET /api/proveedores/activos
     * Obtiene solo proveedores activos
     */
    @GetMapping("/activos")
    public ResponseEntity<List<Proveedor>> getActivos() {
        log.info("GET /api/proveedores/activos");
        return ResponseEntity.ok(proveedorService.findActivos());
    }

    /**
     * GET /api/proveedores/{id}
     * Obtiene proveedor por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        log.info("GET /api/proveedores/{}", id);
        return proveedorService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(null));
    }

    /**
     * POST /api/proveedores
     * Crea un nuevo proveedor
     */
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody Proveedor proveedor) {
        log.info("POST /api/proveedores - {}", proveedor.getNombre());
        try {
            Proveedor created = proveedorService.create(proveedor);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            log.error("Error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/proveedores/{id}
     * Actualiza un proveedor
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
        @PathVariable Long id,
        @Valid @RequestBody Proveedor proveedor
    ) {
        log.info("PUT /api/proveedores/{}", id);
        try {
            Proveedor updated = proveedorService.update(id, proveedor);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            log.error("Error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/proveedores/{id}
     * Elimina un proveedor
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        log.info("DELETE /api/proveedores/{}", id);
        try {
            proveedorService.delete(id);
            return ResponseEntity.ok(Map.of(
                "message", "Proveedor eliminado exitosamente",
                "id", id
            ));
        } catch (IllegalArgumentException e) {
            log.error("Error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PATCH /api/proveedores/{id}/toggle
     * Activa/desactiva un proveedor
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<?> toggleActivo(@PathVariable Long id) {
        log.info("PATCH /api/proveedores/{}/toggle", id);
        try {
            Proveedor updated = proveedorService.toggleActivo(id);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/proveedores/buscar?nombre={nombre}
     * Busca proveedores por nombre
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<Proveedor>> search(
        @RequestParam(required = false, defaultValue = "") String nombre
    ) {
        log.info("GET /api/proveedores/buscar?nombre={}", nombre);
        return ResponseEntity.ok(proveedorService.search(nombre));
    }

    /**
     * GET /api/proveedores/count
     * Cuenta proveedores activos
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> count() {
        return ResponseEntity.ok(Map.of("total", proveedorService.countActivos()));
    }
}
