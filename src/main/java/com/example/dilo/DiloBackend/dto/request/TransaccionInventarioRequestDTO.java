package com.example.dilo.DiloBackend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransaccionInventarioRequestDTO {

    @NotBlank(message = "El tipo de transacción es obligatorio (INGRESO, EGRESO, TRANSFERENCIA)")
    private String tipo;

    @NotNull(message = "El ID del producto es obligatorio")
    private Long productoId;

    private Long bodegaOrigenId;
    private Long bodegaDestinoId;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;

    @NotBlank(message = "El motivo de la transacción es obligatorio")
    private String motivo;

    private BigDecimal costoUnitario;

    private String documentoReferencia;

    private Long loteId;
}