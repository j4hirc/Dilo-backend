package com.example.dilo.DiloBackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MiembroNegocioRequestDTO {

    @NotNull(message = "El id del usuario no puede ser nulo")
    private Long idUsuario;
    @NotNull(message = "El id del Negocio no puede ser nulo")
    private Long idNegocio;
    @NotNull(message = "El id del rol no puede ser nulo")
    private Long idRol;

    @NotBlank(message = "El estado laboral no puede estar vacio")
    private String estadoLaboral;

    private String estadoInvitacion;

}
