package com.example.dilo.DiloBackend.service;

import com.example.dilo.DiloBackend.dto.request.FacturaRequestDTO;
import com.example.dilo.DiloBackend.dto.response.FacturaResponseDTO;

import java.util.List;

public interface FacturaService {
    FacturaResponseDTO generarFactura(Long negocioId, String emailUsuario, FacturaRequestDTO requestDTO);

    // NUEVO: Método para listar las facturas
    List<FacturaResponseDTO> obtenerFacturasPorNegocio(Long negocioId);
}