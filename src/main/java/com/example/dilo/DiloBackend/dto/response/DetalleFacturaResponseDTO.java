package com.example.dilo.DiloBackend.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DetalleFacturaResponseDTO {
    private Long id;
    private String productoNombre;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotalItem;
}