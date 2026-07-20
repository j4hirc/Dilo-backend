package com.example.dilo.DiloBackend.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DetalleCompraResponseDTO {
    private Long productoId;
    private String productoNombre;
    private Integer cantidad;
    private BigDecimal costoUnitario;
    private BigDecimal costoTotal;
    private LocalDate fechaCaducidad;
}