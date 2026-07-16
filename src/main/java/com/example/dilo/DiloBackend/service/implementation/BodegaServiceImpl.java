package com.example.dilo.DiloBackend.service.implementation;

import com.example.dilo.DiloBackend.dto.request.BodegaRequestDTO;
import com.example.dilo.DiloBackend.dto.response.BodegaResponseDTO;
import com.example.dilo.DiloBackend.exception.ResourceNotFoundException;
import com.example.dilo.DiloBackend.model.Bodega;
import com.example.dilo.DiloBackend.model.Negocio;
import com.example.dilo.DiloBackend.repository.BodegaRepository;
import com.example.dilo.DiloBackend.repository.NegocioRepository;
import com.example.dilo.DiloBackend.service.BodegaService;
import com.example.dilo.DiloBackend.service.mapper.BodegaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BodegaServiceImpl implements BodegaService {

    private final BodegaRepository bodegaRepository;
    private final NegocioRepository negocioRepository;
    private final BodegaMapper bodegaMapper;

    @Override
    public List<BodegaResponseDTO> obtenerPorNegocio(Long negocioId) {
        return bodegaRepository.findByNegocioId(negocioId).stream()
                .map(bodegaMapper::toDto)
                .toList();
    }

    @Override
    public BodegaResponseDTO obtenerPorId(Long negocioId, Long id) {
        Bodega bodega = bodegaRepository.findByIdAndNegocioId(id, negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Bodega no encontrada en este negocio"));
        return bodegaMapper.toDto(bodega);
    }

    @Override
    public BodegaResponseDTO crearBodega(Long negocioId, BodegaRequestDTO requestDTO) {
        Negocio negocio = negocioRepository.findById(negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Negocio no encontrado"));

        if (bodegaRepository.existsByNombreIgnoreCaseAndNegocioId(requestDTO.getNombre(), negocioId)) {
            throw new RuntimeException("Ya existe una bodega con ese nombre en este negocio");
        }

        Bodega bodega = bodegaMapper.toEntity(requestDTO);
        bodega.setNegocio(negocio);

        Bodega guardada = bodegaRepository.save(bodega);
        return bodegaMapper.toDto(guardada);
    }

    @Override
    public BodegaResponseDTO actualizarBodega(Long negocioId, Long id, BodegaRequestDTO requestDTO) {
        Bodega bodega = bodegaRepository.findByIdAndNegocioId(id, negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Bodega no encontrada en este negocio"));

        if (!bodega.getNombre().equalsIgnoreCase(requestDTO.getNombre()) &&
                bodegaRepository.existsByNombreIgnoreCaseAndNegocioId(requestDTO.getNombre(), negocioId)) {
            throw new RuntimeException("Ya existe otra bodega con ese nombre");
        }

        bodega.setNombre(requestDTO.getNombre());
        bodega.setDireccion(requestDTO.getDireccion());

        Bodega actualizada = bodegaRepository.save(bodega);
        return bodegaMapper.toDto(actualizada);
    }

    @Override
    public void eliminarBodega(Long negocioId, Long id) {
        Bodega bodega = bodegaRepository.findByIdAndNegocioId(id, negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Bodega no encontrada en este negocio"));

        bodegaRepository.delete(bodega);
    }

    @Override
    public List<BodegaResponseDTO> buscarPorTermino(Long negocioId, String term) {
        List<Bodega> bodegas = bodegaRepository.buscarPorTermino(negocioId, term);
        if (bodegas.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron bodegas con el término: " + term);
        }
        return bodegas.stream()
                .map(bodegaMapper::toDto)
                .toList();
    }
}