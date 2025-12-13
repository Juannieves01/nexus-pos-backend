package com.nexuspos.backend.controller;

import com.nexuspos.backend.model.Descuento;
import com.nexuspos.backend.service.DescuentoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * CONTROLLER DE DESCUENTOS - API REST
 */
@RestController
@RequestMapping("/api/descuentos")
@RequiredArgsConstructor
@Slf4j
public class DescuentoController {

    private final DescuentoService descuentoService;

    /**
     * GET /api/descuentos
     * Obtener todos los descuentos
     */
    @GetMapping
    public ResponseEntity<List<Descuento>> getAllDescuentos() {
        log.info("GET /api/descuentos");
        List<Descuento> descuentos = descuentoService.findAll();
        return ResponseEntity.ok(descuentos);
    }

    /**
     * GET /api/descuentos/{id}
     * Obtener descuento por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getDescuentoById(@PathVariable Long id) {
        log.info("GET /api/descuentos/{}", id);

        return descuentoService.findById(id)
            .map(descuento -> ResponseEntity.ok((Object) descuento))
            .orElseGet(() -> ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body("Descuento no encontrado"));
    }

    /**
     * GET /api/descuentos/activos
     * Obtener solo descuentos activos
     */
    @GetMapping("/activos")
    public ResponseEntity<List<Descuento>> getDescuentosActivos() {
        log.info("GET /api/descuentos/activos");
        List<Descuento> descuentos = descuentoService.findActivos();
        return ResponseEntity.ok(descuentos);
    }

    /**
     * GET /api/descuentos/validos
     * Obtener descuentos válidos actualmente
     */
    @GetMapping("/validos")
    public ResponseEntity<List<Descuento>> getDescuentosValidos() {
        log.info("GET /api/descuentos/validos");
        List<Descuento> descuentos = descuentoService.findValidos();
        return ResponseEntity.ok(descuentos);
    }

    /**
     * GET /api/descuentos/buscar?nombre=...
     * Buscar descuentos por nombre
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<Descuento>> buscarDescuentos(@RequestParam String nombre) {
        log.info("GET /api/descuentos/buscar?nombre={}", nombre);
        List<Descuento> descuentos = descuentoService.buscarPorNombre(nombre);
        return ResponseEntity.ok(descuentos);
    }

    /**
     * GET /api/descuentos/mejor?total=...
     * Encontrar el mejor descuento para un total dado
     */
    @GetMapping("/mejor")
    public ResponseEntity<?> getMejorDescuento(@RequestParam double total) {
        log.info("GET /api/descuentos/mejor?total={}", total);

        Optional<Descuento> mejor = descuentoService.encontrarMejorDescuento(total);

        if (mejor.isPresent()) {
            Descuento descuento = mejor.get();
            return ResponseEntity.ok(Map.of(
                "descuento", descuento,
                "montoDescuento", descuento.calcularDescuento(total),
                "totalFinal", descuento.aplicarDescuento(total)
            ));
        } else {
            return ResponseEntity.ok(Map.of("mensaje", "No hay descuentos aplicables"));
        }
    }

    /**
     * POST /api/descuentos
     * Crear nuevo descuento
     *
     * Body:
     * {
     *   "nombre": "10% de descuento",
     *   "descripcion": "Descuento del 10% en compras mayores a 50000",
     *   "tipo": "PORCENTAJE",
     *   "valor": 10,
     *   "compraMinima": 50000,
     *   "activo": true,
     *   "fechaInicio": "2024-01-01T00:00:00",
     *   "fechaFin": "2024-12-31T23:59:59"
     * }
     */
    @PostMapping
    public ResponseEntity<?> createDescuento(@RequestBody Descuento descuento) {
        log.info("POST /api/descuentos - Nombre: {}", descuento.getNombre());

        try {
            Descuento nuevoDescuento = descuentoService.create(descuento);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoDescuento);
        } catch (IllegalArgumentException e) {
            log.error("Error al crear descuento: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/descuentos/{id}
     * Actualizar descuento existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDescuento(
        @PathVariable Long id,
        @RequestBody Descuento descuento
    ) {
        log.info("PUT /api/descuentos/{}", id);

        try {
            Descuento descuentoActualizado = descuentoService.update(id, descuento);
            return ResponseEntity.ok(descuentoActualizado);
        } catch (IllegalArgumentException e) {
            log.error("Error: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PATCH /api/descuentos/{id}/toggle
     * Activar/desactivar descuento
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<?> toggleDescuento(@PathVariable Long id) {
        log.info("PATCH /api/descuentos/{}/toggle", id);

        try {
            Descuento descuento = descuentoService.toggleActivo(id);
            return ResponseEntity.ok(descuento);
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/descuentos/{id}
     * Eliminar descuento
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDescuento(@PathVariable Long id) {
        log.info("DELETE /api/descuentos/{}", id);

        try {
            descuentoService.delete(id);
            return ResponseEntity.ok(Map.of("message", "Descuento eliminado"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }
}
