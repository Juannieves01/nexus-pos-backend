package com.nexuspos.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * ENTIDAD GASTO
 *
 * Representa un gasto del negocio.
 * Cada gasto reduce el dinero en caja.
 *
 * Ejemplos de gastos:
 * - Compra de ingredientes
 * - Pago de servicios (luz, agua)
 * - Salarios
 * - Mantenimiento
 */
@Entity
@Table(name = "gastos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Gasto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * CONCEPTO del gasto
     *
     * Descripción de para qué fue el gasto
     * Ejemplo: "Compra de tomates", "Pago de luz", "Salario mesero"
     */
    @Column(nullable = false, length = 200)
    @NotBlank(message = "El concepto es obligatorio")
    @Size(min = 3, max = 200, message = "El concepto debe tener entre 3 y 200 caracteres")
    private String concepto;

    /**
     * MONTO del gasto
     *
     * Cuánto dinero se gastó
     */
    @Column(nullable = false, columnDefinition = "DECIMAL(10,2)")
    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor a 0")
    private Double monto;

    /**
     * TIPO DE PAGO
     *
     * Valores: "efectivo" o "transferencia"
     * Determina de dónde se descuenta el dinero en la caja
     */
    @Column(name = "tipo_pago", nullable = false, length = 20)
    @NotBlank(message = "El tipo de pago es obligatorio")
    private String tipoPago; // "efectivo" o "transferencia"

    /**
     * FECHA del gasto
     *
     * Se registra automáticamente al crear
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    /**
     * PERÍODO de caja
     *
     * Asocia el gasto a un período de caja
     * Útil para reportes por turno
     *
     * Ejemplo: "2025-12-09_turno1"
     */
    @Column(length = 50)
    private String periodo;

    /**
     * USUARIO que registró el gasto
     *
     * Para auditoría (cuando implementes autenticación)
     */
    @Column(length = 50)
    private String usuario;

    /**
     * CATEGORÍA del gasto
     *
     * Para reportes agrupados
     * Ejemplo: "Compras", "Servicios", "Personal", "Mantenimiento"
     */
    @Column(length = 50)
    private String categoria;

    // =========================================================================
    // MÉTODOS DE NEGOCIO
    // =========================================================================

    /**
     * Verifica si el gasto fue pagado en efectivo
     *
     * @return true si tipoPago = "efectivo"
     */
    public boolean isEfectivo() {
        return "efectivo".equalsIgnoreCase(this.tipoPago);
    }

    /**
     * Verifica si el gasto fue pagado por transferencia
     *
     * @return true si tipoPago = "transferencia"
     */
    public boolean isTransferencia() {
        return "transferencia".equalsIgnoreCase(this.tipoPago);
    }
}
