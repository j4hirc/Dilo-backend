package com.example.dilo.DiloBackend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class FacturaRequestDTO {


    private Long clienteId;

    @NotNull(message = "El método de pago es obligatorio")
    private String metodoPago;

    @NotEmpty(message = "La factura debe tener al menos un detalle")
    @Valid
    private List<DetalleFacturaRequestDTO> detalles;

    private BigDecimal descuentoGlobal;

    private String tarjeta;

    private Integer numeroCuotas;
}