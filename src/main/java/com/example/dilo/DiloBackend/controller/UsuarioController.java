package com.example.dilo.DiloBackend.controller;

import com.example.dilo.DiloBackend.dto.request.UpdateUsuarioDTO;
import com.example.dilo.DiloBackend.dto.response.UsuarioResponseDTO;
import com.example.dilo.DiloBackend.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UsuarioResponseDTO> obtenerMiPerfil(Authentication authentication) {
        String emailLogueado = authentication.getName();
        UsuarioResponseDTO response = usuarioService.obtenerMiPerfil(emailLogueado);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UsuarioResponseDTO> actualizarMiPerfil(
            Authentication authentication,
            @RequestPart("datos") UpdateUsuarioDTO datosUsuario,
            @RequestPart(value = "foto", required = false) MultipartFile foto) {

        String emailLogueado = authentication.getName();
        UsuarioResponseDTO response = usuarioService.actualizarMiPerfil(emailLogueado, datosUsuario, foto);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<List<UsuarioResponseDTO>> obtenerTodos() {
        List<UsuarioResponseDTO> response = usuarioService.obtenerTodosLosUsuarios();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/negocio/{negocioId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UsuarioResponseDTO>> obtenerPorNegocio(@PathVariable Long negocioId) {
        List<UsuarioResponseDTO> response = usuarioService.obtenerUsuariosPorNegocio(negocioId);
        return ResponseEntity.ok(response);
    }
}