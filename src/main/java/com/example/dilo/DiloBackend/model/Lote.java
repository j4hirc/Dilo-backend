package com.example.dilo.DiloBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "lote")
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "negocio_id", nullable = false)
    private Negocio negocio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bodega_id", nullable = false)
    private Bodega bodega;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compra_id")
    private Compra compra;

    @Column(name = "numero_lote", length = 100)
    private String numeroLote;

    @Column(name = "cantidad_inicial", precision = 10, scale = 2, nullable = false)
    private BigDecimal cantidadInicial;

    @Column(name = "cantidad_disponible", precision = 10, scale = 2, nullable = false)
    private BigDecimal cantidadDisponible;

    @Column(name = "costo_unitario", precision = 12, scale = 4, nullable = false)
    private BigDecimal costoUnitario;

    @Column(name = "costo_total", precision = 12, scale = 4, nullable = false)
    private BigDecimal costoTotal;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDateTime fechaIngreso;

    @Column(name = "fecha_caducidad")
    private LocalDate fechaCaducidad;

    @Column(name = "estado", length = 20, nullable = false)
    private String estado = "ACTIVO"; // ACTIVO, AGOTADO, VENCIDO
}