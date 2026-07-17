package com.example.dilo.DiloBackend.service.mapper;

import com.example.dilo.DiloBackend.dto.response.DetalleFacturaResponseDTO;
import com.example.dilo.DiloBackend.dto.response.FacturaResponseDTO;
import com.example.dilo.DiloBackend.model.DetalleFactura;
import com.example.dilo.DiloBackend.model.Factura;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class FacturaMapper {

    public FacturaResponseDTO toDto(Factura factura, List<DetalleFactura> detalles) {
        FacturaResponseDTO dto = new FacturaResponseDTO();
        dto.setId(factura.getId());
        dto.setNumeroFactura(factura.getNumeroFactura());
        dto.setFechaEmision(factura.getFechaEmision());
        dto.setClienteNombre(factura.getCliente().getPrimerNombre() + " " + factura.getCliente().getApellidoPaterno());
        dto.setClienteIdentificacion(factura.getCliente().getDni());
        dto.setSubtotalIva0(factura.getSubtotalIva0());
        dto.setSubtotalIvaAplicado(factura.getSubtotalIvaAplicado());
        dto.setTotalIva(factura.getTotalIva());
        dto.setTotalFactura(factura.getTotalFactura());
        dto.setFormaPago(factura.getFormaPago());
        dto.setEstadoSri(factura.getEstadoSri());

        if (detalles != null) {
            List<DetalleFacturaResponseDTO> detallesDto = detalles.stream()
                    .map(this::toDetalleDto)
                    .collect(Collectors.toList());
            dto.setDetalles(detallesDto);
        }

        return dto;
    }

    private DetalleFacturaResponseDTO toDetalleDto(DetalleFactura detalle) {
        DetalleFacturaResponseDTO dto = new DetalleFacturaResponseDTO();
        dto.setId(detalle.getId());
        dto.setProductoNombre(detalle.getProducto().getNombre());
        dto.setCantidad(detalle.getCantidad());
        dto.setPrecioUnitario(detalle.getPrecioUnitario());
        dto.setSubtotalItem(detalle.getSubtotalItem());
        return dto;
    }
}