package com.nexuspos.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * ENTIDAD PRODUCTO
 *
 * Esta clase representa la tabla 'productos' en PostgreSQL.
 * Cada instancia de Producto es una fila en esa tabla.
 *
 * ANOTACIONES JPA (Jakarta Persistence API):
 * ------------------------------------------
 *
 * @Entity - Le dice a JPA: "Esta clase es una entidad, créame una tabla"
 *
 * @Table - Personaliza el nombre de la tabla en la base de datos
 *          Si no pones @Table, el nombre será "Producto" (nombre de la clase)
 *          Con @Table(name = "productos"), la tabla se llama "productos"
 *
 * @Data (Lombok) - Genera automáticamente:
 *          - getters y setters para todos los campos
 *          - toString()
 *          - equals() y hashCode()
 *          - constructor con parámetros requeridos
 *
 * @NoArgsConstructor - Genera constructor vacío (requerido por JPA)
 * @AllArgsConstructor - Genera constructor con todos los parámetros
 */
@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    /**
     * ID - Clave Primaria
     *
     * @Id - Marca este campo como clave primaria
     *
     * @GeneratedValue - Genera automáticamente el valor
     *      strategy = IDENTITY: La base de datos genera el ID (AUTO_INCREMENT)
     *
     * ¿Por qué Long y no long?
     * - Long puede ser null (útil antes de guardar en BD)
     * - long primitivo no puede ser null
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * NOMBRE del producto
     *
     * @Column - Personaliza la columna en la tabla
     *      nullable = false: NO puede ser NULL en la base de datos
     *      length = 100: VARCHAR(100) en PostgreSQL
     *
     * @NotBlank - Validación de Spring (antes de llegar a BD)
     *      - No puede ser null
     *      - No puede ser string vacío ""
     *      - No puede ser solo espacios "   "
     *
     * @Size - Validación de tamaño
     *      min = 1, max = 100 caracteres
     *
     * ¿Por qué 2 validaciones (@Column y @NotBlank)?
     * - @Column: Regla en la BASE DE DATOS (última línea de defensa)
     * - @NotBlank: Validación en JAVA (primera línea de defensa, mejor UX)
     */
    @Column(nullable = false, length = 100)
    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(min = 1, max = 100, message = "El nombre debe tener entre 1 y 100 caracteres")
    private String nombre;

    /**
     * CATEGORÍA del producto
     *
     * Ejemplos: "Bebidas", "Alimentos", "Postres", "Entradas"
     *
     * nullable = true (puede ser null, es opcional)
     * length = 50: VARCHAR(50)
     */
    @Column(length = 50)
    @Size(max = 50, message = "La categoría no puede exceder 50 caracteres")
    private String categoria;

    /**
     * PRECIO del producto
     *
     * @NotNull - No puede ser null (pero puede ser 0)
     *
     * @Positive - Debe ser mayor a 0
     *      (no queremos precios negativos o gratis)
     *
     * @Column - columnDefinition: Tipo exacto en PostgreSQL
     *      DECIMAL(10,2): 10 dígitos totales, 2 decimales
     *      Ejemplo: 9999999.99 (casi 10 millones)
     *
     * ¿Por qué Double y no Float?
     * - Double: 64 bits, más preciso para dinero
     * - Float: 32 bits, puede perder precisión
     */
    @Column(nullable = false, columnDefinition = "DECIMAL(10,2)")
    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor a 0")
    private Double precio;

    /**
     * STOCK (cantidad disponible)
     *
     * @PositiveOrZero - Puede ser 0 o mayor
     *      (stock agotado = 0, no puede ser negativo)
     *
     * ¿Por qué Integer y no int?
     * - Integer: Puede ser null
     * - int: Siempre tiene valor (0 por defecto)
     */
    @Column(nullable = false)
    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    /**
     * Stock mínimo (nivel de alerta)
     */
    @Column(nullable = false)
    private Integer stockMinimo = 10;

    /**
     * FECHA DE CREACIÓN
     *
     * @CreationTimestamp - Hibernate genera automáticamente la fecha
     *      cuando se crea el registro (INSERT)
     *
     * @Column - updatable = false: Una vez creado, no se puede cambiar
     *
     * LocalDateTime: Fecha y hora sin zona horaria
     *      Ejemplo: 2025-12-09T14:30:00
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * FECHA DE ÚLTIMA ACTUALIZACIÓN
     *
     * @UpdateTimestamp - Hibernate actualiza automáticamente esta fecha
     *      cada vez que modificas el registro (UPDATE)
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // =========================================================================
    // MÉTODOS DE NEGOCIO (Business Logic)
    // =========================================================================

    /**
     * Verifica si el producto tiene stock bajo (menos de 20 unidades)
     *
     * ¿Por qué aquí y no en un Service?
     * - Lógica simple que solo involucra el mismo objeto
     * - Sigue el principio DDD (Domain-Driven Design)
     *
     * @return true si stock < 20
     */
    public boolean isStockBajo() {
        return this.stock != null && this.stock < 20;
    }

    /**
     * Verifica si el producto tiene stock crítico (menos de 10 unidades)
     *
     * @return true si stock < 10
     */
    public boolean isStockCritico() {
        return this.stock != null && this.stock < 10;
    }

    /**
     * Incrementa el stock del producto
     *
     * @param cantidad cantidad a agregar
     */
    public void agregarStock(Integer cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        this.stock += cantidad;
    }

    /**
     * Reduce el stock del producto
     *
     * @param cantidad cantidad a reducir
     * @throws IllegalArgumentException si no hay suficiente stock
     */
    public void reducirStock(Integer cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        if (this.stock < cantidad) {
            throw new IllegalArgumentException(
                "Stock insuficiente. Disponible: " + this.stock + ", Solicitado: " + cantidad
            );
        }
        this.stock -= cantidad;
    }
}
