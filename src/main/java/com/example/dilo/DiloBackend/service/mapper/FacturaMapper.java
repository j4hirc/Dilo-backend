package com.example.dilo.DiloBackend.service.mapper;

import com.example.dilo.DiloBackend.dto.response.DetalleFacturaResponseDTO;
import com.example.dilo.DiloBackend.dto.response.FacturaResponseDTO;
import com.example.dilo.DiloBackend.model.Cliente;
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

        Cliente cli = factura.getCliente();
        if (cli != null) {
            dto.setClienteId(cli.getId());
            dto.setClienteNombre(armarNombreCliente(cli));
            dto.setClienteIdentificacion(cli.getDni());
        } else {
            dto.setClienteId(null);
            dto.setClienteNombre("Consumidor Final");
            dto.setClienteIdentificacion("9999999999999");
        }

        dto.setSubtotalIva0(factura.getSubtotalIva0());
        dto.setSubtotalIvaAplicado(factura.getSubtotalIvaAplicado());
        dto.setTotalIva(factura.getTotalIva());
        dto.setTotalFactura(factura.getTotalFactura());
        dto.setFormaPago(factura.getFormaPago());
        dto.setEstadoSri(factura.getEstadoSri());
        dto.setTotalDescuento(factura.getTotalDescuento());
        dto.setPorcentajeIvaAplicado(factura.getPorcentajeIvaAplicado());

        if (detalles != null) {
            List<DetalleFacturaResponseDTO> detallesDto = detalles.stream()
                    .map(this::toDetalleDto)
                    .collect(Collectors.toList());
            dto.setDetalles(detallesDto);
        }

        return dto;
    }

    private String armarNombreCliente(Cliente cli) {
        StringBuilder sb = new StringBuilder();
        appendParte(sb, cli.getPrimerNombre());
        appendParte(sb, cli.getSegundoNombre());
        appendParte(sb, cli.getApellidoPaterno());
        appendParte(sb, cli.getApellidoMaterno());
        String nombre = sb.toString().trim();
        if (nombre.isEmpty()) {
            return "Cliente #" + cli.getId();
        }
        return nombre;
    }

    private void appendParte(StringBuilder sb, String parte) {
        if (parte == null) return;
        String t = parte.trim();
        if (t.isEmpty()) return;
        if (sb.length() > 0) sb.append(' ');
        sb.append(t);
    }

    private DetalleFacturaResponseDTO toDetalleDto(DetalleFactura detalle) {
        DetalleFacturaResponseDTO dto = new DetalleFacturaResponseDTO();
        dto.setId(detalle.getId());
        if (detalle.getProducto() != null) {
            dto.setProductoNombre(detalle.getProducto().getNombre());
        }
        dto.setCantidad(detalle.getCantidad());
        dto.setPrecioUnitario(detalle.getPrecioUnitario());
        dto.setSubtotalItem(detalle.getSubtotalItem());
        dto.setCostoUnitarioReal(detalle.getCostoUnitarioReal());
        dto.setCostoTotalReal(detalle.getCostoTotalReal());
        return dto;
    }
}