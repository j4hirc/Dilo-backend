package com.example.dilo.DiloBackend.controller;

import com.example.dilo.DiloBackend.dto.request.ClienteRequestDTO;
import com.example.dilo.DiloBackend.dto.response.ClienteResponseDTO;
import com.example.dilo.DiloBackend.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/negocios/{negocioId}/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    @PreAuthorize("@seguridadNegocio.tieneRolEnNegocio(authentication, #negocioId, 'PROPIETARIO', 'VENDEDOR')")
    public ResponseEntity<List<ClienteResponseDTO>> obtenerClientes(@PathVariable Long negocioId) {
        return ResponseEntity.ok(clienteService.obtenerPorNegocio(negocioId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@seguridadNegocio.tieneRolEnNegocio(authentication, #negocioId, 'PROPIETARIO', 'VENDEDOR')")
    public ResponseEntity<ClienteResponseDTO> obtenerClientePorId(
            @PathVariable Long negocioId,
            @PathVariable Long id) {
        return ResponseEntity.ok(clienteService.obtenerPorId(negocioId, id));
    }

    @GetMapping("/search")
    @PreAuthorize("@seguridadNegocio.tieneRolEnNegocio(authentication, #negocioId, 'PROPIETARIO', 'VENDEDOR')")
    public ResponseEntity<List<ClienteResponseDTO>> buscarClientes(
            @PathVariable Long negocioId,
            @RequestParam("term") String term) {
        return ResponseEntity.ok(clienteService.buscarPorTermino(negocioId, term));
    }

    @PostMapping
    @PreAuthorize("@seguridadNegocio.tieneRolEnNegocio(authentication, #negocioId, 'PROPIETARIO', 'VENDEDOR')")
    public ResponseEntity<ClienteResponseDTO> crearCliente(
            @PathVariable Long negocioId,
            @Valid @RequestBody ClienteRequestDTO requestDTO) {
        return new ResponseEntity<>(clienteService.crearCliente(negocioId, requestDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@seguridadNegocio.tieneRolEnNegocio(authentication, #negocioId, 'PROPIETARIO', 'VENDEDOR')")
    public ResponseEntity<ClienteResponseDTO> actualizarCliente(
            @PathVariable Long negocioId,
            @PathVariable Long id,
            @Valid @RequestBody ClienteRequestDTO requestDTO) {
        return ResponseEntity.ok(clienteService.actualizarCliente(negocioId, id, requestDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@seguridadNegocio.tieneRolEnNegocio(authentication, #negocioId, 'PROPIETARIO')")
    public ResponseEntity<Void> eliminarCliente(
            @PathVariable Long negocioId,
            @PathVariable Long id) {
        clienteService.eliminarCliente(negocioId, id);
        return ResponseEntity.noContent().build();
    }
}