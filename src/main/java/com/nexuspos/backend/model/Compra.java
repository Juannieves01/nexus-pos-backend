package com.nexuspos.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ENTIDAD COMPRA (Master)
 *
 * Representa una compra realizada a un proveedor.
 * Relación Master-Detail con DetalleCompra.
 */
@Entity
@Table(name = "compras")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Proveedor asociado (relación ManyToOne)
     */
    @ManyToOne
    @JoinColumn(name = "proveedor_id", nullable = false)
    @NotNull(message = "El proveedor es obligatorio")
    private Proveedor proveedor;

    /**
     * Número de factura o documento
     */
    @Column(nullable = false, length = 50)
    @NotBlank(message = "El número de documento es obligatorio")
    @Size(max = 50, message = "El número de documento no puede exceder 50 caracteres")
    private String numeroDocumento;

    /**
     * Fecha de entrega/recepción
     */
    @Column(nullable = false)
    @NotNull(message = "La fecha de entrega es obligatoria")
    private LocalDate fechaEntrega;

    /**
     * Método de pago
     */
    @Column(nullable = false, length = 30)
    @NotBlank(message = "El método de pago es obligatorio")
    private String metodoPago; // EFECTIVO, TRANSFERENCIA, CREDITO, etc.

    /**
     * Total de la compra (suma de subtotales de detalles)
     */
    @Column(nullable = false, columnDefinition = "DECIMAL(10,2)")
    @NotNull(message = "El total es obligatorio")
    @PositiveOrZero(message = "El total no puede ser negativo")
    private Double total = 0.0;

    /**
     * Observaciones o notas adicionales
     */
    @Column(columnDefinition = "TEXT")
    private String observaciones;

    /**
     * Usuario que registró la compra
     */
    @Column(length = 100)
    private String usuario;

    /**
     * Detalles de la compra (relación OneToMany)
     *
     * mappedBy = "compra": La FK está en DetalleCompra
     * cascade = CascadeType.ALL: Operaciones se propagan
     * orphanRemoval = true: Si quitas un detalle de la lista, se borra de BD
     */
    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleCompra> detalles = new ArrayList<>();

    /**
     * Fecha de creación del registro
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // =========================================================================
    // MÉTODOS DE NEGOCIO
    // =========================================================================

    /**
     * Agrega un detalle a la compra
     * Establece la relación bidireccional
     */
    public void agregarDetalle(DetalleCompra detalle) {
        this.detalles.add(detalle);
        detalle.setCompra(this);
        recalcularTotal();
    }

    /**
     * Quita un detalle de la compra
     */
    public void quitarDetalle(DetalleCompra detalle) {
        this.detalles.remove(detalle);
        detalle.setCompra(null);
        recalcularTotal();
    }

    /**
     * Recalcula el total sumando todos los subtotales
     */
    public void recalcularTotal() {
        this.total = this.detalles.stream()
            .mapToDouble(DetalleCompra::getSubtotal)
            .sum();
    }
}
