package com.example.dilo.DiloBackend.service;

import com.example.dilo.DiloBackend.dto.request.LoginRequestDTO;
import com.example.dilo.DiloBackend.dto.response.AuthResponseDTO;

public interface LoginService {

    AuthResponseDTO loginUsuario(LoginRequestDTO loginDto);
}
