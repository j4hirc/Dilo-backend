package com.example.dilo.DiloBackend.dto.response;

import lombok.Data;

@Data
public class InventarioBodegaResponseDTO {
    private Long id;
    private Long productoId;
    private String productoNombre;
    private String productoCodigo;
    private Long bodegaId;
    private String bodegaNombre;
    private Integer cantidadActual;
    private Integer stockMinimo;
    private boolean alertaStock;
}