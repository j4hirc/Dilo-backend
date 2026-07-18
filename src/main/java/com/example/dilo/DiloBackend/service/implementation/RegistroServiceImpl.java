package com.example.dilo.DiloBackend.service.implementation;

import com.example.dilo.DiloBackend.dto.request.RegisterUserDTO;
import com.example.dilo.DiloBackend.dto.response.UsuarioResponseDTO;
import com.example.dilo.DiloBackend.exception.ResourceNotFoundException;
import com.example.dilo.DiloBackend.model.Parroquia;
import com.example.dilo.DiloBackend.model.Role;
import com.example.dilo.DiloBackend.model.Usuario;
import com.example.dilo.DiloBackend.repository.ParroquiaRepository;
import com.example.dilo.DiloBackend.repository.RoleRepository;
import com.example.dilo.DiloBackend.repository.UsuarioRepository;
import com.example.dilo.DiloBackend.service.RegistroService;
import com.example.dilo.DiloBackend.service.mapper.UsuarioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList; // O HashSet si usas Set en tu modelo
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class RegistroServiceImpl implements RegistroService {

    private final UsuarioRepository usuarioRepository;
    private final SupabaseStorageService storageService;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper usuarioMapper;
    private final ParroquiaRepository parroquiaRepository;
    private final RoleRepository roleRepository;

    @Override
    public UsuarioResponseDTO registroUsuario(RegisterUserDTO registerUserDTO, MultipartFile foto) {
        try {
            if (foto != null && !foto.isEmpty()) {
                String urlFoto = storageService.uploadFile(foto, "perfiles");
                registerUserDTO.setFotoPerfil(urlFoto);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al subir la foto de perfil: " + e.getMessage());
        }

        Parroquia parroquia = parroquiaRepository.findById(registerUserDTO.getId_parroquia())
                .orElseThrow(()-> new ResourceNotFoundException("Parroquia no encontrada con el id: " + registerUserDTO.getId_parroquia()));

        Usuario usuario = usuarioMapper.toEntity(registerUserDTO, parroquia);
        usuario.setPassword(passwordEncoder.encode(registerUserDTO.getPassword()));

        usuario.setEstadoLaboral("Activo");

        Role rolBase = roleRepository.findByNombre("USUARIO_BASE")
                .orElseThrow(() -> new ResourceNotFoundException("Rol USUARIO_BASE no encontrado en la base de datos"));

        if (usuario.getRoles() == null) {
            usuario.setRoles(new HashSet<>()); // Cambia a new HashSet<>() si en tu modelo usas un Set
        }
        usuario.getRoles().add(rolBase);

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        return usuarioMapper.toDto(usuarioGuardado);
    }
}