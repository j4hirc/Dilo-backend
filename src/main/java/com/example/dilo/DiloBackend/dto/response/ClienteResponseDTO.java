package com.example.dilo.DiloBackend.dto.response;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ClienteResponseDTO {
    private Long id;
    private String dni;
    private String primerNombre;
    private String segundoNombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String nombreCompleto;
    private String email;
    private LocalDate fechaNacimiento;
    private String telefono;
    private String direccion;
}