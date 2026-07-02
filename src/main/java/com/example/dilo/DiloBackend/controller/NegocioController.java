package com.example.dilo.DiloBackend.controller;

import com.example.dilo.DiloBackend.dto.request.NegocioRequestDTO;
import com.example.dilo.DiloBackend.dto.response.NegocioResponseDTO;
import com.example.dilo.DiloBackend.service.NegocioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/negocios")
@RequiredArgsConstructor
public class NegocioController {

    private final NegocioService negocioService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<NegocioResponseDTO> crearNegocio(
            @RequestPart("datos") NegocioRequestDTO datosNegocio,
            @RequestPart(value = "firma", required = false) MultipartFile firma) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailUsuarioLogueado = authentication.getName();

        NegocioResponseDTO response = negocioService.createNegocio(datosNegocio, firma, emailUsuarioLogueado);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}