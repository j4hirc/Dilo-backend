package com.example.dilo.DiloBackend.service.mapper;

import com.example.dilo.DiloBackend.dto.response.CuentaPorCobrarResponseDTO;
import com.example.dilo.DiloBackend.dto.response.CuotaResponseDTO;
import com.example.dilo.DiloBackend.dto.response.HistorialAbonoResponseDTO; // <-- Importación añadida
import com.example.dilo.DiloBackend.model.CuentasPorCobrar;
import com.example.dilo.DiloBackend.model.Cuota;
import org.springframework.stereotype.Component;

import java.util.ArrayList; // <-- Importación añadida
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

        if (cuenta.getFactura() != null && cuenta.getFactura().getCliente() != null) {
            var cli = cuenta.getFactura().getCliente();

            String primer = cli.getPrimerNombre() != null ? cli.getPrimerNombre().trim() : "";
            String apellido = cli.getApellidoPaterno() != null ? cli.getApellidoPaterno().trim() : "";
            String nombre = (primer + " " + apellido).trim();
            if (nombre.isEmpty()) {
                nombre = "Sin nombre";
            }

            String dni = cli.getDni() != null ? cli.getDni().trim() : "";
            if (!dni.isEmpty()) {
                dto.setNombreCliente(nombre + " (" + dni + ")");
            } else {
                dto.setNombreCliente(nombre);
            }
        } else {
            dto.setNombreCliente("Consumidor Final");
        }

        if (cuenta.getCuotas() != null && !cuenta.getCuotas().isEmpty()) {
            List<CuotaResponseDTO> cuotasDto = cuenta.getCuotas().stream()
                    .map(this::toCuotaDto)
                    .collect(Collectors.toList());
            dto.setCuotas(cuotasDto);
        }

        if (cuenta.getHistorialAbonos() != null && !cuenta.getHistorialAbonos().isEmpty()) {
            List<HistorialAbonoResponseDTO> abonosDto = cuenta.getHistorialAbonos().stream().map(abono -> {
                HistorialAbonoResponseDTO abonoDto = new HistorialAbonoResponseDTO();
                abonoDto.setId(abono.getId());
                abonoDto.setMontoAbonado(abono.getMontoAbonado());
                abonoDto.setFechaAbono(abono.getFechaAbono());
                abonoDto.setMetodoPago(abono.getMetodoPago());
                abonoDto.setReferencia(abono.getReferencia());
                abonoDto.setUsuarioRecibio(abono.getUsuarioRecibio());
                return abonoDto;
            }).collect(Collectors.toList());
            dto.setHistorialAbonos(abonosDto);
        } else {
            dto.setHistorialAbonos(new ArrayList<>());
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