package com.example.dilo.DiloBackend.dto.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DiloSpaceRequestDTO {


    @NotBlank(message = "El RUC no puede estar vacío")
    private String ruc;

    @NotBlank(message = "El nombre comercial no puede estar vacío")
    private String nombreComercial;

    @NotBlank(message = "La razón social no puede estar vacía")
    private String razonSocial;

    @NotNull(message = "Debe especificar si está obligado a llevar contabilidad")
    private Boolean obligadoContabilidad;

    @NotBlank(message = "La contraseña de la firma no puede estar vacía")
    private String passwordFirma;


}
