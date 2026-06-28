package com.example.dilo.DiloBackend.dto.response;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DiloSpaceResponseDTO {

    private Long id;

    private String ruc;

    private String nombreComercial;

    private String razonSocial;

    private Boolean obligadoContabilidad;

    private String rutaFirma;

    private String passwordFirma;

    private String codigoInvitacion;


    private LocalDateTime fechaCreacion;

}
