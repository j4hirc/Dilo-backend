package com.example.dilo.DiloBackend.security;

import com.example.dilo.DiloBackend.model.MiembroNegocio;
import com.example.dilo.DiloBackend.repository.MiembroNegocioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("seguridadNegocio")
@RequiredArgsConstructor
public class SeguridadNegocio {

    private final MiembroNegocioRepository miembroNegocioRepository;

    public boolean tieneRolEnNegocio(Authentication authentication, Long negocioId, String... rolesPermitidos) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String email = authentication.getName();

        List<MiembroNegocio> membresias = miembroNegocioRepository.findByUsuarioEmail(email);

        return membresias.stream()
                .filter(m -> m.getUsuario().getEmail().equals(email))
                .filter(m -> m.getNegocio().getId().equals(negocioId))
                .filter(m -> m.getEstadoLaboral().equals("Activo")) // No puede estar Inactivo o Pendiente
                .anyMatch(m -> {
                    for (String rol : rolesPermitidos) {
                        if (m.getRol().getNombre().equals(rol)) {
                            return true;
                        }
                    }
                    return false;
                });
    }
}