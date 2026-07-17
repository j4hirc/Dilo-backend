package com.example.dilo.DiloBackend.service.implementation;

import com.example.dilo.DiloBackend.model.ParametroGlobal;
import com.example.dilo.DiloBackend.repository.ParametroGlobalRepository;
import com.example.dilo.DiloBackend.service.ParametroGlobalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ParametroGlobalServiceImpl implements ParametroGlobalService {

    private final ParametroGlobalRepository parametroRepository;

    @Override
    @Transactional
    public void actualizarIva(String nuevoIva) {
        ParametroGlobal ivaParam = parametroRepository.findById("IVA_ACTUAL")
                .orElseGet(() -> {
                    ParametroGlobal nuevoParam = new ParametroGlobal();
                    nuevoParam.setClave("IVA_ACTUAL");
                    nuevoParam.setDescripcion("Porcentaje de IVA actual para todos los negocios de Dilo");
                    return nuevoParam;
                });

        ivaParam.setValor(nuevoIva);

        parametroRepository.save(ivaParam);
    }
    @Override
    public String obtenerIvaActual() {
        return parametroRepository.findById("IVA_ACTUAL")
                .map(ParametroGlobal::getValor)
                .orElse("0.15"); // Valor por defecto si por alguna razón falla
    }
}