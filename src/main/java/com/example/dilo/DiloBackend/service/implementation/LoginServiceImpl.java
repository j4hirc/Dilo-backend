package com.example.dilo.DiloBackend.service.implementation;

import com.example.dilo.DiloBackend.dto.request.LoginRequestDTO;
import com.example.dilo.DiloBackend.dto.response.AuthResponseDTO;
import com.example.dilo.DiloBackend.model.Usuario;
import com.example.dilo.DiloBackend.repository.MiembroEspacioRepository;
import com.example.dilo.DiloBackend.repository.UsuarioRepository;
import com.example.dilo.DiloBackend.security.jwt.JwtGenerator;
import com.example.dilo.DiloBackend.service.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {


    private final AuthenticationManager authenticationManager;
    private final JwtGenerator jwtGenerator;
    private final MiembroEspacioRepository miembroEspacioRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public AuthResponseDTO loginUsuario(LoginRequestDTO loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtGenerator.generateToken(authentication);

        Usuario usuario = usuarioRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        boolean esSuperAdmin = miembroEspacioRepository.findSuperAdminByUsuarioId(usuario.getId()).isPresent();

        AuthResponseDTO response = new AuthResponseDTO();
        response.setToken(token);
        response.setIdUsuario(usuario.getId());
        response.setEmail(usuario.getEmail());
        response.setNombreCompleto(usuario.getPrimerNombre() + " " + usuario.getApellidoPaterno());
        response.setFotoPerfil(usuario.getFotoPerfil());
        response.setSuperAdmin(esSuperAdmin);

        return response;
    }
}
