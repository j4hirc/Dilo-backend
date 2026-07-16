package com.example.dilo.DiloBackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ParroquiaRequestDTO {

    @NotBlank(message = "El nombre de la parroquia es obligatorio")
    private String nombre;
}