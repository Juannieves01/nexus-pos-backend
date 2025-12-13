package com.nexuspos.backend.model;

/**
 * ENUM TIPO MOVIMIENTO
 *
 * Define los tipos de movimientos de inventario
 */
public enum TipoMovimiento {
    ENTRADA,        // Compra o entrada de stock
    SALIDA,         // Venta o salida de stock
    AJUSTE_POSITIVO, // Corrección aumentando stock
    AJUSTE_NEGATIVO, // Corrección disminuyendo stock
    DEVOLUCION      // Devolución de producto
}
