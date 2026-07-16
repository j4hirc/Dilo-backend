package com.example.dilo.DiloBackend.controller;

import com.example.dilo.DiloBackend.dto.request.BodegaRequestDTO;
import com.example.dilo.DiloBackend.dto.response.BodegaResponseDTO;
import com.example.dilo.DiloBackend.service.BodegaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/negocios/{negocioId}/bodegas")
@RequiredArgsConstructor
public class BodegaController {

    private final BodegaService bodegaService;

    @GetMapping
    @PreAuthorize("@seguridadNegocio.tieneRolEnNegocio(authentication, #negocioId, 'PROPIETARIO', 'VENDEDOR', 'BODEGUERO')")
    public ResponseEntity<List<BodegaResponseDTO>> obtenerBodegas(@PathVariable Long negocioId) {
        return ResponseEntity.ok(bodegaService.obtenerPorNegocio(negocioId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@seguridadNegocio.tieneRolEnNegocio(authentication, #negocioId, 'PROPIETARIO', 'VENDEDOR', 'BODEGUERO')")
    public ResponseEntity<BodegaResponseDTO> obtenerBodegaPorId(
            @PathVariable Long negocioId,
            @PathVariable Long id) {
        return ResponseEntity.ok(bodegaService.obtenerPorId(negocioId, id));
    }

    @GetMapping("/search")
    @PreAuthorize("@seguridadNegocio.tieneRolEnNegocio(authentication, #negocioId, 'PROPIETARIO', 'VENDEDOR', 'BODEGUERO')")
    public ResponseEntity<List<BodegaResponseDTO>> buscarBodegas(
            @PathVariable Long negocioId,
            @RequestParam("term") String term) {
        return ResponseEntity.ok(bodegaService.buscarPorTermino(negocioId, term));
    }

    @PostMapping
    @PreAuthorize("@seguridadNegocio.tieneRolEnNegocio(authentication, #negocioId, 'PROPIETARIO')")
    public ResponseEntity<BodegaResponseDTO> crearBodega(
            @PathVariable Long negocioId,
            @Valid @RequestBody BodegaRequestDTO requestDTO) {
        return new ResponseEntity<>(bodegaService.crearBodega(negocioId, requestDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@seguridadNegocio.tieneRolEnNegocio(authentication, #negocioId, 'PROPIETARIO')")
    public ResponseEntity<BodegaResponseDTO> actualizarBodega(
            @PathVariable Long negocioId,
            @PathVariable Long id,
            @Valid @RequestBody BodegaRequestDTO requestDTO) {
        return ResponseEntity.ok(bodegaService.actualizarBodega(negocioId, id, requestDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@seguridadNegocio.tieneRolEnNegocio(authentication, #negocioId, 'PROPIETARIO')")
    public ResponseEntity<Void> eliminarBodega(
            @PathVariable Long negocioId,
            @PathVariable Long id) {
        bodegaService.eliminarBodega(negocioId, id);
        return ResponseEntity.noContent().build();
    }
}