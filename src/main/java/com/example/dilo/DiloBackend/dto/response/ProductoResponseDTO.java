package com.example.dilo.DiloBackend.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductoResponseDTO {
    private Long id;
    private Long categoriaId;
    private String categoriaNombre;
    private String codigoPrincipal;
    private String marca;
    private String imagen;
    private String nombre;
    private BigDecimal precioUnitario;
    private Boolean grabaIva;
}
