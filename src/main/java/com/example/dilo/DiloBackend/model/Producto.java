package com.example.dilo.DiloBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "productos")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Column(name = "codigo_principal", nullable = false, length = 50)
    private String codigoPrincipal;

    @Column(name = "imagen")
    private String imagen;

    @Column(name = "marca")
    private String marca;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "graba_iva")
    private Boolean grabaIva;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "negocio_id", nullable = false)
    private Negocio negocio;

    @Column(name = "costo_promedio_actual", precision = 12, scale = 4)
    private BigDecimal costoPromedioActual = BigDecimal.ZERO;

    // --- NUEVOS CAMPOS ---
    @Column(name = "unidad_medida", length = 20)
    private String unidadMedida = "UNIDADES"; // Ej: LIBRAS, KILOGRAMOS, LITROS, UNIDADES

    @Column(name = "tiene_caducidad")
    private Boolean tieneCaducidad = false;
}