package com.example.dilo.DiloBackend.service.implementation;

import com.example.dilo.DiloBackend.dto.response.AlertaCaducidadResponseDTO;
import com.example.dilo.DiloBackend.model.Lote;
import com.example.dilo.DiloBackend.repository.LoteRepository;
import com.example.dilo.DiloBackend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final LoteRepository loteRepository;

    @Override
    public List<AlertaCaducidadResponseDTO> obtenerAlertasCaducidad(Long negocioId, int diasAviso) {
        LocalDate hoy = LocalDate.now();
        // Calculamos la fecha límite (ejemplo: hoy + 30 días)
        LocalDate fechaLimite = hoy.plusDays(diasAviso);

        // Traemos de BD solo los lotes que vencen antes o igual a esa fecha límite
        List<Lote> lotesEnRiesgo = loteRepository.findLotesProximosAVencer(negocioId, fechaLimite);

        return lotesEnRiesgo.stream().map(lote -> {
            AlertaCaducidadResponseDTO dto = new AlertaCaducidadResponseDTO();
            dto.setLoteId(lote.getId());
            dto.setProductoNombre(lote.getProducto().getNombre());
            dto.setProductoCodigo(lote.getProducto().getCodigoPrincipal());
            dto.setBodegaNombre(lote.getBodega().getNombre());
            dto.setCantidadDisponible(lote.getCantidadDisponible());
            dto.setUnidadMedida(lote.getProducto().getUnidadMedida());
            dto.setFechaCaducidad(lote.getFechaCaducidad());

            // Calculamos exactamente los días que faltan
            long diasRestantes = ChronoUnit.DAYS.between(hoy, lote.getFechaCaducidad());
            dto.setDiasRestantes(diasRestantes);

            return dto;
        }).collect(Collectors.toList());
    }
}