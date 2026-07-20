package com.example.dilo.DiloBackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductoRequestDTO {
    @NotNull(message = "El ID de la categoría es obligatorio")
    private Long categoriaId;

    @NotBlank(message = "El código principal es obligatorio")
    private String codigoPrincipal;

    private String marca;

    @NotBlank(message = "El nombre del producto es obligatorio")
    private String nombre;

    @NotNull(message = "El precio unitario es obligatorio")
    private BigDecimal precioUnitario;

    @NotNull(message = "Debe especificar si graba IVA")
    private Boolean grabaIva;

    // --- NUEVOS CAMPOS ---
    private String unidadMedida;

    private Boolean tieneCaducidad;
}