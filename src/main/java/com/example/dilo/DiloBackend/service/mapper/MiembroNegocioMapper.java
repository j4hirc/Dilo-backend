package com.example.dilo.DiloBackend.service.mapper;

import com.example.dilo.DiloBackend.dto.request.MiembroNegocioRequestDTO;
import com.example.dilo.DiloBackend.dto.response.MiembroNegocioResponseDTO;
import com.example.dilo.DiloBackend.dto.response.NegocioResponseDTO;
import com.example.dilo.DiloBackend.model.MiembroNegocio;
import com.example.dilo.DiloBackend.model.Negocio;
import com.example.dilo.DiloBackend.model.Role;
import com.example.dilo.DiloBackend.model.Usuario;
import org.springframework.stereotype.Component;

@Component
public class MiembroNegocioMapper {

    public MiembroNegocioResponseDTO toDto(MiembroNegocio entity) {
        MiembroNegocioResponseDTO miembroNegocioResponseDTO = new MiembroNegocioResponseDTO();
        miembroNegocioResponseDTO.setId(entity.getId());
        miembroNegocioResponseDTO.setUsuarioId(entity.getUsuario().getId());
        miembroNegocioResponseDTO.setNombreUsuario(entity.getUsuario().getPrimerNombre() + " " + entity.getUsuario().getApellidoPaterno());
        miembroNegocioResponseDTO.setEmailUsuario(entity.getUsuario().getEmail());
        miembroNegocioResponseDTO.setRol(entity.getRol().getNombre());
        miembroNegocioResponseDTO.setFotoPerfil(entity.getUsuario().getFotoPerfil());
        miembroNegocioResponseDTO.setEstadoLaboral(entity.getEstadoLaboral());
        miembroNegocioResponseDTO.setEstadoInvitacion(entity.getEstadoInvitacion());
        miembroNegocioResponseDTO.setFechaVinculacion(entity.getFechaVinculacion());
        return miembroNegocioResponseDTO;
    }

    public MiembroNegocio toEntity(MiembroNegocioRequestDTO dto, Usuario usuario, Negocio negocio, Role rol) {
        MiembroNegocio entity = new MiembroNegocio();
        entity.setUsuario(usuario);
        entity.setNegocio(negocio);
        entity.setRol(rol);
        entity.setEstadoLaboral("Activo");
        entity.setEstadoInvitacion("Pendiente");
        return entity;
    }

}
