package com.example.dilo.DiloBackend.scheduler;

import com.example.dilo.DiloBackend.model.Negocio;
import com.example.dilo.DiloBackend.repository.NegocioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NegocioScheduler {

    private final NegocioRepository negocioRepository;

    @Scheduled(fixedRate = 7200000)
    @Transactional
    public void rotarCodigosInvitacion() {
        List<Negocio> negocios = negocioRepository.findAll();

        for (Negocio negocio : negocios) {
            String nuevoCodigo = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            negocio.setCodigoInvitacion(nuevoCodigo);
        }

        // Guarda todos los negocios con sus nuevos códigos
        negocioRepository.saveAll(negocios);
        System.out.println("✅ [Scheduler] Códigos de invitación rotados para " + negocios.size() + " negocios.");
    }
}