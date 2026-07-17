package com.example.dilo.DiloBackend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
@Entity
@Table(name = "cuotas")
public class Cuota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación de vuelta hacia la cabecera de la deuda
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_por_cobrar_id", nullable = false)
    private CuentasPorCobrar cuentaPorCobrar;

    @Column(name = "numero_cuota", nullable = false)
    private Integer numeroCuota; // Ejemplo: 1, 2, 3...

    @Column(name = "monto_cuota", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoCuota;

    @Column(name = "saldo_pendiente_cuota", nullable = false, precision = 10, scale = 2)
    private BigDecimal saldoPendienteCuota;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDateTime fechaVencimiento;

    @Column(nullable = false)
    private String estado = "PENDIENTE"; // PENDIENTE, PAGADA, VENCIDA
}