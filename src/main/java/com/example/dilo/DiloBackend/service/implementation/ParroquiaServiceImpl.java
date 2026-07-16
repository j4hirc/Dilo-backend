package com.example.dilo.DiloBackend.service.implementation;

import com.example.dilo.DiloBackend.dto.request.ParroquiaRequestDTO;
import com.example.dilo.DiloBackend.dto.response.ParroquiaResponseDTO;
import com.example.dilo.DiloBackend.exception.ResourceNotFoundException;
import com.example.dilo.DiloBackend.model.Parroquia;
import com.example.dilo.DiloBackend.repository.ParroquiaRepository;
import com.example.dilo.DiloBackend.service.ParroquiaService;
import com.example.dilo.DiloBackend.service.mapper.ParroquiaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParroquiaServiceImpl implements ParroquiaService {

    private final ParroquiaRepository parroquiaRepository;
    private final ParroquiaMapper parroquiaMapper;

    @Override
    public List<ParroquiaResponseDTO> obtenerTodas() {
        return parroquiaRepository.findAllByOrderByNombreAsc().stream()
                .map(parroquiaMapper::toDto)
                .toList();
    }

    @Override
    public ParroquiaResponseDTO obtenerPorId(Long id) {
        Parroquia parroquia = parroquiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parroquia no encontrada"));
        return parroquiaMapper.toDto(parroquia);
    }

    @Override
    public ParroquiaResponseDTO crearParroquia(ParroquiaRequestDTO requestDTO) {
        if (parroquiaRepository.existsByNombreIgnoreCase(requestDTO.getNombre())) {
            throw new RuntimeException("Ya existe una parroquia con el nombre: " + requestDTO.getNombre());
        }

        Parroquia parroquia = parroquiaMapper.toEntity(requestDTO);
        Parroquia guardada = parroquiaRepository.save(parroquia);
        return parroquiaMapper.toDto(guardada);
    }

    @Override
    public ParroquiaResponseDTO actualizarParroquia(Long id, ParroquiaRequestDTO requestDTO) {
        Parroquia parroquia = parroquiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parroquia no encontrada"));

        if (!parroquia.getNombre().equalsIgnoreCase(requestDTO.getNombre()) &&
                parroquiaRepository.existsByNombreIgnoreCase(requestDTO.getNombre())) {
            throw new RuntimeException("Ya existe otra parroquia con el nombre: " + requestDTO.getNombre());
        }

        parroquia.setNombre(requestDTO.getNombre());
        Parroquia actualizada = parroquiaRepository.save(parroquia);
        return parroquiaMapper.toDto(actualizada);
    }

    @Override
    public void eliminarParroquia(Long id) {
        Parroquia parroquia = parroquiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parroquia no encontrada"));

        parroquiaRepository.delete(parroquia);
    }
}