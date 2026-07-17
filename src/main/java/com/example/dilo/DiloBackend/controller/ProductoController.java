package com.example.dilo.DiloBackend.controller;

import com.example.dilo.DiloBackend.dto.request.ProductoRequestDTO;
import com.example.dilo.DiloBackend.dto.response.ProductoResponseDTO;
import com.example.dilo.DiloBackend.model.Producto;
import com.example.dilo.DiloBackend.repository.ProductoRepository;
import com.example.dilo.DiloBackend.service.ProductoService;
import com.example.dilo.DiloBackend.service.implementation.NlpService;
import com.example.dilo.DiloBackend.service.mapper.ProductoMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/negocios/{negocioId}/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;
    private final NlpService nlpService;

    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;


    @GetMapping
    @PreAuthorize("@seguridadNegocio.tieneRolEnNegocio(authentication, #negocioId, 'PROPIETARIO', 'VENDEDOR', 'BODEGUERO')")
    public ResponseEntity<List<ProductoResponseDTO>> obtenerProductos(@PathVariable Long negocioId) {
        return ResponseEntity.ok(productoService.obtenerPorNegocio(negocioId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@seguridadNegocio.tieneRolEnNegocio(authentication, #negocioId, 'PROPIETARIO', 'VENDEDOR', 'BODEGUERO')")
    public ResponseEntity<ProductoResponseDTO> obtenerProductoPorId(
            @PathVariable Long negocioId,
            @PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerPorId(negocioId, id));
    }

    @GetMapping("/search")
    @PreAuthorize("@seguridadNegocio.tieneRolEnNegocio(authentication, #negocioId, 'PROPIETARIO', 'VENDEDOR', 'BODEGUERO')")
    public ResponseEntity<List<ProductoResponseDTO>> buscarProductos(
            @PathVariable Long negocioId,
            @RequestParam("term") String term) {

        List<ProductoResponseDTO> response = productoService.buscarPorTermino(negocioId, term);
        return ResponseEntity.ok(response);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@seguridadNegocio.tieneRolEnNegocio(authentication, #negocioId, 'PROPIETARIO', 'BODEGUERO')")
    public ResponseEntity<ProductoResponseDTO> crearProducto(
            @PathVariable Long negocioId,
            @RequestPart("datos") @Valid ProductoRequestDTO requestDTO,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen) {

        ProductoResponseDTO response = productoService.crearProducto(negocioId, requestDTO, imagen);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@seguridadNegocio.tieneRolEnNegocio(authentication, #negocioId, 'PROPIETARIO', 'BODEGUERO')")
    public ResponseEntity<ProductoResponseDTO> actualizarProducto(
            @PathVariable Long negocioId,
            @PathVariable Long id,
            @RequestPart("datos") @Valid ProductoRequestDTO requestDTO,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen) {

        return ResponseEntity.ok(productoService.actualizarProducto(negocioId, id, requestDTO, imagen));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@seguridadNegocio.tieneRolEnNegocio(authentication, #negocioId, 'PROPIETARIO')")
    public ResponseEntity<Void> eliminarProducto(
            @PathVariable Long negocioId,
            @PathVariable Long id) {

        productoService.eliminarProducto(negocioId, id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/buscar-voz")
    @PreAuthorize("@seguridadNegocio.tieneRolEnNegocio(authentication, #negocioId, 'PROPIETARIO', 'VENDEDOR', 'BODEGUERO')")
    public ResponseEntity<List<ProductoResponseDTO>> buscarProductoPorVoz(
            @PathVariable Long negocioId,
            @RequestParam("q") String textoDictado) {

        String entidadLimpia = nlpService.limpiarComandoVoz(textoDictado);
        System.out.println("🎙️ Voz original: " + textoDictado);
        System.out.println("🤖 Entidad extraída por PLN: " + entidadLimpia);

        List<Producto> productos = productoRepository.buscarPorVoz(negocioId, entidadLimpia);

        List<ProductoResponseDTO> response = productos.stream()
                .map(productoMapper::toDto)
                .toList();

        return ResponseEntity.ok(response);
    }

}