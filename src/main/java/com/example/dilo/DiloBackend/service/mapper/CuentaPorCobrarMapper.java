package com.example.dilo.DiloBackend.service.mapper;

import com.example.dilo.DiloBackend.dto.response.CuentaPorCobrarResponseDTO;
import com.example.dilo.DiloBackend.dto.response.CuotaResponseDTO;
import com.example.dilo.DiloBackend.model.CuentasPorCobrar;
import com.example.dilo.DiloBackend.model.Cuota;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CuentaPorCobrarMapper {

    public CuentaPorCobrarResponseDTO toDto(CuentasPorCobrar cuenta) {
        CuentaPorCobrarResponseDTO dto = new CuentaPorCobrarResponseDTO();
        dto.setId(cuenta.getId());

        if (cuenta.getFactura() != null) {
            dto.setFacturaId(cuenta.getFactura().getId());
            dto.setNumeroFactura(cuenta.getFactura().getNumeroFactura());
        }

        dto.setFechaVencimiento(cuenta.getFechaVencimiento());
        dto.setMontoTotal(cuenta.getMontoTotal());
        dto.setSaldoPendiente(cuenta.getSaldoPendiente());
        dto.setEstado(cuenta.getEstado());

        if (cuenta.getCuotas() != null && !cuenta.getCuotas().isEmpty()) {
            List<CuotaResponseDTO> cuotasDto = cuenta.getCuotas().stream()
                    .map(this::toCuotaDto)
                    .collect(Collectors.toList());
            dto.setCuotas(cuotasDto);
        }

        return dto;
    }

    private CuotaResponseDTO toCuotaDto(Cuota cuota) {
        CuotaResponseDTO dto = new CuotaResponseDTO();
        dto.setId(cuota.getId());
        dto.setNumeroCuota(cuota.getNumeroCuota());
        dto.setMontoCuota(cuota.getMontoCuota());
        dto.setSaldoPendienteCuota(cuota.getSaldoPendienteCuota());
        dto.setFechaVencimiento(cuota.getFechaVencimiento());
        dto.setEstado(cuota.getEstado());
        return dto;
    }
}