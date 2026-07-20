package com.example.dilo.DiloBackend.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DetalleFacturaResponseDTO {
    private Long id;
    private String productoNombre;
    private Integer cantidad;
    private BigDecimal precioUnitario; // Precio al que se vendió
    private BigDecimal subtotalItem;
    private BigDecimal costoUnitarioReal; // Lo que le costó al negocio
    private BigDecimal costoTotalReal;    // Costo total de esa línea
}