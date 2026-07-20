package com.example.dilo.DiloBackend.service;

import com.example.dilo.DiloBackend.dto.request.ProveedorRequestDTO;
import com.example.dilo.DiloBackend.dto.response.ProveedorResponseDTO;

import java.util.List;

public interface ProveedorService {
    List<ProveedorResponseDTO> getAllByNegocio(Long negocioId);
    ProveedorResponseDTO getById(Long id, Long negocioId);
    ProveedorResponseDTO create(Long negocioId, ProveedorRequestDTO requestDTO);
    ProveedorResponseDTO update(Long id, Long negocioId, ProveedorRequestDTO requestDTO);
    void delete(Long id, Long negocioId);
}