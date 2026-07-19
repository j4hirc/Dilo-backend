package com.example.dilo.DiloBackend.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UpdateUsuarioDTO {
    private String primerNombre;
    private String segundoNombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String telefono;
    private String direccion;
    private LocalDate fechaNacimiento;
    private Long id_parroquia;
}