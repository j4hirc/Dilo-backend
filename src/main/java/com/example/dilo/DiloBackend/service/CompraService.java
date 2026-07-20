package com.example.dilo.DiloBackend.service;

import com.example.dilo.DiloBackend.dto.request.CompraRequestDTO;
import com.example.dilo.DiloBackend.dto.response.CompraResponseDTO;

public interface CompraService {
    CompraResponseDTO registrarCompra(Long negocioId, String emailUsuario, CompraRequestDTO requestDTO);
}