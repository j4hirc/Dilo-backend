package com.example.dilo.DiloBackend.service;

import com.example.dilo.DiloBackend.dto.request.DiloSpaceRequestDTO;
import com.example.dilo.DiloBackend.dto.response.DiloSpaceResponseDTO;
import com.example.dilo.DiloBackend.model.DiloSpace;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DiloSpaceService {

    List<DiloSpaceResponseDTO> findAll();

    DiloSpaceResponseDTO findByDilo(String dilo);

    DiloSpaceResponseDTO createDiloSpace(DiloSpaceRequestDTO diloSpaceRequestDTO, MultipartFile archivoFirma, String emailUsuario);


}
