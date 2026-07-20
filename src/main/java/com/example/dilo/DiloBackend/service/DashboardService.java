package com.example.dilo.DiloBackend.service;

import com.example.dilo.DiloBackend.dto.response.AlertaCaducidadResponseDTO;
import java.util.List;

public interface DashboardService {
    List<AlertaCaducidadResponseDTO> obtenerAlertasCaducidad(Long negocioId, int diasAviso);
}