package com.example.dilo.DiloBackend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ClienteRequestDTO {

    @NotBlank(message = "El DNI/RUC es obligatorio")
    private String dni;

    @NotBlank(message = "El primer nombre es obligatorio")
    private String primerNombre;

    private String segundoNombre;

    @NotBlank(message = "El apellido paterno es obligatorio")
    private String apellidoPaterno;

    private String apellidoMaterno;

    @Email(message = "El formato del email no es válido")
    private String email;

    private LocalDate fechaNacimiento;
    private String telefono;
    private String direccion;
}