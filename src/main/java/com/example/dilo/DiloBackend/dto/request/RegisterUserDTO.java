package com.example.dilo.DiloBackend.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class RegisterUserDTO {


    private String dni;

    private String fotoPerfil;

    private String primerNombre;

    private String segundoNombre;

    private String apellidoPaterno;

    private String apellidoMaterno;

    private String email;

    private String password;

    private LocalDate fechaNacimiento;

    private String telefono;

    private String direccion;

    private Long id_parroquia;
}
