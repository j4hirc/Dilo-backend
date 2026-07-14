package com.example.dilo.DiloBackend.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class MiembroNegocioResponseDTO {

    private Long nombreUsuario;
    private Long NombreNegocio;
    private Long nombreRol;

    private String estadoLaboral;

    private String estadoInvitacion;

    private LocalDateTime fechaVinculacion;

}
