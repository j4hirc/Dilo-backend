package com.example.dilo.DiloBackend.controller;

import com.example.dilo.DiloBackend.dto.request.IvaRequestDTO;
import com.example.dilo.DiloBackend.service.ParametroGlobalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/parametros")
@RequiredArgsConstructor
public class ParametroGlobalController {

    private final ParametroGlobalService parametroService;

    @GetMapping("/iva")
    public ResponseEntity<Map<String, String>> obtenerIva() {
        String ivaActual = parametroService.obtenerIvaActual();
        return ResponseEntity.ok(Map.of("ivaActual", ivaActual));
    }

    @PutMapping("/iva")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<Map<String, String>> actualizarIva(@Valid @RequestBody IvaRequestDTO request) {

        parametroService.actualizarIva(request.getNuevoIva());

        return ResponseEntity.ok(Map.of(
                "mensaje", "El IVA ha sido actualizado exitosamente para todos los negocios del sistema.",
                "nuevoIva", request.getNuevoIva()
        ));
    }
}