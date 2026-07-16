package com.example.dilo.DiloBackend.service;

import com.example.dilo.DiloBackend.dto.request.ClienteRequestDTO;
import com.example.dilo.DiloBackend.dto.response.ClienteResponseDTO;

import java.util.List;

public interface ClienteService {
    List<ClienteResponseDTO> obtenerPorNegocio(Long negocioId);
    ClienteResponseDTO obtenerPorId(Long negocioId, Long id);
    ClienteResponseDTO crearCliente(Long negocioId, ClienteRequestDTO requestDTO);
    ClienteResponseDTO actualizarCliente(Long negocioId, Long id, ClienteRequestDTO requestDTO);
    void eliminarCliente(Long negocioId, Long id);
    List<ClienteResponseDTO> buscarPorTermino(Long negocioId, String term);
}
