package com.example.dilo.DiloBackend.service.implementation;

import com.example.dilo.DiloBackend.dto.request.UpdateUsuarioDTO;
import com.example.dilo.DiloBackend.dto.response.UsuarioResponseDTO;
import com.example.dilo.DiloBackend.exception.ResourceNotFoundException;
import com.example.dilo.DiloBackend.model.Parroquia;
import com.example.dilo.DiloBackend.model.Usuario;
import com.example.dilo.DiloBackend.repository.ParroquiaRepository;
import com.example.dilo.DiloBackend.repository.UsuarioRepository;
import com.example.dilo.DiloBackend.service.UsuarioService;
import com.example.dilo.DiloBackend.service.mapper.UsuarioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final ParroquiaRepository parroquiaRepository; // 🔥 Para actualizar la parroquia
    private final SupabaseStorageService storageService;

    @Override
    public UsuarioResponseDTO obtenerMiPerfil(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado en la base de datos"));
        return usuarioMapper.toDto(usuario);
    }

    @Override
    public List<UsuarioResponseDTO> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(usuarioMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UsuarioResponseDTO> obtenerUsuariosPorNegocio(Long negocioId) {
        return usuarioRepository.findUsuariosByNegocioId(negocioId).stream()
                .map(usuarioMapper::toDto)
                .collect(Collectors.toList());
    }

    // 🔥 NUEVO: ACTUALIZAR MI PERFIL (Con foto)
    @Override
    @Transactional
    public UsuarioResponseDTO actualizarMiPerfil(String email, UpdateUsuarioDTO dto, MultipartFile foto) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (dto.getPrimerNombre() != null) usuario.setPrimerNombre(dto.getPrimerNombre());
        if (dto.getSegundoNombre() != null) usuario.setSegundoNombre(dto.getSegundoNombre());
        if (dto.getApellidoPaterno() != null) usuario.setApellidoPaterno(dto.getApellidoPaterno());
        if (dto.getApellidoMaterno() != null) usuario.setApellidoMaterno(dto.getApellidoMaterno());
        if (dto.getTelefono() != null) usuario.setTelefono(dto.getTelefono());
        if (dto.getDireccion() != null) usuario.setDireccion(dto.getDireccion());
        if (dto.getFechaNacimiento() != null) usuario.setFechaNacimiento(dto.getFechaNacimiento());

        // Actualizamos parroquia si se envió un ID
        if (dto.getId_parroquia() != null) {
            Parroquia parroquia = parroquiaRepository.findById(dto.getId_parroquia())
                    .orElseThrow(() -> new ResourceNotFoundException("Parroquia no encontrada"));
            usuario.setParroquia(parroquia);
        }

        // Subimos la foto si el usuario seleccionó una
        if (foto != null && !foto.isEmpty()) {
            try {
                String urlFoto = storageService.uploadFile(foto, "perfiles");
                usuario.setFotoPerfil(urlFoto);
            } catch (Exception e) {
                throw new RuntimeException("Error al subir la foto de perfil: " + e.getMessage());
            }
        }

        Usuario actualizado = usuarioRepository.save(usuario);
        return usuarioMapper.toDto(actualizado);
    }

}