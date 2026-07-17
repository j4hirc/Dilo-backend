package com.example.dilo.DiloBackend.controller;

import com.example.dilo.DiloBackend.dto.request.PagoCuotaRequestDTO;
import com.example.dilo.DiloBackend.dto.response.CuentaPorCobrarResponseDTO;
import com.example.dilo.DiloBackend.service.CuentaPorCobrarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cuentas-por-cobrar")
@RequiredArgsConstructor
public class CuentasPorCobrarController {

    private final CuentaPorCobrarService cuentaPorCobrarService;

    @GetMapping("/negocio/{negocioId}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'PROPIETARIO', 'EMPLEADO')")
    public ResponseEntity<List<CuentaPorCobrarResponseDTO>> getCuentasPorNegocio(@PathVariable Long negocioId) {
        List<CuentaPorCobrarResponseDTO> response = cuentaPorCobrarService.listarPorNegocio(negocioId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'PROPIETARIO', 'EMPLEADO')")
    public ResponseEntity<CuentaPorCobrarResponseDTO> getDetalleCuenta(@PathVariable Long id) {
        CuentaPorCobrarResponseDTO response = cuentaPorCobrarService.obtenerDetalle(id);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/cuotas/{cuotaId}/pagar")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'PROPIETARIO', 'EMPLEADO')")
    public ResponseEntity<String> pagarCuota(
            @PathVariable Long cuotaId,
            @Valid @RequestBody PagoCuotaRequestDTO request) {

        cuentaPorCobrarService.registrarPagoCuota(cuotaId, request.getMontoPago());

        return ResponseEntity.ok("Pago registrado exitosamente");
    }


}