package com.example.dilo.DiloBackend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class FacturaRequestDTO {

    @NotNull(message = "El ID del cliente es obligatorio")
    private Long clienteId;

    @NotNull(message = "El método de pago es obligatorio")
    private String metodoPago;

    @NotEmpty(message = "La factura debe tener al menos un detalle")
    @Valid
    private List<DetalleFacturaRequestDTO> detalles;

    private Integer numeroCuotas;
}