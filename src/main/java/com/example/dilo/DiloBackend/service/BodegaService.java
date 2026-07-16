package com.example.dilo.DiloBackend.service;

import com.example.dilo.DiloBackend.dto.request.BodegaRequestDTO;
import com.example.dilo.DiloBackend.dto.response.BodegaResponseDTO;

import java.util.List;

public interface BodegaService {
    List<BodegaResponseDTO> obtenerPorNegocio(Long negocioId);
    BodegaResponseDTO obtenerPorId(Long negocioId, Long id);
    BodegaResponseDTO crearBodega(Long negocioId, BodegaRequestDTO requestDTO);
    BodegaResponseDTO actualizarBodega(Long negocioId, Long id, BodegaRequestDTO requestDTO);
    void eliminarBodega(Long negocioId, Long id);
    List<BodegaResponseDTO> buscarPorTermino(Long negocioId, String term);
}
