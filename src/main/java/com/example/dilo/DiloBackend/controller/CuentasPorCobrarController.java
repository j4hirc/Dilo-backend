package com.example.dilo.DiloBackend.controller;

import com.example.dilo.DiloBackend.dto.request.PagoCuotaRequestDTO;
import com.example.dilo.DiloBackend.dto.response.CuentaPorCobrarResponseDTO;
import com.example.dilo.DiloBackend.service.CuentaPorCobrarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/cuentas-por-cobrar")
@RequiredArgsConstructor
public class CuentasPorCobrarController {

    private final CuentaPorCobrarService cuentaPorCobrarService;

    @GetMapping("/negocio/{negocioId}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'PROPIETARIO', 'VENDEDOR')")
    public ResponseEntity<List<CuentaPorCobrarResponseDTO>> getCuentasPorNegocio(@PathVariable Long negocioId) {
        List<CuentaPorCobrarResponseDTO> response = cuentaPorCobrarService.listarPorNegocio(negocioId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'PROPIETARIO', 'VENDEDOR')")
    public ResponseEntity<CuentaPorCobrarResponseDTO> getDetalleCuenta(@PathVariable Long id) {
        CuentaPorCobrarResponseDTO response = cuentaPorCobrarService.obtenerDetalle(id);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/{id}/pagar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> registrarPago(@PathVariable Long id, @RequestBody Map<String, Object> payload) {

        BigDecimal montoPago = new BigDecimal(payload.get("montoPago").toString());
        String metodoPago = payload.get("metodoPago") != null ? payload.get("metodoPago").toString() : "EFECTIVO";
        String referencia = payload.get("referencia") != null ? payload.get("referencia").toString() : "";

        String usuarioLogueado = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();

        cuentaPorCobrarService.registrarPagoCuota(id, montoPago, metodoPago, referencia, usuarioLogueado);

        return ResponseEntity.ok("Pago registrado con éxito");
    }


}
