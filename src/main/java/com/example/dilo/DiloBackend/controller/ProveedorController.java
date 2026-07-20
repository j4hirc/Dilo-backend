package com.example.dilo.DiloBackend.controller;

import com.example.dilo.DiloBackend.dto.request.ProveedorRequestDTO;
import com.example.dilo.DiloBackend.dto.response.ProveedorResponseDTO;
import com.example.dilo.DiloBackend.service.ProveedorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/negocios/{negocioId}/proveedores")
@RequiredArgsConstructor
public class ProveedorController {

    private final ProveedorService proveedorService;

    @GetMapping
    @PreAuthorize("@seguridadNegocio.tieneRolEnNegocio(authentication, #negocioId, 'SUPER_ADMIN', 'PROPIETARIO', 'VENDEDOR', 'BODEGUERO')")
    public ResponseEntity<List<ProveedorResponseDTO>> obtenerProveedores(@PathVariable Long negocioId) {
        return ResponseEntity.ok(proveedorService.getAllByNegocio(negocioId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@seguridadNegocio.tieneRolEnNegocio(authentication, #negocioId, 'SUPER_ADMIN', 'PROPIETARIO', 'VENDEDOR', 'BODEGUERO')")
    public ResponseEntity<ProveedorResponseDTO> obtenerProveedorPorId(
            @PathVariable Long negocioId,
            @PathVariable Long id) {
        return ResponseEntity.ok(proveedorService.getById(id, negocioId));
    }

    @PostMapping
    @PreAuthorize("@seguridadNegocio.tieneRolEnNegocio(authentication, #negocioId, 'SUPER_ADMIN', 'PROPIETARIO', 'BODEGUERO')")
    public ResponseEntity<ProveedorResponseDTO> crearProveedor(
            @PathVariable Long negocioId,
            @Valid @RequestBody ProveedorRequestDTO requestDTO) {
        return new ResponseEntity<>(proveedorService.create(negocioId, requestDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@seguridadNegocio.tieneRolEnNegocio(authentication, #negocioId, 'SUPER_ADMIN', 'PROPIETARIO', 'BODEGUERO')")
    public ResponseEntity<ProveedorResponseDTO> actualizarProveedor(
            @PathVariable Long negocioId,
            @PathVariable Long id,
            @Valid @RequestBody ProveedorRequestDTO requestDTO) {
        return ResponseEntity.ok(proveedorService.update(id, negocioId, requestDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@seguridadNegocio.tieneRolEnNegocio(authentication, #negocioId, 'SUPER_ADMIN', 'PROPIETARIO')")
    public ResponseEntity<String> eliminarProveedor(
            @PathVariable Long negocioId,
            @PathVariable Long id) {
        proveedorService.delete(id, negocioId);
        return ResponseEntity.ok("Proveedor eliminado exitosamente");
    }
}