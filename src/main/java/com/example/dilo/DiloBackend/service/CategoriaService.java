package com.example.dilo.DiloBackend.service;

import com.example.dilo.DiloBackend.dto.request.CategoriaRequestDTO;
import com.example.dilo.DiloBackend.dto.response.CategoriaResponseDTO;

import java.util.List;

public interface CategoriaService {
    List<CategoriaResponseDTO> obtenerPorNegocio(Long negocioId);
    CategoriaResponseDTO crearCategoria(Long negocioId, CategoriaRequestDTO requestDTO);
    CategoriaResponseDTO actualizarCategoria(Long negocioId, Long id, CategoriaRequestDTO requestDTO);
    void eliminarCategoria(Long negocioId, Long id);
}
