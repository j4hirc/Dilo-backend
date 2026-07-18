package com.example.dilo.DiloBackend.controller;

import com.example.dilo.DiloBackend.dto.request.ParroquiaRequestDTO;
import com.example.dilo.DiloBackend.dto.response.ParroquiaResponseDTO;
import com.example.dilo.DiloBackend.service.ParroquiaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/parroquias")
@RequiredArgsConstructor
public class ParroquiaController {

    private final ParroquiaService parroquiaService;

    @GetMapping
    public ResponseEntity<List<ParroquiaResponseDTO>> obtenerParroquias() {
        return ResponseEntity.ok(parroquiaService.obtenerTodas());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ParroquiaResponseDTO> obtenerParroquiaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(parroquiaService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ParroquiaResponseDTO> crearParroquia(@Valid @RequestBody ParroquiaRequestDTO requestDTO) {
        return new ResponseEntity<>(parroquiaService.crearParroquia(requestDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ParroquiaResponseDTO> actualizarParroquia(
            @PathVariable Long id,
            @Valid @RequestBody ParroquiaRequestDTO requestDTO) {
        return ResponseEntity.ok(parroquiaService.actualizarParroquia(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> eliminarParroquia(@PathVariable Long id) {
        parroquiaService.eliminarParroquia(id);
        return ResponseEntity.noContent().build();
    }
}