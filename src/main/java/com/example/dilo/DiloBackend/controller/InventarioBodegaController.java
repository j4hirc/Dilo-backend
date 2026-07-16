package com.example.dilo.DiloBackend.controller;

import com.example.dilo.DiloBackend.dto.request.InventarioBodegaRequestDTO;
import com.example.dilo.DiloBackend.dto.response.InventarioBodegaResponseDTO;
import com.example.dilo.DiloBackend.service.InventarioBodegaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/negocios/{negocioId}/inventario")
@RequiredArgsConstructor
public class InventarioBodegaController {

    private final InventarioBodegaService inventarioService;

    @GetMapping
    @PreAuthorize("@seguridadNegocio.tieneRolEnNegocio(authentication, #negocioId, 'PROPIETARIO', 'VENDEDOR', 'BODEGUERO')")
    public ResponseEntity<List<InventarioBodegaResponseDTO>> obtenerInventarioGeneral(@PathVariable Long negocioId) {
        return ResponseEntity.ok(inventarioService.obtenerInventarioGeneral(negocioId));
    }

    @GetMapping("/bodega/{bodegaId}")
    @PreAuthorize("@seguridadNegocio.tieneRolEnNegocio(authentication, #negocioId, 'PROPIETARIO', 'VENDEDOR', 'BODEGUERO')")
    public ResponseEntity<List<InventarioBodegaResponseDTO>> obtenerInventarioPorBodega(
            @PathVariable Long negocioId,
            @PathVariable Long bodegaId) {
        return ResponseEntity.ok(inventarioService.obtenerInventarioPorBodega(negocioId, bodegaId));
    }

    @PostMapping
    @PreAuthorize("@seguridadNegocio.tieneRolEnNegocio(authentication, #negocioId, 'PROPIETARIO', 'BODEGUERO')")
    public ResponseEntity<InventarioBodegaResponseDTO> inicializarInventario(
            @PathVariable Long negocioId,
            @Valid @RequestBody InventarioBodegaRequestDTO requestDTO) {
        return new ResponseEntity<>(inventarioService.inicializarInventario(negocioId, requestDTO), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/stock-minimo")
    @PreAuthorize("@seguridadNegocio.tieneRolEnNegocio(authentication, #negocioId, 'PROPIETARIO', 'BODEGUERO')")
    public ResponseEntity<InventarioBodegaResponseDTO> actualizarStockMinimo(
            @PathVariable Long negocioId,
            @PathVariable Long id,
            @RequestParam("valor") Integer nuevoStockMinimo) {
        return ResponseEntity.ok(inventarioService.actualizarStockMinimo(negocioId, id, nuevoStockMinimo));
    }

    @PatchMapping("/{id}/cantidad-actual")
    @PreAuthorize("@seguridadNegocio.tieneRolEnNegocio(authentication, #negocioId, 'PROPIETARIO', 'BODEGUERO')")
    public ResponseEntity<InventarioBodegaResponseDTO> actualizarCantidadActual(
            @PathVariable Long negocioId,
            @PathVariable Long id,
            @RequestParam("valor") Integer nuevaCantidad) {
        return ResponseEntity.ok(inventarioService.actualizarCantidadActual(negocioId, id, nuevaCantidad));
    }
}