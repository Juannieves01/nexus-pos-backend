package com.nexuspos.backend.model;

/**
 * ENUM ROL
 *
 * Define los roles disponibles en el sistema
 */
public enum Rol {
    ADMINISTRADOR,  // Acceso total al sistema
    CAJERO,         // Gestión de caja, ventas y mesas
    MESERO          // Solo gestión de mesas y pedidos
}
