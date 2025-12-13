package com.nexuspos.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO para registrar una compra
 */
@Data
public class CompraDTO {

    @NotNull(message = "El proveedor es obligatorio")
    private Long proveedorId;

    @NotBlank(message = "El número de documento es obligatorio")
    private String numeroDocumento;

    @NotNull(message = "La fecha de entrega es obligatoria")
    private LocalDate fechaEntrega;

    @NotBlank(message = "El método de pago es obligatorio")
    private String metodoPago;

    private String observaciones;

    private String usuario;

    @NotEmpty(message = "Debe incluir al menos un producto")
    @Valid
    private List<DetalleCompraDTO> detalles;
}
