package com.nexuspos.backend.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * MODELO DE DESCUENTO
 *
 * Gestiona descuentos que pueden aplicarse a las ventas
 */
@Entity
@Table(name = "descuentos")
@Data
public class Descuento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nombre/código del descuento
    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    // Tipo de descuento (PORCENTAJE o MONTO_FIJO)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDescuento tipo;

    // Valor del descuento
    // Si tipo = PORCENTAJE: 0-100 (ej: 10 = 10%)
    // Si tipo = MONTO_FIJO: monto en guaraníes (ej: 5000)
    @Column(nullable = false)
    private Double valor;

    // Compra mínima requerida para aplicar el descuento (opcional)
    @Column(name = "compra_minima")
    private Double compraMinima;

    // Descuento activo o no
    @Column(nullable = false)
    private Boolean activo = true;

    // Validez (opcional)
    @Column(name = "fecha_inicio")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaFin;

    // Auditoría
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * Verifica si el descuento es válido en este momento
     */
    public boolean esValido() {
        if (!activo) {
            return false;
        }

        LocalDateTime ahora = LocalDateTime.now();

        // Verificar fecha de inicio
        if (fechaInicio != null && ahora.isBefore(fechaInicio)) {
            return false;
        }

        // Verificar fecha de fin
        if (fechaFin != null && ahora.isAfter(fechaFin)) {
            return false;
        }

        return true;
    }

    /**
     * Calcula el monto de descuento para un total dado
     */
    public double calcularDescuento(double total) {
        if (!esValido()) {
            return 0.0;
        }

        // Verificar compra mínima
        if (compraMinima != null && total < compraMinima) {
            return 0.0;
        }

        if (tipo == TipoDescuento.PORCENTAJE) {
            return total * (valor / 100.0);
        } else {
            // MONTO_FIJO
            return Math.min(valor, total); // No puede ser mayor al total
        }
    }

    /**
     * Calcula el total final después del descuento
     */
    public double aplicarDescuento(double total) {
        return total - calcularDescuento(total);
    }
}
