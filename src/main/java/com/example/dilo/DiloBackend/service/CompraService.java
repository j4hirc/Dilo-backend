package com.example.dilo.DiloBackend.service;

import com.example.dilo.DiloBackend.dto.request.CompraRequestDTO;
import com.example.dilo.DiloBackend.dto.response.CompraResponseDTO;

import java.util.List;

public interface CompraService {
    CompraResponseDTO registrarCompra(Long negocioId, String emailUsuario, CompraRequestDTO requestDTO);
    List<CompraResponseDTO> obtenerComprasPorNegocio(Long negocioId);
}