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

        dto.setCantidadActual(inventario.getCantidadActual());
        dto.setStockMinimo(inventario.getStockMinimo());
        dto.setAlertaStock(inventario.getCantidadActual() <= inventario.getStockMinimo());

        if (dto.getCostoPromedio() != null && dto.getCantidadActual() != null) {
            dto.setValorInventario(dto.getCostoPromedio().multiply(new BigDecimal(dto.getCantidadActual())));
        } else {
            dto.setValorInventario(BigDecimal.ZERO);
        }

        return dto;
    }

    public InventarioBodega toEntity(InventarioBodegaRequestDTO dto) {
        InventarioBodega inventario = new InventarioBodega();
        inventario.setCantidadActual(dto.getCantidadActual());
        inventario.setStockMinimo(dto.getStockMinimo());
        return inventario;
    }
}