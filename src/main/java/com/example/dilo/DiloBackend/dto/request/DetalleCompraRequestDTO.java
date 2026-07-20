package com.example.dilo.DiloBackend.dto.request;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DetalleCompraRequestDTO {
    @NotNull(message = "El producto es obligatorio")
    private Long productoId;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;

    @NotNull(message = "El costo unitario es obligatorio")
    private BigDecimal costoUnitario;

    private LocalDate fechaCaducidad; // Opcional, depende del producto
}