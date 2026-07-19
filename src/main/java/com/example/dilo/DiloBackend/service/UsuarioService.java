package com.example.dilo.DiloBackend.service;

import com.example.dilo.DiloBackend.dto.request.UpdateUsuarioDTO;
import com.example.dilo.DiloBackend.dto.response.UsuarioResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UsuarioService {
    UsuarioResponseDTO obtenerMiPerfil(String email);

    List<UsuarioResponseDTO> obtenerTodosLosUsuarios();


    UsuarioResponseDTO actualizarMiPerfil(String email, UpdateUsuarioDTO dto, MultipartFile foto);

    List<UsuarioResponseDTO> obtenerUsuariosPorNegocio(Long negocioId);

}