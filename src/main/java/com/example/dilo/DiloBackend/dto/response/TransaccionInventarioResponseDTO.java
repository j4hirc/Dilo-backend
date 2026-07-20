package com.example.dilo.DiloBackend.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransaccionInventarioResponseDTO {
    private Long id;
    private String tipo;
    private String productoNombre;
    private String usuarioResponsableNombre;
    private String bodegaOrigenNombre;
    private String bodegaDestinoNombre;
    private Integer cantidad;
    private LocalDateTime fechaTransaccion;
    private String motivo;

    private BigDecimal costoUnitario;
    private BigDecimal costoTotal;
    private String metodoAplicado;
    private String documentoReferencia;
    private String numeroLote;
}