package com.example.dilo.DiloBackend.service.mapper;

import com.example.dilo.DiloBackend.dto.request.InventarioBodegaRequestDTO;
import com.example.dilo.DiloBackend.dto.response.InventarioBodegaResponseDTO;
import com.example.dilo.DiloBackend.model.InventarioBodega;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class InventarioBodegaMapper {

    public InventarioBodegaResponseDTO toDto(InventarioBodega inventario) {
        InventarioBodegaResponseDTO dto = new InventarioBodegaResponseDTO();
        dto.setId(inventario.getId());

        if (inventario.getProducto() != null) {
            dto.setProductoId(inventario.getProducto().getId());
            dto.setProductoNombre(inventario.getProducto().getNombre());
            dto.setProductoCodigo(inventario.getProducto().getCodigoPrincipal());

            dto.setCostoPromedio(inventario.getProducto().getCostoPromedioActual());
        }

        if (inventario.getBodega() != null) {
            dto.setBodegaId(inventario.getBodega().getId());
            dto.setBodegaNombre(inventario.getBodega().getNombre());
        }

        int cantidad = inventario.getCantidadActual() != null ? inventario.getCantidadActual() : 0;
        int minimo = inventario.getStockMinimo() != null ? inventario.getStockMinimo() : 0;
        dto.setCantidadActual(cantidad);
        dto.setStockMinimo(minimo);
        dto.setAlertaStock(cantidad <= minimo);

        BigDecimal costo = dto.getCostoPromedio() != null ? dto.getCostoPromedio() : BigDecimal.ZERO;
        if (costo.compareTo(BigDecimal.ZERO) < 0) {
            costo = BigDecimal.ZERO;
        }
        dto.setCostoPromedio(costo);

        // Valor = costo promedio × cantidad en esta bodega (2 decimales)
        dto.setValorInventario(
                costo.multiply(new BigDecimal(cantidad)).setScale(2, java.math.RoundingMode.HALF_UP)
        );

        return dto;
    }

    public InventarioBodega toEntity(InventarioBodegaRequestDTO dto) {
        InventarioBodega inventario = new InventarioBodega();
        inventario.setCantidadActual(dto.getCantidadActual());
        inventario.setStockMinimo(dto.getStockMinimo());
        return inventario;
    }
}