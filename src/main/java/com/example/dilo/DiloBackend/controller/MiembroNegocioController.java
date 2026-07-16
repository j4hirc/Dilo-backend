package com.example.dilo.DiloBackend.controller;

import com.example.dilo.DiloBackend.dto.request.MiembroNegocioRequestDTO;
import com.example.dilo.DiloBackend.dto.request.UnirseNegocioRequestDTO;
import com.example.dilo.DiloBackend.dto.response.MiembroNegocioResponseDTO;
import com.example.dilo.DiloBackend.service.MiembroNegocioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/negocios/{negocioId}/miembros")
@RequiredArgsConstructor
public class MiembroNegocioController {

    private final MiembroNegocioService miembroNegocioService;

    @GetMapping
    @PreAuthorize("@seguridadNegocio.tieneRolEnNegocio(authentication, #negocioId, 'PROPIETARIO', 'VENDEDOR', 'BODEGUERO')")
    public ResponseEntity<List<MiembroNegocioResponseDTO>> obtenerMiembros(@PathVariable Long negocioId) {
        List<MiembroNegocioResponseDTO> response = miembroNegocioService.obtenerMiembrosPorNegocio(negocioId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/invitar")
    @PreAuthorize("@seguridadNegocio.tieneRolEnNegocio(authentication, #negocioId, 'PROPIETARIO')")
    public ResponseEntity<MiembroNegocioResponseDTO> invitarMiembro(
            @PathVariable Long negocioId,
            @Valid @RequestBody MiembroNegocioRequestDTO requestDTO) {

        MiembroNegocioResponseDTO response = miembroNegocioService.invitarMiembro(negocioId, requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{miembroId}/responder")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MiembroNegocioResponseDTO> responderInvitacion(
            @PathVariable Long negocioId,
            @PathVariable Long miembroId,
            @RequestParam("aceptar") boolean aceptar) {

        MiembroNegocioResponseDTO response = miembroNegocioService.responderInvitacion(negocioId, miembroId, aceptar);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{miembroId}/desactivar")
    @PreAuthorize("@seguridadNegocio.tieneRolEnNegocio(authentication, #negocioId, 'PROPIETARIO')")
    public ResponseEntity<MiembroNegocioResponseDTO> desactivarMiembro(
            @PathVariable Long negocioId,
            @PathVariable Long miembroId) {

        MiembroNegocioResponseDTO response = miembroNegocioService.desactivarMiembro(negocioId, miembroId);
        return ResponseEntity.ok(response);
    }
}