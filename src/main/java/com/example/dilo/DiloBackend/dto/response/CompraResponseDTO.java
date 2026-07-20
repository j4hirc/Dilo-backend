package com.example.dilo.DiloBackend.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CompraResponseDTO {
    private Long id;
    private String proveedorNombre;
    private String bodegaIngresoNombre;
    private String numeroComprobante;
    private LocalDateTime fechaCompra;
    private BigDecimal totalCompra;

    // Lista con el detalle de los productos que ingresaron
    private List<DetalleCompraResponseDTO> detalles;
}