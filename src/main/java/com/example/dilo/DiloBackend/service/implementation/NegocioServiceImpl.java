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

import java.util.List;

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
    public List<NegocioResponseDTO> getAll() {
        List<Negocio> negocios = negocioRepository.findAll();
        return negocios.stream()
                .map(negocioMapper::toDto)
                .toList();
    }

    @Override
    public NegocioResponseDTO findById(Long id) {
        Negocio negocio = negocioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Negocio no encontrado"));
        return negocioMapper.toDto(negocio);
    }

    @Override
    public List<NegocioResponseDTO> findByTerm(String term) {
        List<Negocio> negocios = negocioRepository.buscarPorTermino(term);

        if (negocios.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron negocios con el término: " + term);
        }

        return negocios.stream()
                .map(negocioMapper::toDto)
                .toList();
    }

    @Override
    public NegocioResponseDTO createNegocio(NegocioRequestDTO negocioRequestDTO, MultipartFile firma, MultipartFile imagen, String email) {
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

        try {
            if (imagen != null && !imagen.isEmpty()) {
                String urlImagen = storageService.uploadFile(imagen, "logos");
                negocio.setRutaImagen(urlImagen);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error crítico al subir la imagen del negocio: " + e.getMessage());
        }

        if (negocioRequestDTO.getPasswordFirma() != null && !negocioRequestDTO.getPasswordFirma().isBlank()) {
            String passwordEncriptada = firmaEncryptionService.encriptar(negocioRequestDTO.getPasswordFirma());
            negocio.setPasswordFirma(passwordEncriptada);
        }

        Negocio negocioGuardado = negocioRepository.save(negocio);

        Role rolPropietario = roleRepository.findByNombre("PROPIETARIO")
                .orElseThrow(() -> new ResourceNotFoundException("Error: El rol PROPIETARIO no está configurado en la BD"));

        if (!usuario.getRoles().contains(rolPropietario)) {
            usuario.getRoles().add(rolPropietario);
            usuarioRepository.save(usuario);
        }

        MiembroNegocio nuevoJefe = new MiembroNegocio();
        nuevoJefe.setUsuario(usuario);
        nuevoJefe.setNegocio(negocioGuardado);
        nuevoJefe.setRol(rolPropietario);
        nuevoJefe.setEstadoLaboral("Activo");

        miembroNegocioRepository.save(nuevoJefe);

        return negocioMapper.toDto(negocioGuardado);
    }

    @Override
    public NegocioResponseDTO updateNegocio(Long id, NegocioRequestDTO requestDTO, MultipartFile firma, MultipartFile imagen) {
        Negocio negocioExistente = negocioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Negocio no encontrado con ID: " + id));

        negocioExistente.setRuc(requestDTO.getRuc());
        negocioExistente.setRazonSocial(requestDTO.getRazonSocial());
        negocioExistente.setNombreComercial(requestDTO.getNombreComercial());

        // --- AQUÍ ACTUALIZAMOS LA DIRECCIÓN ---
        negocioExistente.setDireccion(requestDTO.getDireccion());

        negocioExistente.setObligadoContabilidad(requestDTO.getObligadoContabilidad());

        try {
            if (firma != null && !firma.isEmpty()) {
                String urlFirma = storageService.uploadFile(firma, "firmas");
                negocioExistente.setRutaFirma(urlFirma);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al subir la nueva firma electrónica: " + e.getMessage());
        }

        try {
            if (imagen != null && !imagen.isEmpty()) {
                String urlImagen = storageService.uploadFile(imagen, "logos");
                negocioExistente.setRutaImagen(urlImagen);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al subir la nueva imagen: " + e.getMessage());
        }

        if (requestDTO.getPasswordFirma() != null && !requestDTO.getPasswordFirma().isBlank()) {
            String passwordEncriptada = firmaEncryptionService.encriptar(requestDTO.getPasswordFirma());
            negocioExistente.setPasswordFirma(passwordEncriptada);
        }

        Negocio negocioActualizado = negocioRepository.save(negocioExistente);
        return negocioMapper.toDto(negocioActualizado);
    }

    @Override
    public void deleteNegocio(Long id) {
        Negocio negocio = negocioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Negocio no encontrado con ID: " + id));

        negocioRepository.delete(negocio);
    }
}