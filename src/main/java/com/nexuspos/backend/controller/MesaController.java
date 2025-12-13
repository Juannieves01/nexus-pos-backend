package com.nexuspos.backend.controller;

import com.nexuspos.backend.model.Mesa;
import com.nexuspos.backend.service.MesaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * CONTROLLER DE MESA - API REST
 *
 * Endpoints para gestionar mesas del restaurante
 */
@RestController
@RequestMapping("/api/mesas")
@RequiredArgsConstructor
@Slf4j
public class MesaController {

    private final MesaService mesaService;

    // =========================================================================
    // CRUD BÁSICO
    // =========================================================================

    /**
     * GET /api/mesas
     * Obtiene todas las mesas ordenadas por número
     */
    @GetMapping
    public ResponseEntity<List<Mesa>> getAllMesas() {
        log.info("GET /api/mesas - Obteniendo todas las mesas");
        List<Mesa> mesas = mesaService.findAll();
        return ResponseEntity.ok(mesas);
    }

    /**
     * GET /api/mesas/{id}
     * Obtiene una mesa por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getMesaById(@PathVariable Long id) {
        log.info("GET /api/mesas/{} - Buscando mesa", id);

        return mesaService.findById(id)
            .map(mesa -> ResponseEntity.ok((Object) mesa))
            .orElseGet(() -> ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body((Object) ("Mesa no encontrada con id: " + id)));
    }

    /**
     * POST /api/mesas
     * Crea una nueva mesa
     *
     * Body:
     * {
     *   "numero": 1,
     *   "nombre": "Terraza"
     * }
     */
    @PostMapping
    public ResponseEntity<?> createMesa(@Valid @RequestBody Mesa mesa) {
        log.info("POST /api/mesas - Creando mesa: {}", mesa.getNombre());

        try {
            Mesa created = mesaService.create(mesa);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            log.error("Error al crear mesa: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/mesas/{id}
     * Actualiza una mesa existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMesa(
        @PathVariable Long id,
        @Valid @RequestBody Mesa mesa
    ) {
        log.info("PUT /api/mesas/{} - Actualizando mesa", id);

        try {
            Mesa updated = mesaService.update(id, mesa);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            log.error("Error al actualizar mesa: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/mesas/{id}
     * Elimina una mesa
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMesa(@PathVariable Long id) {
        log.info("DELETE /api/mesas/{} - Eliminando mesa", id);

        try {
            mesaService.delete(id);
            return ResponseEntity.ok(Map.of(
                "message", "Mesa eliminada exitosamente",
                "id", id
            ));
        } catch (IllegalArgumentException e) {
            log.error("Error al eliminar mesa: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    // =========================================================================
    // OPERACIONES DE ESTADO
    // =========================================================================

    /**
     * PATCH /api/mesas/{id}/ocupar
     * Marca una mesa como ocupada
     */
    @PatchMapping("/{id}/ocupar")
    public ResponseEntity<?> ocuparMesa(@PathVariable Long id) {
        log.info("PATCH /api/mesas/{}/ocupar", id);

        try {
            Mesa mesa = mesaService.ocupar(id);
            return ResponseEntity.ok(mesa);
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PATCH /api/mesas/{id}/liberar
     * Marca una mesa como libre y limpia pedidos
     */
    @PatchMapping("/{id}/liberar")
    public ResponseEntity<?> liberarMesa(@PathVariable Long id) {
        log.info("PATCH /api/mesas/{}/liberar", id);

        try {
            Mesa mesa = mesaService.liberar(id);
            return ResponseEntity.ok(mesa);
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }

    // =========================================================================
    // OPERACIONES DE PEDIDOS
    // =========================================================================

    /**
     * POST /api/mesas/{id}/pedidos
     * Agrega un pedido a la mesa
     *
     * Body:
     * {
     *   "productoId": 1,
     *   "cantidad": 3
     * }
     */
    @PostMapping("/{id}/pedidos")
    public ResponseEntity<?> agregarPedido(
        @PathVariable Long id,
        @RequestBody Map<String, Object> body
    ) {
        Long productoId = Long.valueOf(body.get("productoId").toString());
        Integer cantidad = Integer.valueOf(body.get("cantidad").toString());

        log.info("POST /api/mesas/{}/pedidos - Producto: {}, Cantidad: {}", id, productoId, cantidad);

        try {
            Mesa mesa = mesaService.agregarPedido(id, productoId, cantidad);
            return ResponseEntity.ok(mesa);
        } catch (IllegalArgumentException e) {
            log.error("Error: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/mesas/{mesaId}/pedidos/{pedidoId}
     * Quita un pedido de la mesa
     */
    @DeleteMapping("/{mesaId}/pedidos/{pedidoId}")
    public ResponseEntity<?> quitarPedido(
        @PathVariable Long mesaId,
        @PathVariable Long pedidoId
    ) {
        log.info("DELETE /api/mesas/{}/pedidos/{}", mesaId, pedidoId);

        try {
            Mesa mesa = mesaService.quitarPedido(mesaId, pedidoId);
            return ResponseEntity.ok(mesa);
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PATCH /api/mesas/{mesaId}/pedidos/{pedidoId}
     * Actualiza la cantidad de un pedido
     *
     * Body:
     * {
     *   "cantidad": 5
     * }
     */
    @PatchMapping("/{mesaId}/pedidos/{pedidoId}")
    public ResponseEntity<?> actualizarCantidadPedido(
        @PathVariable Long mesaId,
        @PathVariable Long pedidoId,
        @RequestBody Map<String, Integer> body
    ) {
        Integer nuevaCantidad = body.get("cantidad");

        log.info("PATCH /api/mesas/{}/pedidos/{} - Nueva cantidad: {}", mesaId, pedidoId, nuevaCantidad);

        try {
            Mesa mesa = mesaService.actualizarCantidadPedido(mesaId, pedidoId, nuevaCantidad);
            return ResponseEntity.ok(mesa);
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    // =========================================================================
    // CONSULTAS ESPECÍFICAS
    // =========================================================================

    /**
     * GET /api/mesas/ocupadas
     * Obtiene solo las mesas ocupadas
     */
    @GetMapping("/ocupadas")
    public ResponseEntity<List<Mesa>> getMesasOcupadas() {
        log.info("GET /api/mesas/ocupadas");
        List<Mesa> mesas = mesaService.findMesasOcupadas();
        return ResponseEntity.ok(mesas);
    }

    /**
     * GET /api/mesas/libres
     * Obtiene solo las mesas libres
     */
    @GetMapping("/libres")
    public ResponseEntity<List<Mesa>> getMesasLibres() {
        log.info("GET /api/mesas/libres");
        List<Mesa> mesas = mesaService.findMesasLibres();
        return ResponseEntity.ok(mesas);
    }

    /**
     * GET /api/mesas/count
     * Obtiene el total de mesas
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> countMesas() {
        log.info("GET /api/mesas/count");
        long total = mesaService.count();
        long ocupadas = mesaService.countByEstado("ocupada");
        long libres = mesaService.countByEstado("libre");

        return ResponseEntity.ok(Map.of(
            "total", total,
            "ocupadas", ocupadas,
            "libres", libres
        ));
    }
}
