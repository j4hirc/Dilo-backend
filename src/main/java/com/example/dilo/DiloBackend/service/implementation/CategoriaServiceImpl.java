package com.example.dilo.DiloBackend.service.implementation;

import com.example.dilo.DiloBackend.dto.request.CategoriaRequestDTO;
import com.example.dilo.DiloBackend.dto.response.CategoriaResponseDTO;
import com.example.dilo.DiloBackend.exception.ResourceNotFoundException;
import com.example.dilo.DiloBackend.model.Categoria;
import com.example.dilo.DiloBackend.model.Negocio;
import com.example.dilo.DiloBackend.repository.CategoriaRepository;
import com.example.dilo.DiloBackend.repository.NegocioRepository;
import com.example.dilo.DiloBackend.service.CategoriaService;
import com.example.dilo.DiloBackend.service.mapper.CategoriaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final NegocioRepository negocioRepository;
    private final CategoriaMapper categoriaMapper;

    @Override
    public List<CategoriaResponseDTO> obtenerPorNegocio(Long negocioId) {
        return categoriaRepository.findByNegocioId(negocioId).stream()
                .map(categoriaMapper::toDto)
                .toList();
    }

    @Override
    public CategoriaResponseDTO crearCategoria(Long negocioId, CategoriaRequestDTO requestDTO) {
        Negocio negocio = negocioRepository.findById(negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Negocio no encontrado"));

        if (categoriaRepository.existsByNombreIgnoreCaseAndNegocioId(requestDTO.getNombre(), negocioId)) {
            throw new RuntimeException("Ya existe una categoría con ese nombre en este negocio");
        }

        Categoria categoria = categoriaMapper.toEntity(requestDTO);
        categoria.setNegocio(negocio);

        Categoria guardada = categoriaRepository.save(categoria);
        return categoriaMapper.toDto(guardada);
    }

    @Override
    public CategoriaResponseDTO actualizarCategoria(Long negocioId, Long id, CategoriaRequestDTO requestDTO) {
        Categoria categoria = categoriaRepository.findByIdAndNegocioId(id, negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada en este negocio"));

        if (!categoria.getNombre().equalsIgnoreCase(requestDTO.getNombre()) &&
                categoriaRepository.existsByNombreIgnoreCaseAndNegocioId(requestDTO.getNombre(), negocioId)) {
            throw new RuntimeException("Ya existe otra categoría con ese nombre");
        }

        categoria.setNombre(requestDTO.getNombre());
        categoria.setDescripcion(requestDTO.getDescripcion());

        Categoria actualizada = categoriaRepository.save(categoria);
        return categoriaMapper.toDto(actualizada);
    }

    @Override
    public void eliminarCategoria(Long negocioId, Long id) {
        Categoria categoria = categoriaRepository.findByIdAndNegocioId(id, negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada en este negocio"));

        categoriaRepository.delete(categoria);
    }
}