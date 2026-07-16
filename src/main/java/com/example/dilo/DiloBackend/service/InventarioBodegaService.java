package com.example.dilo.DiloBackend.service;

import com.example.dilo.DiloBackend.dto.request.InventarioBodegaRequestDTO;
import com.example.dilo.DiloBackend.dto.response.InventarioBodegaResponseDTO;

import java.util.List;

public interface InventarioBodegaService {
    List<InventarioBodegaResponseDTO> obtenerInventarioGeneral(Long negocioId);
    List<InventarioBodegaResponseDTO> obtenerInventarioPorBodega(Long negocioId, Long bodegaId);
    InventarioBodegaResponseDTO inicializarInventario(Long negocioId, InventarioBodegaRequestDTO requestDTO);
    InventarioBodegaResponseDTO actualizarStockMinimo(Long negocioId, Long id, Integer nuevoStockMinimo);
    InventarioBodegaResponseDTO actualizarCantidadActual(Long negocioId, Long id, Integer nuevaCantidad);
}
