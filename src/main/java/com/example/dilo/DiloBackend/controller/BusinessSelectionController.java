package com.example.dilo.DiloBackend.controller;

import com.example.dilo.DiloBackend.dto.response.AuthResponseDTO;
import com.example.dilo.DiloBackend.dto.request.BusinessSelectionDTO;
import com.example.dilo.DiloBackend.model.Usuario;
import com.example.dilo.DiloBackend.repository.MiembroNegocioRepository;
import com.example.dilo.DiloBackend.repository.UsuarioRepository;
import com.example.dilo.DiloBackend.security.jwt.JwtGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class BusinessSelectionController {

    private final MiembroNegocioRepository miembroNegocioRepository;
    private final UsuarioRepository usuarioRepository;
    private final JwtGenerator jwtGenerator;

    @PostMapping("/select-business")
    public ResponseEntity<AuthResponseDTO> selectBusiness(@RequestBody BusinessSelectionDTO request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        boolean belongs = miembroNegocioRepository.existsByUsuarioIdAndNegocioId(usuario.getId(), request.getBusinessId());
        if (!belongs) {
            return ResponseEntity.status(403).build();
        }
        String newToken = jwtGenerator.generateToken(authentication, request.getBusinessId());
        AuthResponseDTO response = new AuthResponseDTO();
        response.setToken(newToken);
        response.setIdUsuario(usuario.getId());
        response.setEmail(usuario.getEmail());
        response.setNombreCompleto(usuario.getPrimerNombre() + " " + usuario.getApellidoPaterno());
        response.setFotoPerfil(usuario.getFotoPerfil());
        response.setSuperAdmin(usuario.getRoles().stream()
                .anyMatch(r -> r.getNombre().equals("SUPER_ADMIN")));
        response.setSelectedBusinessId(request.getBusinessId());
        response.setNeedsBusinessSelection(false);
        return ResponseEntity.ok(response);
    }
}
