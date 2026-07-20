package com.example.dilo.DiloBackend.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AlertaCaducidadResponseDTO {
    private Long loteId;
    private String productoNombre;
    private String productoCodigo;
    private String bodegaNombre;
    private BigDecimal cantidadDisponible;
    private String unidadMedida;
    private LocalDate fechaCaducidad;
    private long diasRestantes; // Super útil para el frontend
}