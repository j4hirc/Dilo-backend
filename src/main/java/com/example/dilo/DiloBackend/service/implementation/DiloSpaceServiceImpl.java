package com.example.dilo.DiloBackend.service.implementation;

import com.example.dilo.DiloBackend.dto.request.DiloSpaceRequestDTO;
import com.example.dilo.DiloBackend.dto.response.DiloSpaceResponseDTO;
import com.example.dilo.DiloBackend.exception.ResourceNotFoundException;
import com.example.dilo.DiloBackend.model.DiloSpace;
import com.example.dilo.DiloBackend.model.MiembroEspacio;
import com.example.dilo.DiloBackend.model.Role;
import com.example.dilo.DiloBackend.model.Usuario;
import com.example.dilo.DiloBackend.repository.DiloSpaceRepository;
import com.example.dilo.DiloBackend.repository.MiembroEspacioRepository;
import com.example.dilo.DiloBackend.repository.RoleRepository;
import com.example.dilo.DiloBackend.repository.UsuarioRepository;
import com.example.dilo.DiloBackend.service.DiloSpaceService;
import com.example.dilo.DiloBackend.service.mapper.DiloSpaceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DiloSpaceServiceImpl implements DiloSpaceService {

    private final DiloSpaceRepository diloSpaceRepository;
    private final MiembroEspacioRepository miembroEspacioRepository;
    private final UsuarioRepository usuarioRepository;
    private final FirmaEncryptionService  firmaEncryptionService;
    private final DiloSpaceMapper diloSpaceMapper;
    private final SupabaseStorageService storageService;
    private final RoleRepository roleRepository;


    @Override
    public List<DiloSpaceResponseDTO> findAll() {
        List<DiloSpace> diloSpaces = diloSpaceRepository.findAll();

        return diloSpaces.stream()
                .map(diloSpaceMapper::toResponseDTO)
                .toList();
    }

    @Override
    public DiloSpaceResponseDTO findByDilo(String dilo) {
        DiloSpace space = diloSpaceRepository.findByCualquierCampo(dilo)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró ningún DiloSpace con ese valor: " + dilo));

        return diloSpaceMapper.toResponseDTO(space);
    }

    @Override
    public DiloSpaceResponseDTO createDiloSpace(DiloSpaceRequestDTO diloSpaceRequestDTO, MultipartFile archivoFirma, String emailUsuario) {

        diloSpaceRepository.findByNombreComercial(diloSpaceRequestDTO.getNombreComercial())
                .ifPresent(espacio -> {
                    throw new RuntimeException("El nombre comercial ya está en uso");
                });

        DiloSpace nuevoEspacio = diloSpaceMapper.toEntity(diloSpaceRequestDTO);

        String passEncriptado = firmaEncryptionService.encriptar(diloSpaceRequestDTO.getPasswordFirma());
        nuevoEspacio.setPasswordFirma(passEncriptado);

        try {
            if (archivoFirma != null && !archivoFirma.isEmpty()) {
                String rutaFirma = storageService.uploadFile(archivoFirma, "firmas_sri");
                nuevoEspacio.setRutaFirma(rutaFirma);
            } else {
                throw new RuntimeException("El archivo de la firma electrónica es obligatorio.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al subir la firma: " + e.getMessage());
        }

        diloSpaceRepository.save(nuevoEspacio);

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Role rolPropietario = roleRepository.findByNombre("PROPIETARIO")
                .orElseThrow(() -> new ResourceNotFoundException("Rol PROPIETARIO no encontrado"));

        MiembroEspacio miembroEspacio = new MiembroEspacio();
        miembroEspacio.setUsuario(usuario);
        miembroEspacio.setEspacio(nuevoEspacio);
        miembroEspacio.setRol(rolPropietario);
        miembroEspacio.setEstadoInvitacion("ACEPTADO");

        miembroEspacioRepository.save(miembroEspacio);

        return diloSpaceMapper.toResponseDTO(nuevoEspacio);
    }


}
