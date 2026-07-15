package com.example.dilo.DiloBackend.service.implementation;


import com.example.dilo.DiloBackend.dto.request.MiembroNegocioRequestDTO;
import com.example.dilo.DiloBackend.dto.request.UnirseNegocioRequestDTO;
import com.example.dilo.DiloBackend.dto.response.MiembroNegocioResponseDTO;
import com.example.dilo.DiloBackend.exception.ResourceNotFoundException;
import com.example.dilo.DiloBackend.model.MiembroNegocio;
import com.example.dilo.DiloBackend.model.Negocio;
import com.example.dilo.DiloBackend.model.Role;
import com.example.dilo.DiloBackend.model.Usuario;
import com.example.dilo.DiloBackend.repository.MiembroNegocioRepository;
import com.example.dilo.DiloBackend.repository.NegocioRepository;
import com.example.dilo.DiloBackend.repository.RoleRepository;
import com.example.dilo.DiloBackend.repository.UsuarioRepository;
import com.example.dilo.DiloBackend.service.MiembroNegocioService;
import com.example.dilo.DiloBackend.service.mapper.MiembroNegocioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MiembroNegocioServiceImpl implements MiembroNegocioService {

    private final MiembroNegocioRepository miembroNegocioRepository;
    private final NegocioRepository negocioRepository;
    private final UsuarioRepository usuarioRepository;
    private final RoleRepository roleRepository;
    private final MiembroNegocioMapper miembroNegocioMapper;


    @Override
    public List<MiembroNegocioResponseDTO> obtenerMiembrosPorNegocio(Long negocioId) {
        negocioRepository.findById(negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Negocio no encontrado con ID: " + negocioId));

        return miembroNegocioRepository.findByNegocioId(negocioId).stream()
                .map(miembroNegocioMapper::toDto)
                .toList();
    }

    @Override
    public MiembroNegocioResponseDTO invitarMiembro(Long negocioId, MiembroNegocioRequestDTO requestDTO) {
        Negocio negocio = negocioRepository.findById(negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Negocio no encontrado"));

        Usuario usuario = usuarioRepository.findById(requestDTO.getIdUsuario())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + requestDTO.getIdUsuario()));

        Role rol = roleRepository.findById(requestDTO.getIdRol())
                .orElseThrow(() -> new ResourceNotFoundException("El rol especificado no existe con ID: " + requestDTO.getIdRol()));

        boolean yaExiste = miembroNegocioRepository.existsByUsuarioIdAndNegocioId(usuario.getId(), negocio.getId());
        if (yaExiste) {
            throw new RuntimeException("El usuario ya tiene una invitación o ya pertenece a este negocio.");
        }

        MiembroNegocio nuevoMiembro = miembroNegocioMapper.toEntity(requestDTO, usuario, negocio, rol);

        nuevoMiembro.setEstadoInvitacion("PENDIENTE");
        nuevoMiembro.setFechaVinculacion(null);

        MiembroNegocio guardado = miembroNegocioRepository.save(nuevoMiembro);

        return miembroNegocioMapper.toDto(guardado);
    }

    @Override
    public MiembroNegocioResponseDTO responderInvitacion(Long negocioId, Long miembroId, boolean aceptar) {
        MiembroNegocio miembro = miembroNegocioRepository.findById(miembroId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitación no encontrada"));

        if (!miembro.getNegocio().getId().equals(negocioId)) {
            throw new RuntimeException("La invitación no corresponde a este negocio");
        }

        if (!miembro.getEstadoInvitacion().equals("PENDIENTE")) {
            throw new RuntimeException("Esta invitación ya fue procesada anteriormente");
        }

        if (aceptar) {
            miembro.setEstadoInvitacion("ACEPTADO");
            miembro.setEstadoLaboral("Activo");
            miembro.setFechaVinculacion(LocalDateTime.now());
        } else {
            miembro.setEstadoInvitacion("RECHAZADO");
        }

        MiembroNegocio actualizado = miembroNegocioRepository.save(miembro);
        return miembroNegocioMapper.toDto(actualizado);
    }

    @Override
    public void eliminarMiembro(Long negocioId, Long miembroId) {
        MiembroNegocio miembro = miembroNegocioRepository.findById(miembroId)
                .orElseThrow(() -> new ResourceNotFoundException("Registro de miembro no encontrado"));

        if (!miembro.getNegocio().getId().equals(negocioId)) {
            throw new RuntimeException("El miembro no pertenece al negocio especificado");
        }

        miembroNegocioRepository.delete(miembro);
    }

    @Override
    public MiembroNegocioResponseDTO unirseConCodigo(UnirseNegocioRequestDTO requestDTO, String emailUsuario) {

        Negocio negocio = negocioRepository.findByCodigoInvitacion(requestDTO.getCodigoInvitacion())
                .orElseThrow(() -> new ResourceNotFoundException("Código de invitación inválido o negocio no encontrado"));

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado en el sistema"));

        boolean yaExiste = miembroNegocioRepository.existsByUsuarioIdAndNegocioId(usuario.getId(), negocio.getId());
        if (yaExiste) {
            throw new RuntimeException("Ya tienes una solicitud pendiente o ya perteneces a este negocio.");
        }

        Role rol = roleRepository.findById(requestDTO.getIdRol())
                .orElseThrow(() -> new ResourceNotFoundException("El rol especificado no existe"));

        MiembroNegocio nuevoMiembro = new MiembroNegocio();
        nuevoMiembro.setUsuario(usuario);
        nuevoMiembro.setNegocio(negocio);
        nuevoMiembro.setRol(rol);

        nuevoMiembro.setEstadoLaboral("Inactivo");
        nuevoMiembro.setEstadoInvitacion("PENDIENTE");
        nuevoMiembro.setFechaVinculacion(null);

        MiembroNegocio guardado = miembroNegocioRepository.save(nuevoMiembro);

        return miembroNegocioMapper.toDto(guardado);
    }
}
