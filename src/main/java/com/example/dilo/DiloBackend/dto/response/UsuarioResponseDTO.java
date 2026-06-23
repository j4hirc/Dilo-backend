package com.example.dilo.DiloBackend.dto.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UsuarioResponseDTO {

    private Long id;

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

    private String nameParroquia;
}
