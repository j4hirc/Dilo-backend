package com.example.dilo.DiloBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "facturas")
public class Factura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_emisor_id", nullable = false)
    private Usuario usuarioEmisor;

    @Column(name = "numero_factura", nullable = false, length = 50)
    private String numeroFactura;

    @Column(name = "fecha_emision")
    private LocalDateTime fechaEmision;

    @Column(name = "subtotal_iva_0", precision = 10, scale = 2)
    private BigDecimal subtotalIva0;

    @Column(name = "subtotal_iva_aplicado", precision = 10, scale = 2)
    private BigDecimal subtotalIvaAplicado;

    @Column(name = "total_descuento", precision = 10, scale = 2)
    private BigDecimal totalDescuento;

    @Column(name = "porcentaje_iva_aplicado", precision = 5, scale = 2)
    private BigDecimal porcentajeIvaAplicado;

    @Column(name = "total_iva", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalIva;

    @Column(name = "total_factura", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalFactura;

    @Column(name = "metodo_pago", nullable = false, length = 50)
    private String formaPago;

    @Column(name = "clave_acceso_sri", length = 49)
    private String claveAccesoSri;

    @Column(name = "estado_sri", length = 20)
    private String estadoSri;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "negocio_id", nullable = false)
    private Negocio negocio;
}