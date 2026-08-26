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
import com.example.dilo.DiloBackend.service.EmailService;
import com.example.dilo.DiloBackend.service.UsuarioService;
import com.example.dilo.DiloBackend.service.mapper.UsuarioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final ParroquiaRepository parroquiaRepository;
    private final SupabaseStorageService storageService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final Map<String, String> codigosRecuperacion = new ConcurrentHashMap<>();

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
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Las contraseñas nuevas no coinciden");
        }

        usuario.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        usuarioRepository.save(usuario);
    }

    @Override
    public UsuarioResponseDTO cambiarEstadoSuspension(Long id, boolean estado) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con el id: " + id));

        usuario.setSuspendido(estado);
        Usuario usuarioActualizado = usuarioRepository.save(usuario);

        return usuarioMapper.toDto(usuarioActualizado);
    }


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
                throw new RuntimeException("Error al subir la foto de perfil: " + e.getMessage());
            }
        }

        Usuario actualizado = usuarioRepository.save(usuario);
        return usuarioMapper.toDto(actualizado);
    }

    @Override
    public void generarCodigoRecuperacion(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("El correo ingresado no está registrado."));

        String codigo = String.format("%06d", new java.util.Random().nextInt(999999));

        codigosRecuperacion.put(email, codigo);
        emailService.enviarCodigoRecuperacion(email, codigo);
    }

    @Override
    public void restablecerPasswordConCodigo(String email, String codigo, String nuevaPassword) {
        String codigoGuardado = codigosRecuperacion.get(email);

        if (codigoGuardado == null || !codigoGuardado.equals(codigo)) {
            throw new IllegalArgumentException("El código es incorrecto o ha expirado.");
        }

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);
        codigosRecuperacion.remove(email);
    }

}