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

import java.time.LocalDate;
import java.time.Period;
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
        if (usuarioRepository.existsByDni(registerUserDTO.getDni())) {
            throw new IllegalArgumentException("La cédula ingresada ya se encuentra registrada.");
        }

        if (usuarioRepository.existsByEmail(registerUserDTO.getEmail())) {
            throw new IllegalArgumentException("El correo electrónico ya se encuentra registrado.");
        }

        if (!esCedulaValida(registerUserDTO.getDni())) {
            throw new IllegalArgumentException("El número de cédula ecuatoriana no es válido.");
        }

        if (registerUserDTO.getFechaNacimiento() != null) {
            int edad = Period.between(registerUserDTO.getFechaNacimiento(), LocalDate.now()).getYears();
            if (edad < 18) {
                throw new IllegalArgumentException("El usuario debe ser mayor de 18 años para registrarse.");
            }
        }

        if (registerUserDTO.getPassword() == null || registerUserDTO.getPassword().length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres.");
        }

        try {
            if (foto != null && !foto.isEmpty()) {
                String urlFoto = storageService.uploadFile(foto, "perfiles");
                registerUserDTO.setFotoPerfil(urlFoto);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al subir la foto de perfil: " + e.getMessage());
        }

        Parroquia parroquia = parroquiaRepository.findById(registerUserDTO.getId_parroquia())
                .orElseThrow(() -> new ResourceNotFoundException("Parroquia no encontrada con el id: " + registerUserDTO.getId_parroquia()));

        Usuario usuario = usuarioMapper.toEntity(registerUserDTO, parroquia);
        usuario.setPassword(passwordEncoder.encode(registerUserDTO.getPassword()));
        usuario.setEstadoLaboral("Activo");

        Role rolBase = roleRepository.findByNombre("USUARIO_BASE")
                .orElseThrow(() -> new ResourceNotFoundException("Rol USUARIO_BASE no encontrado en la base de datos"));

        if (usuario.getRoles() == null) {
            usuario.setRoles(new HashSet<>());
        }
        usuario.getRoles().add(rolBase);

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        return usuarioMapper.toDto(usuarioGuardado);
    }

    private boolean esCedulaValida(String cedula) {
        if (cedula == null || cedula.length() != 10 || !cedula.matches("\\d+")) {
            return false;
        }
        int provincia = Integer.parseInt(cedula.substring(0, 2));
        if (provincia < 1 || (provincia > 24 && provincia != 30)) {
            return false;
        }
        int tercerDigito = Integer.parseInt(cedula.substring(2, 3));
        if (tercerDigito >= 6) {
            return false;
        }
        int suma = 0;
        for (int i = 0; i < 9; i++) {
            int digito = Character.getNumericValue(cedula.charAt(i));
            if (i % 2 == 0) {
                digito *= 2;
                if (digito > 9) digito -= 9;
            }
            suma += digito;
        }
        int digitoVerificador = Character.getNumericValue(cedula.charAt(9));
        int decenaSuperior = (suma % 10 == 0) ? suma : ((suma / 10) + 1) * 10;
        return (decenaSuperior - suma) == digitoVerificador;
    }
}