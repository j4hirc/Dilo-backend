package com.example.dilo.DiloBackend.controller;

import com.example.dilo.DiloBackend.dto.request.DiloSpaceRequestDTO;
import com.example.dilo.DiloBackend.dto.response.DiloSpaceResponseDTO;
import com.example.dilo.DiloBackend.service.DiloSpaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/dilo-space")
@RequiredArgsConstructor
public class DiloSpaceController {

    private final DiloSpaceService diloSpaceService;


    @PostMapping(value = "/crear", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DiloSpaceResponseDTO> createDiloSpace(
            @RequestPart("datos") DiloSpaceRequestDTO datosNegocio,
            @RequestPart("firma") MultipartFile archivoFirma,
            Authentication authentication) {

            String emailUsuario = authentication.getName();
            DiloSpaceResponseDTO dto = diloSpaceService.createDiloSpace(datosNegocio, archivoFirma, emailUsuario);
            return new ResponseEntity<>(dto, HttpStatus.CREATED);

    }


}
