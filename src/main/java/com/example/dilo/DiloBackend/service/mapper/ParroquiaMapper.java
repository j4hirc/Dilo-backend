package com.example.dilo.DiloBackend.service.mapper;

import com.example.dilo.DiloBackend.dto.request.ParroquiaRequestDTO;
import com.example.dilo.DiloBackend.dto.response.ParroquiaResponseDTO;
import com.example.dilo.DiloBackend.model.Parroquia;
import org.springframework.stereotype.Component;

@Component
public class ParroquiaMapper {

    public ParroquiaResponseDTO toDto(Parroquia parroquia) {
        ParroquiaResponseDTO dto = new ParroquiaResponseDTO();
        dto.setId(parroquia.getId());
        dto.setNombre(parroquia.getNombre());
        return dto;
    }

    public Parroquia toEntity(ParroquiaRequestDTO dto) {
        Parroquia parroquia = new Parroquia();
        parroquia.setNombre(dto.getNombre());
        return parroquia;
    }

}