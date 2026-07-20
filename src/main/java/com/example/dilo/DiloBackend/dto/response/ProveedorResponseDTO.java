package com.example.dilo.DiloBackend.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProveedorResponseDTO {
    private Long id;
    private String dni;
    private String nombre;
    private String telefono;
    private Boolean estado;
    private LocalDateTime fechaCreacion;

    private List<CategoriaResponseDTO> categorias;
}