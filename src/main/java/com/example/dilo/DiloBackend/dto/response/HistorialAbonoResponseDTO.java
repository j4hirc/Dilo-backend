package com.example.dilo.DiloBackend.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class HistorialAbonoResponseDTO {
    private Long id;
    private BigDecimal montoAbonado;
    private LocalDateTime fechaAbono;
    private String metodoPago;
    private String referencia;
    private String usuarioRecibio;
}