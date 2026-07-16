package com.example.dilo.DiloBackend.service.mapper;

import com.example.dilo.DiloBackend.dto.request.BodegaRequestDTO;
import com.example.dilo.DiloBackend.dto.response.BodegaResponseDTO;
import com.example.dilo.DiloBackend.model.Bodega;
import org.springframework.stereotype.Component;

@Component
public class BodegaMapper {

    public BodegaResponseDTO toDto(Bodega bodega) {
        BodegaResponseDTO dto = new BodegaResponseDTO();
        dto.setId(bodega.getId());
        dto.setNombre(bodega.getNombre());
        dto.setDireccion(bodega.getDireccion());
        return dto;
    }

    public Bodega toEntity(BodegaRequestDTO dto) {
        Bodega bodega = new Bodega();
        bodega.setNombre(dto.getNombre());
        bodega.setDireccion(dto.getDireccion());
        return bodega;
    }
}
