package com.nexuspos.backend.controller;

import com.nexuspos.backend.model.Pedido;
import com.nexuspos.backend.service.PedidoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * CONTROLLER DE PEDIDO - API REST
 *
 * Endpoints básicos para consultar pedidos
 * La mayoría de operaciones se hacen a través de MesaController
 */
@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
@Slf4j
public class PedidoController {

    private final PedidoService pedidoService;

    /**
     * GET /api/pedidos
     * Obtiene todos los pedidos
     */
    @GetMapping
    public ResponseEntity<List<Pedido>> getAllPedidos() {
        log.info("GET /api/pedidos - Obteniendo todos los pedidos");
        List<Pedido> pedidos = pedidoService.findAll();
        return ResponseEntity.ok(pedidos);
    }

    /**
     * GET /api/pedidos/{id}
     * Obtiene un pedido por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getPedidoById(@PathVariable Long id) {
        log.info("GET /api/pedidos/{} - Buscando pedido", id);

        return pedidoService.findById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity
                .notFound()
                .build());
    }

    /**
     * GET /api/pedidos/mesa/{mesaId}
     * Obtiene todos los pedidos de una mesa
     */
    @GetMapping("/mesa/{mesaId}")
    public ResponseEntity<List<Pedido>> getPedidosByMesa(@PathVariable Long mesaId) {
        log.info("GET /api/pedidos/mesa/{}", mesaId);
        List<Pedido> pedidos = pedidoService.findByMesaId(mesaId);
        return ResponseEntity.ok(pedidos);
    }

    /**
     * GET /api/pedidos/mesa/{mesaId}/count
     * Cuenta los pedidos de una mesa
     */
    @GetMapping("/mesa/{mesaId}/count")
    public ResponseEntity<Map<String, Long>> countPedidosByMesa(@PathVariable Long mesaId) {
        log.info("GET /api/pedidos/mesa/{}/count", mesaId);
        Long count = pedidoService.countByMesaId(mesaId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * GET /api/pedidos/mesa/{mesaId}/total
     * Calcula el total de una mesa
     */
    @GetMapping("/mesa/{mesaId}/total")
    public ResponseEntity<Map<String, Double>> getTotalMesa(@PathVariable Long mesaId) {
        log.info("GET /api/pedidos/mesa/{}/total", mesaId);
        Double total = pedidoService.calcularTotalMesa(mesaId);
        return ResponseEntity.ok(Map.of("total", total));
    }
}
