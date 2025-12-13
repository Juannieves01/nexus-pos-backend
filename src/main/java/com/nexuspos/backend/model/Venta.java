package com.nexuspos.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ENTIDAD VENTA
 *
 * Representa una venta cerrada (mesa pagada).
 * Guarda un snapshot de los pedidos para histórico.
 */
@Entity
@Table(name = "ventas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Mesa asociada (número + nombre para referencia)
     */
    @Column(nullable = false, length = 100)
    @NotBlank(message = "El identificador de mesa es obligatorio")
    private String mesa; // Ejemplo: "1 - Terraza"

    /**
     * Total de la venta
     */
    @Column(nullable = false)
    @NotNull(message = "El total es obligatorio")
    @PositiveOrZero(message = "El total no puede ser negativo")
    private Double total = 0.0;

    /**
     * Monto pagado en efectivo
     */
    @Column(nullable = false)
    @NotNull(message = "El efectivo es obligatorio")
    @PositiveOrZero(message = "El efectivo no puede ser negativo")
    private Double efectivo = 0.0;

    /**
     * Monto pagado con transferencia
     */
    @Column(nullable = false)
    @NotNull(message = "Las transferencias son obligatorias")
    @PositiveOrZero(message = "Las transferencias no pueden ser negativas")
    private Double transferencias = 0.0;

    /**
     * Productos vendidos (snapshot de los pedidos)
     * Stored as JSON text for simplicity
     */
    @Column(columnDefinition = "TEXT")
    private String productosJson;

    /**
     * Fecha de creación
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Helper method para calcular si hay cambio
    public Double calcularCambio() {
        Double totalPagado = efectivo + transferencias;
        return totalPagado - total;
    }
}
