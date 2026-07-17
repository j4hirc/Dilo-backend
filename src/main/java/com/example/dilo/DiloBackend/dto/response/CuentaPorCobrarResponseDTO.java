package com.example.dilo.DiloBackend.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CuentaPorCobrarResponseDTO {
    private Long id;
    private Long facturaId;
    private String numeroFactura;
    private LocalDateTime fechaVencimiento;
    private BigDecimal montoTotal;
    private BigDecimal saldoPendiente;
    private String estado;
    private List<CuotaResponseDTO> cuotas;
}