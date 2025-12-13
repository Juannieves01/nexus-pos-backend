package com.nexuspos.backend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * DTO para detalle de compra
 */
@Data
public class DetalleCompraDTO {

    @NotNull(message = "El producto es obligatorio")
    private Long productoId;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;

    @NotNull(message = "El precio unitario es obligatorio")
    @Positive(message = "El precio unitario debe ser mayor a 0")
    private Double precioUnitario;
}
