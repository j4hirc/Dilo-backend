package com.example.dilo.DiloBackend.controller;

import com.example.dilo.DiloBackend.dto.request.CompraRequestDTO;
import com.example.dilo.DiloBackend.dto.response.CompraResponseDTO;
import com.example.dilo.DiloBackend.service.CompraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/negocios/{negocioId}/compras")
@RequiredArgsConstructor
public class CompraController {

    private final CompraService compraService;

    @PostMapping
    @PreAuthorize("@seguridadNegocio.tieneRolEnNegocio(authentication, #negocioId, 'SUPER_ADMIN', 'PROPIETARIO', 'BODEGUERO')")
    public ResponseEntity<CompraResponseDTO> registrarCompra(
            @PathVariable Long negocioId,
            Authentication authentication, // Extraemos la sesión actual de Spring Security
            @Valid @RequestBody CompraRequestDTO requestDTO) {

        // Obtenemos el email del usuario logueado directamente del token JWT
        String emailUsuario = authentication.getName();

        CompraResponseDTO compraRegistrada = compraService.registrarCompra(negocioId, emailUsuario, requestDTO);
        return new ResponseEntity<>(compraRegistrada, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("@seguridadNegocio.tieneRolEnNegocio(authentication, #negocioId, 'SUPER_ADMIN', 'PROPIETARIO', 'BODEGUERO')")
    public ResponseEntity<List<CompraResponseDTO>> obtenerCompras(@PathVariable Long negocioId) {
        return ResponseEntity.ok(compraService.obtenerComprasPorNegocio(negocioId));
    }
}