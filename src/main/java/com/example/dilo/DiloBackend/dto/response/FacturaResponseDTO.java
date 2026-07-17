package com.example.dilo.DiloBackend.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class FacturaResponseDTO {
    private Long id;
    private String numeroFactura;
    private LocalDateTime fechaEmision;
    private String clienteNombre;
    private String clienteIdentificacion;
    private BigDecimal subtotalIva0;
    private BigDecimal subtotalIvaAplicado;
    private BigDecimal totalIva;
    private BigDecimal totalFactura;
    private String formaPago;
    private String estadoSri;
    private List<DetalleFacturaResponseDTO> detalles;
}