package com.example.dilo.DiloBackend.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CuotaResponseDTO {
    private Long id;
    private Integer numeroCuota;
    private BigDecimal montoCuota;
    private BigDecimal saldoPendienteCuota;
    private LocalDateTime fechaVencimiento;
    private String estado;
}