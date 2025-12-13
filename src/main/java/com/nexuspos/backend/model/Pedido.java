package com.nexuspos.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * ENTIDAD PEDIDO
 *
 * Representa un producto agregado a una mesa.
 * Es la relación MANY-TO-MANY entre Mesa y Producto (tabla intermedia).
 *
 * Ejemplo:
 * - Mesa 5 tiene:
 *   - Pedido 1: 3 Hamburguesas (precio: 15000 c/u, subtotal: 45000)
 *   - Pedido 2: 2 Coca Colas (precio: 3000 c/u, subtotal: 6000)
 *   - Total mesa: 51000
 *
 * RELACIONES JPA:
 * ---------------
 *
 * @ManyToOne con Mesa:
 * - MUCHOS pedidos pertenecen a UNA mesa
 * - Columna: mesa_id (FK) en tabla pedidos
 *
 * @ManyToOne con Producto:
 * - MUCHOS pedidos usan UN mismo producto
 * - Columna: producto_id (FK) en tabla pedidos
 *
 * ¿Por qué guardamos precio y nombre del producto?
 * -------------------------------------------------
 * DESNORMALIZACIÓN INTENCIONAL:
 * - Guardamos precioUnitario y nombreProducto en el pedido
 * - Aunque ya existen en la tabla productos
 * - ¿Por qué? Para tener HISTÓRICO INMUTABLE
 *
 * Ejemplo:
 * 1. Hoy vendes hamburguesa a $15.000
 * 2. Mañana subes precio a $18.000
 * 3. Si solo guardas producto_id, el pedido de AYER mostraría $18.000 ❌
 * 4. Al guardar el precio en el pedido, el de AYER sigue mostrando $15.000 ✅
 *
 * Esto es CRÍTICO para:
 * - Reportes históricos
 * - Auditorías
 * - Contabilidad precisa
 */
@Entity
@Table(name = "pedidos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * RELACIÓN ManyToOne con Mesa
     *
     * @ManyToOne:
     * - Muchos pedidos pertenecen a una mesa
     * - Crea columna "mesa_id" en la tabla pedidos
     *
     * @JoinColumn(name = "mesa_id"):
     * - Personaliza el nombre de la columna FK
     * - Por defecto sería "mesa_id" de todas formas
     * - nullable = false: Un pedido SIEMPRE debe tener una mesa
     *
     * @JsonIgnore:
     * - Evita loops infinitos al serializar a JSON
     * - Sin esto: Mesa → Pedidos → Mesa → Pedidos → ...
     * - Con esto: Solo se serializa el ID de la mesa, no toda la entidad
     */
    @ManyToOne
    @JoinColumn(name = "mesa_id", nullable = false)
    @JsonIgnore
    private Mesa mesa;

    /**
     * RELACIÓN ManyToOne con Producto
     *
     * Guardamos la referencia al producto original
     * Pero NO dependemos de él para precio/nombre (ver abajo)
     */
    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    /**
     * NOMBRE del producto (snapshot)
     *
     * Guardamos el nombre del producto en el MOMENTO del pedido
     * Aunque el producto cambie de nombre después, este queda fijo
     */
    @Column(nullable = false, length = 100)
    @NotBlank(message = "El nombre del producto es obligatorio")
    private String nombreProducto;

    /**
     * CANTIDAD de productos
     *
     * Cuántas unidades de este producto se pidieron
     * Ejemplo: 3 hamburguesas, 2 coca colas
     */
    @Column(nullable = false)
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;

    /**
     * PRECIO UNITARIO (snapshot)
     *
     * Precio del producto en el MOMENTO del pedido
     * CRÍTICO: NO se actualiza si cambias el precio del producto después
     *
     * Ejemplo:
     * - Pedido creado hoy: precioUnitario = 15000
     * - Mañana cambias precio del producto a 18000
     * - Este pedido SIGUE mostrando 15000 ✅
     */
    @Column(nullable = false, columnDefinition = "DECIMAL(10,2)")
    @NotNull(message = "El precio unitario es obligatorio")
    @Positive(message = "El precio unitario debe ser mayor a 0")
    private Double precioUnitario;

    /**
     * SUBTOTAL
     *
     * Calculado: cantidad * precioUnitario
     * Se calcula automáticamente antes de guardar
     *
     * Ejemplo:
     * - cantidad = 3
     * - precioUnitario = 15000
     * - subtotal = 45000
     */
    @Column(nullable = false, columnDefinition = "DECIMAL(10,2)")
    @NotNull(message = "El subtotal es obligatorio")
    @PositiveOrZero(message = "El subtotal no puede ser negativo")
    private Double subtotal = 0.0;

    /**
     * FECHA DE CREACIÓN
     *
     * Cuándo se agregó el producto a la mesa
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // =========================================================================
    // MÉTODOS DE LIFECYCLE (JPA Callbacks)
    // =========================================================================

    /**
     * @PrePersist: Se ejecuta ANTES de insertar en la BD
     * @PreUpdate: Se ejecuta ANTES de actualizar en la BD
     *
     * Aquí calculamos automáticamente el subtotal
     */
    @PrePersist
    @PreUpdate
    private void calcularSubtotal() {
        if (this.cantidad != null && this.precioUnitario != null) {
            this.subtotal = this.cantidad * this.precioUnitario;
        }
    }

    // =========================================================================
    // MÉTODOS DE NEGOCIO
    // =========================================================================

    /**
     * Actualiza la cantidad del pedido
     *
     * @param nuevaCantidad Nueva cantidad
     * @throws IllegalArgumentException si la cantidad es menor a 1
     */
    public void actualizarCantidad(Integer nuevaCantidad) {
        if (nuevaCantidad == null || nuevaCantidad < 1) {
            throw new IllegalArgumentException("La cantidad debe ser al menos 1");
        }
        this.cantidad = nuevaCantidad;
        calcularSubtotal();
    }

    /**
     * Incrementa la cantidad en 1
     */
    public void incrementarCantidad() {
        this.cantidad++;
        calcularSubtotal();
    }

    /**
     * Decrementa la cantidad en 1
     * No permite bajar de 1
     */
    public void decrementarCantidad() {
        if (this.cantidad > 1) {
            this.cantidad--;
            calcularSubtotal();
        }
    }
}
