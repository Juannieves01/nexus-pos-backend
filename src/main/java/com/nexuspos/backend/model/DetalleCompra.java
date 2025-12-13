package com.nexuspos.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ENTIDAD DETALLE COMPRA (Detail)
 *
 * Representa cada producto comprado dentro de una compra.
 * Relación Detail en el patrón Master-Detail.
 *
 * SNAPSHOT PATTERN:
 * - Guarda nombreProducto y precioUnitario en el momento de la compra
 * - Aunque el producto cambie después, este registro queda inmutable
 */
@Entity
@Table(name = "detalles_compra")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Compra a la que pertenece (relación ManyToOne)
     * @JsonIgnore previene loops infinitos en serialización JSON
     */
    @ManyToOne
    @JoinColumn(name = "compra_id", nullable = false)
    @JsonIgnore
    private Compra compra;

    /**
     * Producto comprado (relación ManyToOne)
     */
    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    @NotNull(message = "El producto es obligatorio")
    private Producto producto;

    /**
     * SNAPSHOT: Nombre del producto en el momento de la compra
     */
    @Column(nullable = false, length = 100)
    @NotBlank(message = "El nombre del producto es obligatorio")
    private String nombreProducto;

    /**
     * Cantidad comprada
     */
    @Column(nullable = false)
    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;

    /**
     * SNAPSHOT: Precio unitario en el momento de la compra
     */
    @Column(nullable = false, columnDefinition = "DECIMAL(10,2)")
    @NotNull(message = "El precio unitario es obligatorio")
    @Positive(message = "El precio unitario debe ser mayor a 0")
    private Double precioUnitario;

    /**
     * Subtotal = cantidad * precioUnitario
     */
    @Column(nullable = false, columnDefinition = "DECIMAL(10,2)")
    @NotNull(message = "El subtotal es obligatorio")
    @PositiveOrZero(message = "El subtotal no puede ser negativo")
    private Double subtotal = 0.0;

    // =========================================================================
    // LIFECYCLE CALLBACKS
    // =========================================================================

    /**
     * Calcula automáticamente el subtotal antes de guardar/actualizar
     */
    @PrePersist
    @PreUpdate
    private void calcularSubtotal() {
        if (this.cantidad != null && this.precioUnitario != null) {
            this.subtotal = this.cantidad * this.precioUnitario;
        }
    }
}
