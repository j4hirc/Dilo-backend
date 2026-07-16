package com.example.dilo.DiloBackend.controller;

import com.example.dilo.DiloBackend.dto.request.TransaccionInventarioRequestDTO;
import com.example.dilo.DiloBackend.dto.response.TransaccionInventarioResponseDTO;
import com.example.dilo.DiloBackend.service.TransaccionInventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/negocios/{negocioId}/kardex")
@RequiredArgsConstructor
public class TransaccionInventarioController {

    private final TransaccionInventarioService transaccionService;

    @GetMapping
    @PreAuthorize("@seguridadNegocio.tieneRolEnNegocio(authentication, #negocioId, 'PROPIETARIO', 'VENDEDOR', 'BODEGUERO')")
    public ResponseEntity<List<TransaccionInventarioResponseDTO>> obtenerKardexGeneral(@PathVariable Long negocioId) {
        return ResponseEntity.ok(transaccionService.obtenerKardexGeneral(negocioId));
    }

    @GetMapping("/producto/{productoId}")
    @PreAuthorize("@seguridadNegocio.tieneRolEnNegocio(authentication, #negocioId, 'PROPIETARIO', 'VENDEDOR', 'BODEGUERO')")
    public ResponseEntity<List<TransaccionInventarioResponseDTO>> obtenerKardexPorProducto(
            @PathVariable Long negocioId,
            @PathVariable Long productoId) {
        return ResponseEntity.ok(transaccionService.obtenerKardexPorProducto(negocioId, productoId));
    }

    @PostMapping
    @PreAuthorize("@seguridadNegocio.tieneRolEnNegocio(authentication, #negocioId, 'PROPIETARIO', 'BODEGUERO')")
    public ResponseEntity<TransaccionInventarioResponseDTO> registrarTransaccion(
            @PathVariable Long negocioId,
            @Valid @RequestBody TransaccionInventarioRequestDTO requestDTO,
            Authentication authentication) {

        String emailUsuario = authentication.getName();

        TransaccionInventarioResponseDTO response = transaccionService.registrarTransaccion(negocioId, emailUsuario, requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}