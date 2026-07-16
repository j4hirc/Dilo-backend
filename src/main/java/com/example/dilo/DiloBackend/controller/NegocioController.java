package com.example.dilo.DiloBackend.controller;

import com.example.dilo.DiloBackend.dto.request.NegocioRequestDTO;
import com.example.dilo.DiloBackend.dto.request.UnirseNegocioRequestDTO;
import com.example.dilo.DiloBackend.dto.response.MiembroNegocioResponseDTO;
import com.example.dilo.DiloBackend.dto.response.NegocioResponseDTO;
import com.example.dilo.DiloBackend.service.MiembroNegocioService;
import com.example.dilo.DiloBackend.service.NegocioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/negocios")
@RequiredArgsConstructor
public class NegocioController {

    private final NegocioService negocioService;
    private final MiembroNegocioService miembroNegocioService;

    // Solo un administrador general debería poder listar TODOS los negocios de la base de datos
    @GetMapping
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<List<NegocioResponseDTO>> getAllNegocios() {
        List<NegocioResponseDTO> response = negocioService.getAll();
        return ResponseEntity.ok(response);
    }

    // Cualquiera que sea parte del negocio (o un admin) debería poder ver sus detalles
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'PROPIETARIO', 'EMPLEADO')")
    public ResponseEntity<NegocioResponseDTO> getNegocioById(@PathVariable Long id) {
        NegocioResponseDTO response = negocioService.findById(id);
        return ResponseEntity.ok(response);
    }

    // Búsqueda habilitada para los roles principales
    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'PROPIETARIO', 'EMPLEADO')")
    public ResponseEntity<List<NegocioResponseDTO>> searchNegocios(@RequestParam("term") String term) {
        List<NegocioResponseDTO> response = negocioService.findByTerm(term);
        return ResponseEntity.ok(response);
    }

    // Cualquier usuario autenticado puede crear un negocio (por eso no lleva @PreAuthorize restrictivo).
    // Al crearlo, el servicio ya le asigna el rol 'PROPIETARIO' en la BD.
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<NegocioResponseDTO> crearNegocio(
            @RequestPart("datos") NegocioRequestDTO datosNegocio,
            @RequestPart(value = "firma", required = false) MultipartFile firma,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailUsuarioLogueado = authentication.getName();

        NegocioResponseDTO response = negocioService.createNegocio(datosNegocio, firma, imagen, emailUsuarioLogueado);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Solo el dueño del negocio o un admin pueden actualizar la información
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'PROPIETARIO')")
    public ResponseEntity<NegocioResponseDTO> actualizarNegocio(
            @PathVariable Long id,
            @RequestPart("datos") NegocioRequestDTO datosNegocio,
            @RequestPart(value = "firma", required = false) MultipartFile firma,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen) {

        NegocioResponseDTO response = negocioService.updateNegocio(id, datosNegocio, firma, imagen);
        return ResponseEntity.ok(response);
    }

    // Solo el dueño del negocio o un admin pueden eliminarlo
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'PROPIETARIO')")
    public ResponseEntity<Void> eliminarNegocio(@PathVariable Long id) {
        negocioService.deleteNegocio(id);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/unirse")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MiembroNegocioResponseDTO> unirseConCodigo(
            @Valid @RequestBody UnirseNegocioRequestDTO requestDTO) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailUsuarioLogueado = authentication.getName();

        MiembroNegocioResponseDTO response = miembroNegocioService.unirseConCodigo(requestDTO, emailUsuarioLogueado);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}