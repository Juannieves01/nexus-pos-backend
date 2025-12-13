package com.nexuspos.backend.model;

/**
 * ENUM - Estados de una Reserva
 */
public enum EstadoReserva {
    PENDIENTE,      // Reserva confirmada, esperando llegada del cliente
    CONFIRMADA,     // Cliente confirmó asistencia
    EN_MESA,        // Cliente llegó y está ocupando la mesa
    CANCELADA,      // Reserva cancelada
    NO_ASISTIO      // Cliente no llegó (no-show)
}
