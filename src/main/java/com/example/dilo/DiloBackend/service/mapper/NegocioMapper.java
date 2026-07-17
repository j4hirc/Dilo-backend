package com.example.dilo.DiloBackend.service.mapper;

import com.example.dilo.DiloBackend.dto.request.NegocioRequestDTO;
import com.example.dilo.DiloBackend.dto.response.NegocioResponseDTO;
import com.example.dilo.DiloBackend.model.Negocio;
import org.springframework.stereotype.Component;

@Component
public class NegocioMapper {

    public Negocio toEntity(NegocioRequestDTO negocio) {
        Negocio entity = new Negocio();
        entity.setRuc(negocio.getRuc());
        entity.setNombreComercial(negocio.getNombreComercial());
        entity.setRazonSocial(negocio.getRazonSocial());
        entity.setDireccion(negocio.getDireccion());
        entity.setObligadoContabilidad(negocio.getObligadoContabilidad());
        return entity;
    }

    public NegocioResponseDTO toDto(Negocio entity) {
        NegocioResponseDTO negocioResponseDTO = new NegocioResponseDTO();
        negocioResponseDTO.setIdNegocio(entity.getId());
        negocioResponseDTO.setRuc(entity.getRuc());
        negocioResponseDTO.setNombreComercial(entity.getNombreComercial());
        negocioResponseDTO.setRazonSocial(entity.getRazonSocial());
        negocioResponseDTO.setDireccion(entity.getDireccion());
        negocioResponseDTO.setObligadoContabilidad(entity.getObligadoContabilidad());
        negocioResponseDTO.setCodigoInvitacion(entity.getCodigoInvitacion());
        negocioResponseDTO.setRutaImagen(entity.getRutaImagen());
        return negocioResponseDTO;
    }

}