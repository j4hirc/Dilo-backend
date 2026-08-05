package com.example.dilo.DiloBackend.controller;

import com.example.dilo.DiloBackend.dto.response.AlertaCaducidadResponseDTO;
import com.example.dilo.DiloBackend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/negocios/{negocioId}/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/alertas-caducidad")
    @PreAuthorize("@seguridadNegocio.tieneRolEnNegocio(authentication, #negocioId, 'SUPER_ADMIN', 'PROPIETARIO', 'BODEGUERO')")
    public ResponseEntity<List<AlertaCaducidadResponseDTO>> obtenerAlertasCaducidad(
            @PathVariable Long negocioId,
            @RequestParam(defaultValue = "30") int dias) {

        return ResponseEntity.ok(dashboardService.obtenerAlertasCaducidad(negocioId, dias));
    }
}