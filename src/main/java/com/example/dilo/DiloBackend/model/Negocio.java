package com.example.dilo.DiloBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "negocios")
@NoArgsConstructor
@AllArgsConstructor
public class Negocio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String ruc;

    @Column(name = "ruta_imagen")
    private String rutaImagen;

    @Column(name = "razon_social", nullable = false)
    private String razonSocial;

    @Column(name = "nombre_comercial", nullable = false)
    private String nombreComercial;

    @Column(name = "obligado_contabilidad")
    private Boolean obligadoContabilidad;

    @Column(name = "codigo_invitacion", unique = true, nullable = false)
    private String codigoInvitacion;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "direccion", nullable = false)
    private String direccion;

    @Column(name = "metodo_costeo", length = 20)
    private String metodoCosteo = "PROMEDIO";

    @PrePersist
    public void generarCodigoInvitacion() {
        if (this.codigoInvitacion == null || this.codigoInvitacion.isEmpty()) {
            this.codigoInvitacion = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }
}