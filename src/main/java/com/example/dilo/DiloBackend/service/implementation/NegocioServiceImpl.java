package com.example.dilo.DiloBackend.service.implementation;

import com.example.dilo.DiloBackend.dto.request.NegocioRequestDTO;
import com.example.dilo.DiloBackend.dto.response.NegocioResponseDTO;
import com.example.dilo.DiloBackend.exception.ResourceNotFoundException;
import com.example.dilo.DiloBackend.model.MiembroNegocio;
import com.example.dilo.DiloBackend.model.Negocio;
import com.example.dilo.DiloBackend.model.Role;
import com.example.dilo.DiloBackend.model.Usuario;
import com.example.dilo.DiloBackend.repository.MiembroNegocioRepository;
import com.example.dilo.DiloBackend.repository.NegocioRepository;
import com.example.dilo.DiloBackend.repository.RoleRepository;
import com.example.dilo.DiloBackend.repository.UsuarioRepository;
import com.example.dilo.DiloBackend.service.NegocioService;
import com.example.dilo.DiloBackend.service.mapper.NegocioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class NegocioServiceImpl implements NegocioService {

    private final NegocioRepository negocioRepository;
    private final FirmaEncryptionService firmaEncryptionService;
    private final UsuarioRepository usuarioRepository;
    private final MiembroNegocioRepository miembroNegocioRepository;
    private final RoleRepository roleRepository;
    private final SupabaseStorageService storageService;
    private final NegocioMapper negocioMapper;


    @Override
    public NegocioResponseDTO createNegocio(NegocioRequestDTO negocioRequestDTO, MultipartFile firma, String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado en el sistema"));

        Negocio negocio = negocioMapper.toEntity(negocioRequestDTO);

        try {
            if (firma != null && !firma.isEmpty()) {
                String urlFirma = storageService.uploadFile(firma, "firmas");
                negocio.setRutaFirma(urlFirma);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error crítico al subir la firma electrónica: " + e.getMessage());
        }

        if (negocioRequestDTO.getPasswordFirma() != null && !negocioRequestDTO.getPasswordFirma().isBlank()) {
            String passwordEncriptada = firmaEncryptionService.encriptar(negocioRequestDTO.getPasswordFirma());
            negocio.setPasswordFirma(passwordEncriptada);
        }

        Negocio negocioGuardado = negocioRepository.save(negocio);

        Role rolPropietario = roleRepository.findByNombre("PROPIETARIO")
                .orElseThrow(() -> new ResourceNotFoundException("Error: El rol PROPIETARIO no está configurado en la BD"));

        MiembroNegocio nuevoJefe = new MiembroNegocio();
        nuevoJefe.setUsuario(usuario);
        nuevoJefe.setNegocio(negocioGuardado);
        nuevoJefe.setRol(rolPropietario);
        nuevoJefe.setEstadoLaboral("Activo");

        miembroNegocioRepository.save(nuevoJefe);

        return negocioMapper.toDto(negocioGuardado);
    }
}
