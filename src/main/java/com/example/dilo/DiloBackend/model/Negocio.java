package com.example.dilo.DiloBackend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "negocios")
@NoArgsConstructor
@RequiredArgsConstructor
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
    private Boolean obligadoContabilidad;

    @Column(name = "ruta_firma")
    private String rutaFirma;

    @Column(name = "password_firma")
    private String passwordFirma;

    @Column(name = "codigo_invitacion", unique = true, nullable = false)
    private String codigoInvitacion;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @PrePersist
    public void generarCodigoInvitacion() {
        if (this.codigoInvitacion == null || this.codigoInvitacion.isEmpty()) {
            this.codigoInvitacion = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }

}