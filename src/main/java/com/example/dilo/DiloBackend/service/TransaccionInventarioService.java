package com.example.dilo.DiloBackend.service;

import com.example.dilo.DiloBackend.dto.request.TransaccionInventarioRequestDTO;
import com.example.dilo.DiloBackend.dto.response.TransaccionInventarioResponseDTO;

import java.util.List;

public interface TransaccionInventarioService {
    List<TransaccionInventarioResponseDTO> obtenerKardexGeneral(Long negocioId);
    List<TransaccionInventarioResponseDTO> obtenerKardexPorProducto(Long negocioId, Long productoId);
    TransaccionInventarioResponseDTO registrarTransaccion(Long negocioId, String emailUsuario, TransaccionInventarioRequestDTO requestDTO);
}
