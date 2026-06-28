package com.example.dilo.DiloBackend.service.mapper;

import com.example.dilo.DiloBackend.dto.request.DiloSpaceRequestDTO;
import com.example.dilo.DiloBackend.dto.response.DiloSpaceResponseDTO;
import com.example.dilo.DiloBackend.model.DiloSpace;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.ZoneId;

import java.time.LocalDateTime;

@Component
public class DiloSpaceMapper {

    private static final String CARACTERES_PERMITIDOS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int LONGITUD_CODIGO = 6;
    private final SecureRandom random = new SecureRandom();

    public DiloSpace toEntity(DiloSpaceRequestDTO dto) {
        DiloSpace entity = new DiloSpace();

        entity.setRuc(dto.getRuc());
        entity.setNombreComercial(dto.getNombreComercial());
        entity.setRazonSocial(dto.getRazonSocial());
        entity.setObligadoContabilidad(dto.getObligadoContabilidad());
        entity.setPasswordFirma(dto.getPasswordFirma());
        entity.setCodigoInvitacion(generarCodigoUnico());
        entity.setFechaCreacion(LocalDateTime.now(ZoneId.of("America/Chicago")));
        return entity;
    }

    public DiloSpaceResponseDTO toResponseDTO(DiloSpace entity) {
        DiloSpaceResponseDTO dto = new DiloSpaceResponseDTO();
        dto.setId(entity.getId());
        dto.setRuc(entity.getRuc());
        dto.setNombreComercial(entity.getNombreComercial());
        dto.setRazonSocial(entity.getRazonSocial());
        dto.setObligadoContabilidad(entity.getObligadoContabilidad());
        dto.setRutaFirma(entity.getRutaFirma());
        dto.setPasswordFirma(entity.getPasswordFirma());
        dto.setCodigoInvitacion(entity.getCodigoInvitacion());
        dto.setFechaCreacion(entity.getFechaCreacion());
        return dto;
    }



    private String generarCodigoUnico() {
        StringBuilder codigo = new StringBuilder(LONGITUD_CODIGO);
        for (int i = 0; i < LONGITUD_CODIGO; i++) {
            int indice = random.nextInt(CARACTERES_PERMITIDOS.length());
            codigo.append(CARACTERES_PERMITIDOS.charAt(indice));
        }
        return codigo.toString();
    }


}
