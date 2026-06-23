package com.example.dilo.DiloBackend.service;

import com.example.dilo.DiloBackend.dto.request.RegisterUserDTO;
import com.example.dilo.DiloBackend.dto.response.UsuarioResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface RegistroService {

    UsuarioResponseDTO registroUsuario(RegisterUserDTO registerUserDTO, MultipartFile foto);

}
