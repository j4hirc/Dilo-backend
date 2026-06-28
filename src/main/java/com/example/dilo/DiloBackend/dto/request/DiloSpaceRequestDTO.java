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

    @NotNull(message = "El ID de la parroquia no puede ser nulo")
    private Boolean obligadoContabilidad;

    @NotBlank(message = "El path de la firma electrónica no puede estar vacío")
    private String firmaElectronicaPath;

    private LocalDateTime fechaCreacion;

    @NotNull(message = "El ID del usuario no puede ser nulo")
    private Long usuarioId;
}
