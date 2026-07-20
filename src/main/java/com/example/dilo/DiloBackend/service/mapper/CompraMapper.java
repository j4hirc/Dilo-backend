package com.example.dilo.DiloBackend.service.mapper;

import com.example.dilo.DiloBackend.dto.response.CompraResponseDTO;
import com.example.dilo.DiloBackend.dto.response.DetalleCompraResponseDTO;
import com.example.dilo.DiloBackend.model.Compra;
import com.example.dilo.DiloBackend.model.Lote;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CompraMapper {

    public CompraResponseDTO toDto(Compra compra, List<Lote> lotesGenerados) {
        CompraResponseDTO dto = new CompraResponseDTO();
        dto.setId(compra.getId());
        dto.setNumeroComprobante(compra.getNumeroComprobante());
        dto.setFechaCompra(compra.getFechaCompra());
        dto.setTotalCompra(compra.getTotalCompra());

        if (compra.getProveedor() != null) {
            dto.setProveedorNombre(compra.getProveedor().getNombre());
        }

        if (compra.getBodegaIngreso() != null) {
            dto.setBodegaIngresoNombre(compra.getBodegaIngreso().getNombre());
        }

        // Mapear los lotes como detalles de la compra
        if (lotesGenerados != null) {
            List<DetalleCompraResponseDTO> detallesDto = lotesGenerados.stream().map(lote -> {
                DetalleCompraResponseDTO detalle = new DetalleCompraResponseDTO();
                detalle.setProductoId(lote.getProducto().getId());
                detalle.setProductoNombre(lote.getProducto().getNombre());
                detalle.setCantidad(lote.getCantidadInicial().intValue());
                detalle.setCostoUnitario(lote.getCostoUnitario());
                detalle.setCostoTotal(lote.getCostoTotal());
                detalle.setFechaCaducidad(lote.getFechaCaducidad());
                return detalle;
            }).collect(Collectors.toList());

            dto.setDetalles(detallesDto);
        }

        return dto;
    }
}