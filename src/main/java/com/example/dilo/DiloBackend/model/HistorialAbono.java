package com.example.dilo.DiloBackend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "historial_abonos")
public class HistorialAbono {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_por_cobrar_id", nullable = false)
    private CuentasPorCobrar cuentaPorCobrar;

    @Column(name = "monto_abonado", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoAbonado;

    @Column(name = "fecha_abono", nullable = false)
    private LocalDateTime fechaAbono = LocalDateTime.now();

    @Column(name = "metodo_pago", length = 50)
    private String metodoPago;

    @Column(length = 100)
    private String referencia;

    @Column(name = "usuario_receptor")
    private String usuarioRecibio;
}