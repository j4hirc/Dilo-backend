package com.example.dilo.DiloBackend.service.mapper;

import com.example.dilo.DiloBackend.dto.request.RegisterUserDTO;
import com.example.dilo.DiloBackend.dto.response.UsuarioResponseDTO;
import com.example.dilo.DiloBackend.model.Parroquia;
import com.example.dilo.DiloBackend.model.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {


    public Usuario toEntity(RegisterUserDTO registerUserDTO, Parroquia parroquia){
       Usuario entity = new Usuario();
       entity.setDni(registerUserDTO.getDni());
       entity.setFotoPerfil(registerUserDTO.getFotoPerfil());
       entity.setPrimerNombre(registerUserDTO.getPrimerNombre());
       entity.setSegundoNombre(registerUserDTO.getSegundoNombre());
       entity.setApellidoPaterno(registerUserDTO.getApellidoPaterno());
       entity.setApellidoMaterno(registerUserDTO.getApellidoMaterno());
       entity.setEmail(registerUserDTO.getEmail());
       entity.setPassword(registerUserDTO.getPassword());
       entity.setFechaNacimiento(registerUserDTO.getFechaNacimiento());
       entity.setTelefono(registerUserDTO.getTelefono());
       entity.setDireccion(registerUserDTO.getDireccion());
       entity.setParroquia(parroquia);
       return entity;
    }

    public UsuarioResponseDTO toDto(Usuario entity){
        UsuarioResponseDTO usuarioResponseDTO =  new UsuarioResponseDTO();
        usuarioResponseDTO.setId(entity.getId());
        usuarioResponseDTO.setDni(entity.getDni());
        usuarioResponseDTO.setFotoPerfil(entity.getFotoPerfil());
        usuarioResponseDTO.setPrimerNombre(entity.getPrimerNombre());
        usuarioResponseDTO.setSegundoNombre(entity.getSegundoNombre());
        usuarioResponseDTO.setApellidoPaterno(entity.getApellidoPaterno());
        usuarioResponseDTO.setApellidoMaterno(entity.getApellidoMaterno());
        usuarioResponseDTO.setEmail(entity.getEmail());
        usuarioResponseDTO.setPassword(entity.getPassword());
        usuarioResponseDTO.setFechaNacimiento(entity.getFechaNacimiento());
        usuarioResponseDTO.setTelefono(entity.getTelefono());
        usuarioResponseDTO.setDireccion(entity.getDireccion());
        usuarioResponseDTO.setId_parroquia(entity.getParroquia().getId());
        usuarioResponseDTO.setNameParroquia(entity.getParroquia().getNombre());
return usuarioResponseDTO;
    }

}
