package com.example.dilo.DiloBackend.service.implementation;

import com.example.dilo.DiloBackend.dto.request.ChangePasswordRequestDTO;
import com.example.dilo.DiloBackend.dto.request.UpdateUsuarioDTO;
import com.example.dilo.DiloBackend.dto.request.UserAdminUpdateDTO;
import com.example.dilo.DiloBackend.dto.response.UsuarioResponseDTO;
import com.example.dilo.DiloBackend.exception.ResourceNotFoundException;
import com.example.dilo.DiloBackend.model.Parroquia;
import com.example.dilo.DiloBackend.model.Usuario;
import com.example.dilo.DiloBackend.repository.ParroquiaRepository;
import com.example.dilo.DiloBackend.repository.UsuarioRepository;
import com.example.dilo.DiloBackend.service.UsuarioService;
import com.example.dilo.DiloBackend.service.mapper.UsuarioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

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

    @Override
    public UsuarioResponseDTO actualizarUsuarioAdmin(Long id, UserAdminUpdateDTO dto, MultipartFile foto) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con el id: " + id));

        // 2. Actualizamos los datos de texto
        usuario.setDni(dto.getDni());
        usuario.setPrimerNombre(dto.getPrimerNombre());
        usuario.setSegundoNombre(dto.getSegundoNombre());
        usuario.setApellidoPaterno(dto.getApellidoPaterno());
        usuario.setApellidoMaterno(dto.getApellidoMaterno());
        usuario.setEmail(dto.getEmail());
        usuario.setTelefono(dto.getTelefono());
        usuario.setDireccion(dto.getDireccion());
        usuario.setFechaNacimiento(dto.getFechaNacimiento());

        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        if (dto.getId_parroquia() != null) {
            Parroquia parroquia = parroquiaRepository.findById(dto.getId_parroquia())
                    .orElseThrow(() -> new ResourceNotFoundException("Parroquia no encontrada"));
            usuario.setParroquia(parroquia);
        }

        if (foto != null && !foto.isEmpty()) {
            try {
                String urlFoto = storageService.uploadFile(foto, "perfiles");
                usuario.setFotoPerfil(urlFoto);
            } catch (Exception e) {
                throw new RuntimeException("Error al subir la nueva foto: " + e.getMessage());
            }
        }

        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        return usuarioMapper.toDto(usuarioActualizado);
    }

    @Override
    public void cambiarPassword(String email, ChangePasswordRequestDTO dto) {
        // 1. Buscar al usuario
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // 🔥 Se eliminó la validación de la contraseña actual

        // 2. Validar que la nueva contraseña y la confirmación coincidan
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Las contraseñas nuevas no coinciden");
        }

        // (Opcional) Validar la fortaleza de la contraseña aquí (longitud, mayúsculas, etc.)

        // 3. Encriptar y guardar la nueva contraseña
        usuario.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        usuarioRepository.save(usuario);
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