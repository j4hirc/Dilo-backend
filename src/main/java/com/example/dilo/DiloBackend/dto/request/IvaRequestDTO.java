package com.example.dilo.DiloBackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class IvaRequestDTO {

    @NotBlank(message = "El valor del nuevo IVA es obligatorio (ej: 0.15)")
    private String nuevoIva;
}