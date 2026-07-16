package com.example.dilo.DiloBackend.controller;

import com.example.dilo.DiloBackend.dto.request.CategoriaRequestDTO;
import com.example.dilo.DiloBackend.dto.response.CategoriaResponseDTO;
import com.example.dilo.DiloBackend.service.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/negocios/{negocioId}/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    @GetMapping
    @PreAuthorize("@seguridadNegocio.tieneRolEnNegocio(authentication, #negocioId, 'PROPIETARIO', 'VENDEDOR', 'BODEGUERO')")
    public ResponseEntity<List<CategoriaResponseDTO>> obtenerCategorias(@PathVariable Long negocioId) {
        return ResponseEntity.ok(categoriaService.obtenerPorNegocio(negocioId));
    }

    @PostMapping
    @PreAuthorize("@seguridadNegocio.tieneRolEnNegocio(authentication, #negocioId, 'PROPIETARIO', 'BODEGUERO')")
    public ResponseEntity<CategoriaResponseDTO> crearCategoria(
            @PathVariable Long negocioId,
            @Valid @RequestBody CategoriaRequestDTO requestDTO) {
        return new ResponseEntity<>(categoriaService.crearCategoria(negocioId, requestDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@seguridadNegocio.tieneRolEnNegocio(authentication, #negocioId, 'PROPIETARIO', 'BODEGUERO')")
    public ResponseEntity<CategoriaResponseDTO> actualizarCategoria(
            @PathVariable Long negocioId,
            @PathVariable Long id,
            @Valid @RequestBody CategoriaRequestDTO requestDTO) {
        return ResponseEntity.ok(categoriaService.actualizarCategoria(negocioId, id, requestDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@seguridadNegocio.tieneRolEnNegocio(authentication, #negocioId, 'PROPIETARIO')")
    public ResponseEntity<String> eliminarCategoria(
            @PathVariable Long negocioId,
            @PathVariable Long id) {
        categoriaService.eliminarCategoria(negocioId, id);
        return ResponseEntity.ok("Categoría eliminada exitosamente");
    }
}