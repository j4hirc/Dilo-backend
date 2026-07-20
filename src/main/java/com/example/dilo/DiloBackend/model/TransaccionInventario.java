package com.example.dilo.DiloBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "transacciones_inventario")
public class TransaccionInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_responsable_id", nullable = false)
    private Usuario usuarioResponsable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bodega_origen_id")
    private Bodega bodegaOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bodega_destino_id")
    private Bodega bodegaDestino;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "fecha_transaccion", updatable = false)
    private LocalDateTime fechaTransaccion;

    private String motivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "negocio_id", nullable = false)
    private Negocio negocio;

    @Column(name = "costo_unitario", precision = 12, scale = 4)
    private BigDecimal costoUnitario;

    @Column(name = "costo_total", precision = 12, scale = 4)
    private BigDecimal costoTotal;

    @Column(name = "metodo_aplicado", length = 20)
    private String metodoAplicado; // 'FIFO', 'LIFO', 'PROMEDIO'

    @Column(name = "documento_referencia", length = 100)
    private String documentoReferencia; // Ej: "FACT-001", "COMPRA-020"

    // Relación opcional con Lote (si la transacción consumió o ingresó un lote específico)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id")
    private Lote lote;

    @PrePersist
    protected void onCreate() {
        this.fechaTransaccion = LocalDateTime.now();
    }
}