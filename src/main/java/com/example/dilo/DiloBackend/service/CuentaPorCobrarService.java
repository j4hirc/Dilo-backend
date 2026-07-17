package com.example.dilo.DiloBackend.service;

import com.example.dilo.DiloBackend.dto.response.CuentaPorCobrarResponseDTO;
import com.example.dilo.DiloBackend.model.Factura;

import java.math.BigDecimal;
import java.util.List;

public interface CuentaPorCobrarService {
    void generarCuentaPorCobrar(Factura factura, int numeroCuotas);
    List<CuentaPorCobrarResponseDTO> listarPorNegocio(Long negocioId);
    void registrarPagoCuota(Long cuotaId, BigDecimal montoPago);
    CuentaPorCobrarResponseDTO obtenerDetalle(Long id);
}
