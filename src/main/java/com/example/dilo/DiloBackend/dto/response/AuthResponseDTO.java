package com.example.dilo.DiloBackend.dto.response;

import lombok.Data;

@Data
public class AuthResponseDTO {
    private String token;
    private String tokenType = "Bearer ";
    private Long idUsuario;
    private String email;
    private String nombreCompleto;
    private String fotoPerfil;
    private boolean isSuperAdmin;
}
