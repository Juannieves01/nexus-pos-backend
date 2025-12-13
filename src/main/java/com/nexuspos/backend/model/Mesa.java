package com.nexuspos.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ENTIDAD MESA
 *
 * Representa una mesa del restaurante.
 *
 * RELACIONES JPA:
 * ---------------
 * @OneToMany con Pedido
 * - UNA mesa tiene MUCHOS pedidos
 * - Cuando la mesa está ocupada, tiene pedidos activos
 * - Cuando se cierra la mesa (venta), los pedidos se asocian a la venta
 *
 * mappedBy = "mesa":
 * - Indica que la relación se define en la entidad Pedido
 * - La columna "mesa_id" está en la tabla "pedidos", no en "mesas"
 * - Mesa es el lado "inverso" de la relación
 *
 * cascade = CascadeType.ALL:
 * - Si eliminas una mesa, se eliminan todos sus pedidos
 * - Si guardas una mesa con pedidos, se guardan automáticamente
 *
 * orphanRemoval = true:
 * - Si quitas un pedido de la lista, se elimina de la BD automáticamente
 */
@Entity
@Table(name = "mesas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * NÚMERO de la mesa
     *
     * Número único identificador (ej: 1, 2, 3...)
     * Útil para ordenar las mesas en el frontend
     */
    @Column(nullable = false, unique = true)
    @NotNull(message = "El número de mesa es obligatorio")
    @Min(value = 1, message = "El número de mesa debe ser mayor a 0")
    private Integer numero;

    /**
     * NOMBRE de la mesa
     *
     * Nombre descriptivo (ej: "Terraza", "Ventana", "VIP")
     * Ayuda a los meseros a ubicar mejor las mesas
     */
    @Column(nullable = false, length = 50)
    @NotBlank(message = "El nombre de la mesa es obligatorio")
    @Size(min = 1, max = 50, message = "El nombre debe tener entre 1 y 50 caracteres")
    private String nombre;

    /**
     * ESTADO de la mesa
     *
     * Valores permitidos:
     * - "libre": Mesa disponible para nuevos clientes
     * - "ocupada": Mesa con clientes activos
     *
     * NOTA: En una aplicación más avanzada, podrías usar un ENUM:
     * public enum EstadoMesa { LIBRE, OCUPADA, RESERVADA }
     */
    @Column(nullable = false, length = 20)
    @NotBlank(message = "El estado es obligatorio")
    private String estado = "libre"; // "libre" o "ocupada"

    /**
     * TOTAL acumulado de la cuenta
     *
     * Suma de todos los subtotales de los pedidos
     * Se calcula automáticamente al agregar/quitar pedidos
     */
    @Column(nullable = false, columnDefinition = "DECIMAL(10,2) DEFAULT 0")
    @NotNull(message = "El total es obligatorio")
    @PositiveOrZero(message = "El total no puede ser negativo")
    private Double total = 0.0;

    /**
     * RELACIÓN OneToMany con Pedido
     *
     * Una mesa tiene muchos pedidos.
     *
     * @OneToMany(mappedBy = "mesa"):
     * - "mesa" es el nombre del campo en la clase Pedido
     * - La FK está en la tabla pedidos (columna mesa_id)
     *
     * cascade = CascadeType.ALL:
     * - Operaciones en Mesa se propagan a Pedidos
     * - Si guardas Mesa, se guardan sus Pedidos
     * - Si eliminas Mesa, se eliminan sus Pedidos
     *
     * orphanRemoval = true:
     * - Si quitas un Pedido de la lista, se borra de la BD
     * - Útil para limpiar pedidos cancelados
     *
     * fetch = FetchType.LAZY (por defecto):
     * - No carga los pedidos automáticamente
     * - Solo cuando accedes a mesa.getPedidos()
     * - Mejor rendimiento (evita queries innecesarias)
     */
    @OneToMany(mappedBy = "mesa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pedido> pedidos = new ArrayList<>();

    /**
     * FECHA DE CREACIÓN
     *
     * Se establece automáticamente al crear la mesa
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * FECHA DE ÚLTIMA ACTUALIZACIÓN
     *
     * Se actualiza automáticamente cada vez que modificas la mesa
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // =========================================================================
    // MÉTODOS DE NEGOCIO (Business Logic)
    // =========================================================================

    /**
     * Verifica si la mesa está ocupada
     *
     * @return true si estado = "ocupada"
     */
    public boolean isOcupada() {
        return "ocupada".equalsIgnoreCase(this.estado);
    }

    /**
     * Verifica si la mesa está libre
     *
     * @return true si estado = "libre"
     */
    public boolean isLibre() {
        return "libre".equalsIgnoreCase(this.estado);
    }

    /**
     * Marca la mesa como ocupada
     */
    public void ocupar() {
        this.estado = "ocupada";
    }

    /**
     * Marca la mesa como libre y resetea el total
     */
    public void liberar() {
        this.estado = "libre";
        this.total = 0.0;
    }

    /**
     * Agrega un pedido a la mesa
     *
     * IMPORTANTE: Este método establece la relación bidireccional
     * - Agrega el pedido a la lista
     * - Establece esta mesa en el pedido
     * - Recalcula el total
     *
     * @param pedido Pedido a agregar
     */
    public void agregarPedido(Pedido pedido) {
        this.pedidos.add(pedido);
        pedido.setMesa(this); // Establecer relación bidireccional
        recalcularTotal();
        if (this.isLibre()) {
            this.ocupar();
        }
    }

    /**
     * Quita un pedido de la mesa
     *
     * @param pedido Pedido a quitar
     */
    public void quitarPedido(Pedido pedido) {
        this.pedidos.remove(pedido);
        pedido.setMesa(null); // Romper relación
        recalcularTotal();
        if (this.pedidos.isEmpty()) {
            this.liberar();
        }
    }

    /**
     * Recalcula el total sumando todos los subtotales de los pedidos
     */
    public void recalcularTotal() {
        this.total = this.pedidos.stream()
            .mapToDouble(Pedido::getSubtotal)
            .sum();
    }

    /**
     * Obtiene la cantidad total de productos en la mesa
     *
     * @return suma de cantidades de todos los pedidos
     */
    public int getCantidadProductos() {
        return this.pedidos.stream()
            .mapToInt(Pedido::getCantidad)
            .sum();
    }

    /**
     * Limpia todos los pedidos de la mesa
     * (usado al cerrar la mesa / procesar venta)
     */
    public void limpiarPedidos() {
        this.pedidos.clear();
        this.total = 0.0;
        this.liberar();
    }
}
