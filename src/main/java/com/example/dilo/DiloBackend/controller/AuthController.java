package com.example.dilo.DiloBackend.controller;

import com.example.dilo.DiloBackend.dto.request.RegisterUserDTO;
import com.example.dilo.DiloBackend.dto.response.UsuarioResponseDTO;
import com.example.dilo.DiloBackend.service.RegistroService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {


    private final RegistroService registroService;

    @PostMapping(value = "/registro", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registrarUsuario(
            @RequestPart("datos") RegisterUserDTO datosUsuario,
            @RequestPart(value = "foto", required = false) MultipartFile foto) {

        try {
            UsuarioResponseDTO response = registroService.registroUsuario(datosUsuario, foto);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error en el registro: " + e.getMessage());
        }
    }
}