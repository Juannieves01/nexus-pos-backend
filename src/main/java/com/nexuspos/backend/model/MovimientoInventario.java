package com.nexuspos.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * ENTIDAD MOVIMIENTO INVENTARIO
 *
 * Registra todos los movimientos de stock (entradas, salidas, ajustes)
 */
@Entity
@Table(name = "movimientos_inventario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Producto relacionado
     */
    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    @NotNull(message = "El producto es obligatorio")
    private Producto producto;

    /**
     * Tipo de movimiento
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull(message = "El tipo de movimiento es obligatorio")
    private TipoMovimiento tipo;

    /**
     * Cantidad del movimiento
     */
    @Column(nullable = false)
    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser positiva")
    private Integer cantidad;

    /**
     * Stock anterior (antes del movimiento)
     */
    @Column(nullable = false)
    private Integer stockAnterior;

    /**
     * Stock nuevo (después del movimiento)
     */
    @Column(nullable = false)
    private Integer stockNuevo;

    /**
     * Costo unitario (opcional, para entradas)
     */
    private Double costoUnitario;

    /**
     * Costo total del movimiento
     */
    private Double costoTotal;

    /**
     * Motivo o descripción del movimiento
     */
    @Column(length = 500)
    private String motivo;

    /**
     * Usuario que realizó el movimiento (opcional)
     */
    @Column(length = 100)
    private String usuario;

    /**
     * Fecha del movimiento
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    /**
     * Número de factura o documento relacionado (opcional)
     */
    @Column(length = 50)
    private String numeroDocumento;
}
