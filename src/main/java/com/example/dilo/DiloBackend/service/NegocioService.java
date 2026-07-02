package com.example.dilo.DiloBackend.service;

import com.example.dilo.DiloBackend.dto.request.NegocioRequestDTO;
import com.example.dilo.DiloBackend.dto.response.NegocioResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface NegocioService {
    NegocioResponseDTO createNegocio(NegocioRequestDTO negocioRequestDTO, MultipartFile firma, String email);
}
