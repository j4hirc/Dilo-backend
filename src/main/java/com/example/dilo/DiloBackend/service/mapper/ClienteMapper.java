package com.example.dilo.DiloBackend.service.mapper;

import com.example.dilo.DiloBackend.dto.request.ClienteRequestDTO;
import com.example.dilo.DiloBackend.dto.response.ClienteResponseDTO;
import com.example.dilo.DiloBackend.model.Cliente;
import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {

    public ClienteResponseDTO toDto(Cliente cliente) {
        ClienteResponseDTO dto = new ClienteResponseDTO();
        dto.setId(cliente.getId());
        dto.setDni(cliente.getDni());
        dto.setPrimerNombre(cliente.getPrimerNombre());
        dto.setSegundoNombre(cliente.getSegundoNombre());
        dto.setApellidoPaterno(cliente.getApellidoPaterno());
        dto.setApellidoMaterno(cliente.getApellidoMaterno());
        dto.setEmail(cliente.getEmail());
        dto.setFechaNacimiento(cliente.getFechaNacimiento());
        dto.setTelefono(cliente.getTelefono());
        dto.setDireccion(cliente.getDireccion());

        String nombreCompleto = String.format("%s %s %s %s",
                cliente.getPrimerNombre(),
                cliente.getSegundoNombre() != null ? cliente.getSegundoNombre() : "",
                cliente.getApellidoPaterno(),
                cliente.getApellidoMaterno() != null ? cliente.getApellidoMaterno() : ""
        ).replaceAll("\\s+", " ").trim();

        dto.setNombreCompleto(nombreCompleto);

        return dto;
    }

    public Cliente toEntity(ClienteRequestDTO dto) {
        Cliente cliente = new Cliente();
        cliente.setDni(dto.getDni());
        cliente.setPrimerNombre(dto.getPrimerNombre());
        cliente.setSegundoNombre(dto.getSegundoNombre());
        cliente.setApellidoPaterno(dto.getApellidoPaterno());
        cliente.setApellidoMaterno(dto.getApellidoMaterno());
        cliente.setEmail(dto.getEmail());
        cliente.setFechaNacimiento(dto.getFechaNacimiento());
        cliente.setTelefono(dto.getTelefono());
        cliente.setDireccion(dto.getDireccion());
        return cliente;
    }
}