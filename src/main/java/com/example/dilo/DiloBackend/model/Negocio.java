package com.example.dilo.DiloBackend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "negocios")
public class Negocio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String ruc;

    @Column(name = "razon_social", nullable = false)
    private String razonSocial;

    @Column(name = "nombre_comercial", nullable = false)
    private String nombreComercial;

    @Column(name = "obligado_contabilidad")
    private boolean obligadoContabilidad = false;

    @Column(name = "ruta_firma")
    private String rutaFirma;

    @Column(name = "password_firma")
    private String passwordFirma;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    public Negocio() {}
}