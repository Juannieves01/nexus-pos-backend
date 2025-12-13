package com.nexuspos.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * ENTIDAD CIERRE
 *
 * Representa un cierre de caja (histórico).
 * Guarda snapshot de los totales al momento del cierre.
 */
@Entity
@Table(name = "cierres")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cierre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Fecha y hora del cierre
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCierre;

    /**
     * Fecha de apertura de la caja cerrada
     */
    @Column(nullable = false)
    private LocalDateTime fechaApertura;

    /**
     * Base inicial con la que se abrió la caja
     */
    @Column(nullable = false)
    @NotNull(message = "La base inicial es obligatoria")
    @PositiveOrZero(message = "La base inicial no puede ser negativa")
    private Double baseInicial = 0.0;

    /**
     * Total en efectivo al cierre
     */
    @Column(nullable = false)
    @NotNull(message = "El efectivo es obligatorio")
    @PositiveOrZero(message = "El efectivo no puede ser negativo")
    private Double efectivo = 0.0;

    /**
     * Total en transferencias al cierre
     */
    @Column(nullable = false)
    @NotNull(message = "Las transferencias son obligatorias")
    @PositiveOrZero(message = "Las transferencias no pueden ser negativas")
    private Double transferencias = 0.0;

    /**
     * Total de ventas del período
     */
    @Column(nullable = false)
    @NotNull(message = "El total de ventas es obligatorio")
    @PositiveOrZero(message = "El total de ventas no puede ser negativo")
    private Double totalVentas = 0.0;

    /**
     * Total de gastos del período
     */
    @Column(nullable = false)
    @NotNull(message = "El total de gastos es obligatorio")
    @PositiveOrZero(message = "El total de gastos no puede ser negativo")
    private Double totalGastos = 0.0;

    /**
     * Saldo por cobrar (deudas de trabajadores)
     */
    @Column(nullable = false)
    @NotNull(message = "El saldo por cobrar es obligatorio")
    @PositiveOrZero(message = "El saldo por cobrar no puede ser negativo")
    private Double saldoPorCobrar = 0.0;

    /**
     * Base para el siguiente turno
     */
    @Column(nullable = false)
    @NotNull(message = "La base siguiente es obligatoria")
    @PositiveOrZero(message = "La base siguiente no puede ser negativa")
    private Double baseSiguiente = 0.0;

    /**
     * Reporte en texto plano
     */
    @Column(columnDefinition = "TEXT")
    private String reporte;

    /**
     * Usuario que cerró (opcional, por ahora)
     */
    @Column(length = 100)
    private String usuarioCierre;

    // Helper method para calcular efectivo final en caja
    public Double calcularEfectivoFinal() {
        return efectivo - totalGastos;
    }

    // Helper method para calcular el total general
    public Double calcularTotalGeneral() {
        return efectivo + transferencias;
    }
}
