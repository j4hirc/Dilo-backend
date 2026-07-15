package com.example.dilo.DiloBackend.service;

import com.example.dilo.DiloBackend.dto.request.MiembroNegocioRequestDTO;
import com.example.dilo.DiloBackend.dto.request.UnirseNegocioRequestDTO;
import com.example.dilo.DiloBackend.dto.response.MiembroNegocioResponseDTO;

import java.util.List;

public interface MiembroNegocioService {
    List<MiembroNegocioResponseDTO> obtenerMiembrosPorNegocio(Long negocioId);

    MiembroNegocioResponseDTO invitarMiembro(Long negocioId, MiembroNegocioRequestDTO requestDTO);

    MiembroNegocioResponseDTO responderInvitacion(Long negocioId, Long miembroId, boolean aceptar);

    void eliminarMiembro(Long negocioId, Long miembroId);

    MiembroNegocioResponseDTO unirseConCodigo(UnirseNegocioRequestDTO requestDTO, String emailUsuario);
}