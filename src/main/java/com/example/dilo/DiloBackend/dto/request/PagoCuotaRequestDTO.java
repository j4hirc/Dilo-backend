package com.example.dilo.DiloBackend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PagoCuotaRequestDTO {

    @NotNull(message = "El monto a pagar es obligatorio")
    @Positive(message = "El monto debe ser mayor a 0")
    private BigDecimal montoPago;
}