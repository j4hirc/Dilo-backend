package com.example.dilo.DiloBackend.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NegocioResponseDTO {

    private Long idNegocio;

    private String ruc;

    private String razonSocial;

    private String nombreComercial;

    private String direccion;

    private Boolean obligadoContabilidad;

    private String codigoInvitacion;

    private LocalDateTime fechaCreacion = LocalDateTime.now();

    private String rutaImagen;

}