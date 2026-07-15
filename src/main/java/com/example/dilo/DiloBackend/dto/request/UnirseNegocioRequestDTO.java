package com.example.dilo.DiloBackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UnirseNegocioRequestDTO {
    @NotBlank(message = "El código de invitación no puede estar vacío")
    private String codigoInvitacion;

    @NotNull(message = "El ID del rol es obligatorio")
    private Long idRol;
}
