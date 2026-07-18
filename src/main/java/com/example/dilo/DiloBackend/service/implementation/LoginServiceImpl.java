package com.example.dilo.DiloBackend.service.implementation;

import com.example.dilo.DiloBackend.dto.request.LoginRequestDTO;
import com.example.dilo.DiloBackend.dto.response.AuthResponseDTO;
import com.example.dilo.DiloBackend.dto.response.NegocioResponseDTO;
import com.example.dilo.DiloBackend.model.MiembroNegocio;
import com.example.dilo.DiloBackend.service.mapper.NegocioMapper;
import com.example.dilo.DiloBackend.repository.MiembroNegocioRepository;
import java.util.List;
import java.util.stream.Collectors;
import com.example.dilo.DiloBackend.model.Usuario;
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
    private final UsuarioRepository usuarioRepository;
    private final MiembroNegocioRepository miembroNegocioRepository;
    private final NegocioMapper negocioMapper;

    @Override
    public AuthResponseDTO loginUsuario(LoginRequestDTO loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Obtener el usuario completo
        Usuario usuario = usuarioRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Buscar los negocios a los que pertenece el usuario
        List<MiembroNegocio> miembros = miembroNegocioRepository.findByUsuarioId(usuario.getId());
        List<NegocioResponseDTO> negocios = miembros.stream()
                .map(m -> negocioMapper.toDto(m.getNegocio()))
                .distinct() // En caso de que tenga multiples roles en el mismo negocio, evitamos duplicar el negocio
                .collect(Collectors.toList());
                
        List<String> roles = miembros.stream()
                .map(m -> m.getRol().getNombre())
                .distinct()
                .collect(Collectors.toList());

        boolean esSuperAdmin = usuario.getRoles().stream()
                .anyMatch(rol -> rol.getNombre().equals("SUPER_ADMIN"));

        AuthResponseDTO response = new AuthResponseDTO();
        response.setToken(jwtGenerator.generateToken(authentication, 
                (negocios.size() == 1) ? negocios.get(0).getIdNegocio() : null));
        response.setIdUsuario(usuario.getId());
        response.setEmail(usuario.getEmail());
        response.setNombreCompleto(usuario.getPrimerNombre() + " " + usuario.getApellidoPaterno());
        response.setFotoPerfil(usuario.getFotoPerfil());
        response.setSuperAdmin(esSuperAdmin);
        
        response.setBusinesses(negocios);
        response.setSelectedBusinessId(negocios.size() == 1 ? negocios.get(0).getIdNegocio() : null);
        response.setNeedsBusinessSelection(negocios.size() != 1);
        
        response.setRoles(roles);
        if (roles.size() == 1) {
            response.setRol(roles.get(0));
            response.setNeedsRoleSelection(false);
        } else if (roles.size() > 1) {
            response.setRol(null);
            response.setNeedsRoleSelection(true);
        } else {
            response.setNeedsRoleSelection(false);
        }

        return response;
    }
}