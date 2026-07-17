package com.example.dilo.DiloBackend.controller;

import com.example.dilo.DiloBackend.dto.request.FacturaRequestDTO;
import com.example.dilo.DiloBackend.dto.response.FacturaResponseDTO;
import com.example.dilo.DiloBackend.service.FacturaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/negocios/{negocioId}/facturas")
@RequiredArgsConstructor
public class FacturaController {

    private final FacturaService facturaService;

    @PostMapping
    public ResponseEntity<FacturaResponseDTO> generarFactura(
            @PathVariable Long negocioId,
            @Valid @RequestBody FacturaRequestDTO requestDTO,
            Authentication authentication) {

        String emailUsuario = authentication.getName();

        FacturaResponseDTO response = facturaService.generarFactura(negocioId, emailUsuario, requestDTO);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}