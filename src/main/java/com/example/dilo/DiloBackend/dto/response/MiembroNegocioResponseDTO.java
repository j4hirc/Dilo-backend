package com.example.dilo.DiloBackend.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MiembroNegocioResponseDTO {

    private Long id;
    private Long usuarioId;
    private String nombreUsuario;
    private String emailUsuario;
    private String rol;
    private String fotoPerfil;
    private String estadoLaboral;
    private String estadoInvitacion;
    private LocalDateTime fechaVinculacion;
}
