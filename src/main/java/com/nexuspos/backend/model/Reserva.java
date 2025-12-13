package com.nexuspos.backend.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * MODELO DE RESERVA
 *
 * Gestiona reservas de mesas para clientes
 */
@Entity
@Table(name = "reservas")
@Data
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación con Mesa
    @ManyToOne
    @JoinColumn(name = "mesa_id", nullable = false)
    private Mesa mesa;

    // Información del cliente
    @Column(nullable = false, length = 100)
    private String nombreCliente;

    @Column(length = 20)
    private String telefonoCliente;

    @Column(length = 100)
    private String emailCliente;

    // Fecha y hora de la reserva
    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaHoraReserva;

    // Duración estimada en minutos (por defecto 120 min = 2 horas)
    @Column(nullable = false)
    private Integer duracionMinutos = 120;

    // Número de personas
    @Column(nullable = false)
    private Integer numeroPersonas;

    // Estado de la reserva
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReserva estado = EstadoReserva.PENDIENTE;

    // Notas adicionales
    @Column(length = 500)
    private String notas;

    // Usuario que creó la reserva
    @Column(length = 50)
    private String creadoPor;

    // Auditoría
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * Verifica si la reserva está activa (pendiente o confirmada)
     */
    public boolean isActiva() {
        return estado == EstadoReserva.PENDIENTE ||
               estado == EstadoReserva.CONFIRMADA ||
               estado == EstadoReserva.EN_MESA;
    }

    /**
     * Calcula la hora de fin estimada
     */
    public LocalDateTime getFechaHoraFin() {
        return fechaHoraReserva.plusMinutes(duracionMinutos);
    }
}
