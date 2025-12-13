package com.nexuspos.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ENTIDAD CAJA
 *
 * Representa el control de caja del negocio.
 * Ahora soporta MÚLTIPLES CAJAS y TURNOS.
 *
 * FLUJO DE CAJA:
 * 1. Abrir caja: Se crea con número de caja, turno y saldo inicial
 * 2. Durante el turno: Se registran ventas y gastos
 * 3. Cerrar caja: Se genera reporte y se archiva
 *
 * Pueden haber múltiples cajas abiertas simultáneamente.
 */
@Entity
@Table(name = "caja")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Caja {

    /**
     * ID de la caja (auto-incremento)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * NÚMERO DE CAJA
     *
     * Identifica físicamente la caja (Caja 1, Caja 2, etc.)
     */
    @Column(name = "numero_caja", nullable = false)
    @NotNull(message = "El número de caja es obligatorio")
    @Positive(message = "El número de caja debe ser positivo")
    private Integer numeroCaja;

    /**
     * TURNO de trabajo
     *
     * MAÑANA, TARDE o NOCHE
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Turno turno;

    /**
     * EFECTIVO en caja
     *
     * Dinero físico disponible
     */
    @Column(nullable = false, columnDefinition = "DECIMAL(12,2) DEFAULT 0")
    @NotNull(message = "El efectivo es obligatorio")
    @PositiveOrZero(message = "El efectivo no puede ser negativo")
    private Double efectivo = 0.0;

    /**
     * TRANSFERENCIAS acumuladas
     *
     * Pagos electrónicos (tarjeta, transferencia, etc.)
     */
    @Column(nullable = false, columnDefinition = "DECIMAL(12,2) DEFAULT 0")
    @NotNull(message = "Las transferencias son obligatorias")
    @PositiveOrZero(message = "Las transferencias no pueden ser negativas")
    private Double transferencias = 0.0;

    /**
     * SALDO POR COBRAR
     *
     * Dinero que deben los trabajadores (fiados)
     * En tu sistema actual, se usa para control de efectivo
     */
    @Column(name = "saldo_por_cobrar", columnDefinition = "DECIMAL(12,2) DEFAULT 0")
    @PositiveOrZero(message = "El saldo por cobrar no puede ser negativo")
    private Double saldoPorCobrar = 0.0;

    /**
     * ESTADO de la caja
     *
     * true = Abierta (se pueden hacer operaciones)
     * false = Cerrada (solo consulta)
     */
    @Column(nullable = false)
    @NotNull(message = "El estado de apertura es obligatorio")
    private Boolean abierta = false;

    /**
     * BASE INICIAL
     *
     * Dinero con el que se abrió la caja
     * Útil para calcular ganancia neta al cierre
     */
    @Column(name = "base_inicial", columnDefinition = "DECIMAL(12,2)")
    private Double baseInicial = 0.0;

    /**
     * FECHA Y HORA DE APERTURA
     *
     * Cuándo se abrió la caja
     */
    @Column(name = "fecha_apertura")
    private LocalDateTime fechaApertura;

    /**
     * FECHA Y HORA DE CIERRE
     *
     * Cuándo se cerró la caja (null si está abierta)
     */
    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    /**
     * USUARIO que abrió la caja
     *
     * En el futuro, cuando implementes autenticación
     */
    @Column(name = "usuario_apertura", length = 50)
    private String usuarioApertura;

    /**
     * USUARIO que cerró la caja
     */
    @Column(name = "usuario_cierre", length = 50)
    private String usuarioCierre;

    // =========================================================================
    // MÉTODOS DE NEGOCIO
    // =========================================================================

    /**
     * Calcula el total en caja
     *
     * @return efectivo + transferencias
     */
    public Double getTotal() {
        return this.efectivo + this.transferencias;
    }

    /**
     * Abre la caja con un monto inicial
     *
     * @param numeroCaja Número de la caja física (1, 2, 3, etc.)
     * @param turno Turno de trabajo
     * @param montoInicial Efectivo inicial
     * @param usuario Usuario que abre la caja
     */
    public void abrir(Integer numeroCaja, Turno turno, Double montoInicial, String usuario) {
        this.numeroCaja = numeroCaja;
        this.turno = turno;
        this.abierta = true;
        this.baseInicial = montoInicial != null ? montoInicial : 0.0;
        this.efectivo = this.baseInicial;
        this.transferencias = 0.0;
        this.saldoPorCobrar = 0.0;
        this.fechaApertura = LocalDateTime.now();
        this.fechaCierre = null;
        this.usuarioApertura = usuario;
    }

    /**
     * Cierra la caja
     *
     * @param usuario Usuario que cierra la caja
     */
    public void cerrar(String usuario) {
        this.abierta = false;
        this.fechaCierre = LocalDateTime.now();
        this.usuarioCierre = usuario;
    }

    /**
     * Registra una venta en efectivo
     *
     * @param monto Monto de la venta
     */
    public void registrarVentaEfectivo(Double monto) {
        if (!this.abierta) {
            throw new IllegalStateException("La caja está cerrada");
        }
        this.efectivo += monto;
    }

    /**
     * Registra una venta por transferencia
     *
     * @param monto Monto de la transferencia
     */
    public void registrarVentaTransferencia(Double monto) {
        if (!this.abierta) {
            throw new IllegalStateException("La caja está cerrada");
        }
        this.transferencias += monto;
    }

    /**
     * Registra un gasto en efectivo
     *
     * @param monto Monto del gasto
     * @throws IllegalArgumentException si no hay suficiente efectivo
     */
    public void registrarGastoEfectivo(Double monto) {
        if (!this.abierta) {
            throw new IllegalStateException("La caja está cerrada");
        }
        if (this.efectivo < monto) {
            throw new IllegalArgumentException(
                String.format("Efectivo insuficiente. Disponible: %.2f, Solicitado: %.2f",
                    this.efectivo, monto)
            );
        }
        this.efectivo -= monto;
    }

    /**
     * Registra un gasto por transferencia
     *
     * @param monto Monto del gasto
     */
    public void registrarGastoTransferencia(Double monto) {
        if (!this.abierta) {
            throw new IllegalStateException("La caja está cerrada");
        }
        this.transferencias -= monto;
    }

    /**
     * Verifica si la caja está abierta
     *
     * @return true si está abierta
     */
    public boolean isAbierta() {
        return this.abierta != null && this.abierta;
    }
}
