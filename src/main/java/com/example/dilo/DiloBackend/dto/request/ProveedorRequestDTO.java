package com.example.dilo.DiloBackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ProveedorRequestDTO {

    @NotBlank(message = "El DNI es obligatorio")
    private String dni;

    @NotBlank(message = "El nombre comercial es obligatorio")
    private String nombre;

    private String telefono;

    private Boolean estado;

    // Lista de IDs de las categorías que este proveedor distribuye
    private List<Long> categoriasIds;
}