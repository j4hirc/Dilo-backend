package com.example.dilo.DiloBackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BodegaRequestDTO {

    @NotBlank(message = "El nombre de la bodega es obligatorio")
    private String nombre;

    private String direccion;
}