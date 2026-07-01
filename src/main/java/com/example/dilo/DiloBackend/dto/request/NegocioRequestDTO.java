package com.example.dilo.DiloBackend.dto.request;

import jakarta.persistence.Column;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NegocioRequestDTO {


    private String ruc;

    private String razonSocial;

    private String nombreComercial;

    private boolean obligadoContabilidad = false;

    private String rutaFirma;

    private String passwordFirma;

    private LocalDateTime fechaCreacion = LocalDateTime.now();
}
