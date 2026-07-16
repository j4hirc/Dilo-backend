package com.example.dilo.DiloBackend.service;

import com.example.dilo.DiloBackend.dto.request.ProductoRequestDTO;
import com.example.dilo.DiloBackend.dto.response.ProductoResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductoService {

    List<ProductoResponseDTO> obtenerPorNegocio(Long negocioId);
    List<ProductoResponseDTO> buscarPorTermino(Long negocioId, String term);
    ProductoResponseDTO obtenerPorId(Long negocioId, Long id);
    ProductoResponseDTO crearProducto(Long negocioId, ProductoRequestDTO requestDTO, MultipartFile imagen);
    ProductoResponseDTO actualizarProducto(Long negocioId, Long id, ProductoRequestDTO requestDTO, MultipartFile imagen);
    void eliminarProducto(Long negocioId, Long id);
}
