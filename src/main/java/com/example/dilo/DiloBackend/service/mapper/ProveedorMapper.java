package com.example.dilo.DiloBackend.service.mapper;

import com.example.dilo.DiloBackend.dto.request.ProveedorRequestDTO;
import com.example.dilo.DiloBackend.dto.response.ProveedorResponseDTO;
import com.example.dilo.DiloBackend.model.Proveedor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProveedorMapper {

    private final CategoriaMapper categoriaMapper; // Reutilizamos tu mapper actual

    public ProveedorResponseDTO toDto(Proveedor proveedor) {
        ProveedorResponseDTO dto = new ProveedorResponseDTO();
        dto.setId(proveedor.getId());
        dto.setDni(proveedor.getDni());
        dto.setNombre(proveedor.getNombre());
        dto.setTelefono(proveedor.getTelefono());
        dto.setEstado(proveedor.getEstado());
        dto.setFechaCreacion(proveedor.getFechaCreacion());

        if (proveedor.getCategorias() != null) {
            dto.setCategorias(proveedor.getCategorias().stream()
                    .map(categoriaMapper::toDto)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    public Proveedor toEntity(ProveedorRequestDTO dto) {
        Proveedor proveedor = new Proveedor();
        proveedor.setDni(dto.getDni());
        proveedor.setNombre(dto.getNombre());
        proveedor.setTelefono(dto.getTelefono());
        proveedor.setEstado(dto.getEstado() != null ? dto.getEstado() : true);
        return proveedor;
    }
}