package com.example.dilo.DiloBackend.service;

import com.example.dilo.DiloBackend.dto.response.UsuarioResponseDTO;

import java.util.List;

public interface UsuarioService {
    UsuarioResponseDTO obtenerMiPerfil(String email);

    List<UsuarioResponseDTO> obtenerTodosLosUsuarios();
}