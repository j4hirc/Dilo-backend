package com.example.dilo.DiloBackend.controller;

import com.example.dilo.DiloBackend.dto.request.LoginRequestDTO;
import com.example.dilo.DiloBackend.dto.request.RegisterUserDTO;
import com.example.dilo.DiloBackend.dto.response.AuthResponseDTO;
import com.example.dilo.DiloBackend.dto.response.UsuarioResponseDTO;
import com.example.dilo.DiloBackend.exception.ResourceNotFoundException;
import com.example.dilo.DiloBackend.model.Usuario;
import com.example.dilo.DiloBackend.repository.UsuarioRepository;
import com.example.dilo.DiloBackend.security.jwt.JwtGenerator;
import com.example.dilo.DiloBackend.service.LoginService;
import com.example.dilo.DiloBackend.service.RegistroService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {


    private final RegistroService registroService;
    private final LoginService loginService;
    private final JwtGenerator jwtGenerator;
    private final UsuarioRepository usuarioRepository;

    @PostMapping(value = "/registro", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UsuarioResponseDTO> registrarUsuario(
            @RequestPart("datos") RegisterUserDTO datosUsuario,
            @RequestPart(value = "foto", required = false) MultipartFile foto) {

            UsuarioResponseDTO response = registroService.registroUsuario(datosUsuario, foto);
            return new ResponseEntity<>(response, HttpStatus.CREATED);

    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginDto) {
        try {
            AuthResponseDTO response = loginService.loginUsuario(loginDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas: " + e.getMessage());
        }
    }

    @GetMapping("/refresh-token")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AuthResponseDTO> refreshToken() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Collection<GrantedAuthority> authorities = usuario.getRoles().stream()
                .map(rol -> new SimpleGrantedAuthority(rol.getNombre()))
                .collect(Collectors.toList());

        Authentication nuevaAutenticacion = new UsernamePasswordAuthenticationToken(
                usuario.getEmail(), null, authorities
        );

        String nuevoToken = jwtGenerator.generateToken(nuevaAutenticacion);

        AuthResponseDTO response = new AuthResponseDTO();
        response.setToken(nuevoToken);

        return ResponseEntity.ok(response);
    }


}