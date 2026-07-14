package com.example.dilo.DiloBackend.service;

import com.example.dilo.DiloBackend.dto.request.NegocioRequestDTO;
import com.example.dilo.DiloBackend.dto.response.NegocioResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface NegocioService {

    List<NegocioResponseDTO> getAll();

    NegocioResponseDTO findById(Long id);

    List<NegocioResponseDTO> findByTerm(String term);


    NegocioResponseDTO createNegocio(NegocioRequestDTO negocioRequestDTO, MultipartFile firma, String email);
}
