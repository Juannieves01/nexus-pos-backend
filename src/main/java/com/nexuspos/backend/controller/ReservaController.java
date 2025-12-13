package com.nexuspos.backend.controller;

import com.nexuspos.backend.model.EstadoReserva;
import com.nexuspos.backend.model.Reserva;
import com.nexuspos.backend.service.ReservaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * CONTROLLER DE RESERVAS - API REST
 */
@RestController
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
@Slf4j
public class ReservaController {

    private final ReservaService reservaService;

    /**
     * GET /api/reservas
     * Obtener todas las reservas
     */
    @GetMapping
    public ResponseEntity<List<Reserva>> getAllReservas() {
        log.info("GET /api/reservas");
        List<Reserva> reservas = reservaService.findAll();
        return ResponseEntity.ok(reservas);
    }

    /**
     * GET /api/reservas/{id}
     * Obtener reserva por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getReservaById(@PathVariable Long id) {
        log.info("GET /api/reservas/{}", id);

        return reservaService.findById(id)
            .map(reserva -> ResponseEntity.ok((Object) reserva))
            .orElseGet(() -> ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body("Reserva no encontrada"));
    }

    /**
     * GET /api/reservas/activas
     * Obtener solo reservas activas
     */
    @GetMapping("/activas")
    public ResponseEntity<List<Reserva>> getReservasActivas() {
        log.info("GET /api/reservas/activas");
        List<Reserva> reservas = reservaService.findReservasActivas();
        return ResponseEntity.ok(reservas);
    }

    /**
     * GET /api/reservas/hoy
     * Obtener reservas de hoy
     */
    @GetMapping("/hoy")
    public ResponseEntity<List<Reserva>> getReservasHoy() {
        log.info("GET /api/reservas/hoy");
        List<Reserva> reservas = reservaService.findReservasHoy();
        return ResponseEntity.ok(reservas);
    }

    /**
     * GET /api/reservas/rango?inicio=...&fin=...
     * Obtener reservas en un rango de fechas
     */
    @GetMapping("/rango")
    public ResponseEntity<List<Reserva>> getReservasEnRango(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin
    ) {
        log.info("GET /api/reservas/rango?inicio={}&fin={}", inicio, fin);
        List<Reserva> reservas = reservaService.findReservasEnRango(inicio, fin);
        return ResponseEntity.ok(reservas);
    }

    /**
     * GET /api/reservas/buscar?cliente=...
     * Buscar reservas por nombre de cliente
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<Reserva>> buscarPorCliente(@RequestParam String cliente) {
        log.info("GET /api/reservas/buscar?cliente={}", cliente);
        List<Reserva> reservas = reservaService.buscarPorCliente(cliente);
        return ResponseEntity.ok(reservas);
    }

    /**
     * POST /api/reservas
     * Crear nueva reserva
     *
     * Body:
     * {
     *   "mesa": { "id": 1 },
     *   "nombreCliente": "Juan Pérez",
     *   "telefonoCliente": "0981234567",
     *   "emailCliente": "juan@example.com",
     *   "fechaHoraReserva": "2024-01-15T19:00:00",
     *   "duracionMinutos": 120,
     *   "numeroPersonas": 4,
     *   "notas": "Cumpleaños",
     *   "creadoPor": "Admin"
     * }
     */
    @PostMapping
    public ResponseEntity<?> createReserva(@RequestBody Reserva reserva) {
        log.info("POST /api/reservas - Cliente: {}, Mesa: {}, Fecha: {}",
                 reserva.getNombreCliente(),
                 reserva.getMesa().getId(),
                 reserva.getFechaHoraReserva());

        try {
            Reserva nuevaReserva = reservaService.create(reserva);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaReserva);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Error al crear reserva: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/reservas/{id}
     * Actualizar reserva existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateReserva(
        @PathVariable Long id,
        @RequestBody Reserva reserva
    ) {
        log.info("PUT /api/reservas/{}", id);

        try {
            Reserva reservaActualizada = reservaService.update(id, reserva);
            return ResponseEntity.ok(reservaActualizada);
        } catch (IllegalArgumentException e) {
            log.error("Error: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PATCH /api/reservas/{id}/estado
     * Cambiar estado de reserva
     *
     * Body: { "estado": "CONFIRMADA" }
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(
        @PathVariable Long id,
        @RequestBody Map<String, String> body
    ) {
        String estadoStr = body.get("estado");
        log.info("PATCH /api/reservas/{}/estado - Nuevo estado: {}", id, estadoStr);

        try {
            EstadoReserva nuevoEstado = EstadoReserva.valueOf(estadoStr);
            Reserva reserva = reservaService.cambiarEstado(id, nuevoEstado);
            return ResponseEntity.ok(reserva);
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Estado inválido: " + estadoStr));
        }
    }

    /**
     * POST /api/reservas/{id}/confirmar
     * Confirmar reserva
     */
    @PostMapping("/{id}/confirmar")
    public ResponseEntity<Reserva> confirmar(@PathVariable Long id) {
        log.info("POST /api/reservas/{}/confirmar", id);
        Reserva reserva = reservaService.confirmar(id);
        return ResponseEntity.ok(reserva);
    }

    /**
     * POST /api/reservas/{id}/en-mesa
     * Marcar cliente como llegado
     */
    @PostMapping("/{id}/en-mesa")
    public ResponseEntity<Reserva> marcarEnMesa(@PathVariable Long id) {
        log.info("POST /api/reservas/{}/en-mesa", id);
        Reserva reserva = reservaService.marcarEnMesa(id);
        return ResponseEntity.ok(reserva);
    }

    /**
     * POST /api/reservas/{id}/cancelar
     * Cancelar reserva
     */
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<Reserva> cancelar(@PathVariable Long id) {
        log.info("POST /api/reservas/{}/cancelar", id);
        Reserva reserva = reservaService.cancelar(id);
        return ResponseEntity.ok(reserva);
    }

    /**
     * POST /api/reservas/{id}/no-asistio
     * Marcar como no asistió
     */
    @PostMapping("/{id}/no-asistio")
    public ResponseEntity<Reserva> marcarNoAsistio(@PathVariable Long id) {
        log.info("POST /api/reservas/{}/no-asistio", id);
        Reserva reserva = reservaService.marcarNoAsistio(id);
        return ResponseEntity.ok(reserva);
    }

    /**
     * DELETE /api/reservas/{id}
     * Eliminar reserva
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReserva(@PathVariable Long id) {
        log.info("DELETE /api/reservas/{}", id);

        try {
            reservaService.delete(id);
            return ResponseEntity.ok(Map.of("message", "Reserva eliminada"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/reservas/disponibilidad?mesaId=1&inicio=...&fin=...
     * Verificar disponibilidad de mesa
     */
    @GetMapping("/disponibilidad")
    public ResponseEntity<Map<String, Boolean>> verificarDisponibilidad(
        @RequestParam Long mesaId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin
    ) {
        log.info("GET /api/reservas/disponibilidad?mesaId={}&inicio={}&fin={}",
                 mesaId, inicio, fin);

        boolean disponible = reservaService.verificarDisponibilidad(mesaId, inicio, fin);
        return ResponseEntity.ok(Map.of("disponible", disponible));
    }
}
