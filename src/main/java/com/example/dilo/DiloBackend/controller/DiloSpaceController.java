package com.example.dilo.DiloBackend.controller;

import com.example.dilo.DiloBackend.dto.request.DiloSpaceRequestDTO;
import com.example.dilo.DiloBackend.dto.response.DiloSpaceResponseDTO;
import com.example.dilo.DiloBackend.service.DiloSpaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dilo-space")
@RequiredArgsConstructor
public class DiloSpaceController {

    private final DiloSpaceService diloSpaceService;

    @GetMapping("/all")
    public ResponseEntity<List<DiloSpaceResponseDTO>> getAll(){
        List<DiloSpaceResponseDTO> diloSpaceResponseDTO = diloSpaceService.findAll();
        return ResponseEntity.ok(diloSpaceResponseDTO);
    }

    @GetMapping("/param/{param}")
    public ResponseEntity<DiloSpaceResponseDTO> getParam(@PathVariable String param){
        DiloSpaceResponseDTO diloSpaceResponseDTO = diloSpaceService.findByDilo(param);
        return ResponseEntity.ok(diloSpaceResponseDTO);
    }


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
