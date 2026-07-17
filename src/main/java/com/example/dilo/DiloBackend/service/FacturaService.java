package com.example.dilo.DiloBackend.service;

import com.example.dilo.DiloBackend.dto.request.FacturaRequestDTO;
import com.example.dilo.DiloBackend.dto.response.FacturaResponseDTO;

public interface FacturaService {
    FacturaResponseDTO generarFactura(Long negocioId, String emailUsuario, FacturaRequestDTO requestDTO);
}