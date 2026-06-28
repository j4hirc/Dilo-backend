package com.example.dilo.DiloBackend.service;

import com.example.dilo.DiloBackend.dto.request.DiloSpaceRequestDTO;
import com.example.dilo.DiloBackend.dto.response.DiloSpaceResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface DiloSpaceService {

    DiloSpaceResponseDTO createDiloSpace(DiloSpaceRequestDTO diloSpaceRequestDTO, MultipartFile archivoFirma, String emailUsuario);


}
