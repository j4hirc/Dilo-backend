package com.example.dilo.DiloBackend.service;

import com.example.dilo.DiloBackend.dto.request.ParroquiaRequestDTO;
import com.example.dilo.DiloBackend.dto.response.ParroquiaResponseDTO;

import java.util.List;

public interface ParroquiaService {
    List<ParroquiaResponseDTO> obtenerTodas();
    ParroquiaResponseDTO obtenerPorId(Long id);
    ParroquiaResponseDTO crearParroquia(ParroquiaRequestDTO requestDTO);
    ParroquiaResponseDTO actualizarParroquia(Long id, ParroquiaRequestDTO requestDTO);
    void eliminarParroquia(Long id);

}
