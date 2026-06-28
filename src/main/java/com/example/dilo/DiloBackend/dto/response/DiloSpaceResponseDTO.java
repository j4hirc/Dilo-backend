package com.example.dilo.DiloBackend.dto.response;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DiloSpaceResponseDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 13)
    private String ruc;

    @Column(name = "nombre_comercial", nullable = false)
    private String nombreComercial;

    @Column(name = "razon_social", nullable = false)
    private String razonSocial;

    @Column(name = "obligado_contabilidad")
    private Boolean obligadoContabilidad;

    @Column(name = "firma_electronica_path")
    private String firmaElectronicaPath;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

}
