package com.example.dilo.DiloBackend.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
public class CompraRequestDTO {
    @NotNull(message = "El proveedor es obligatorio")
    private Long proveedorId;

    @NotNull(message = "La bodega de ingreso es obligatoria")
    private Long bodegaIngresoId;

    @NotNull(message = "El número de comprobante es obligatorio")
    private String numeroComprobante;

    @NotEmpty(message = "La compra debe tener al menos un producto")
    private List<DetalleCompraRequestDTO> detalles;
}