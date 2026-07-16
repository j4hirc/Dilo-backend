package com.example.dilo.DiloBackend.service.mapper;

import com.example.dilo.DiloBackend.dto.request.TransaccionInventarioRequestDTO;
import com.example.dilo.DiloBackend.dto.response.TransaccionInventarioResponseDTO;
import com.example.dilo.DiloBackend.model.TransaccionInventario;
import org.springframework.stereotype.Component;

@Component
public class TransaccionInventarioMapper {

    public TransaccionInventarioResponseDTO toDto(TransaccionInventario transaccion) {
        TransaccionInventarioResponseDTO dto = new TransaccionInventarioResponseDTO();
        dto.setId(transaccion.getId());
        dto.setTipo(transaccion.getTipo());
        dto.setCantidad(transaccion.getCantidad());
        dto.setFechaTransaccion(transaccion.getFechaTransaccion());
        dto.setMotivo(transaccion.getMotivo());
        if (transaccion.getProducto() != null) {
            dto.setProductoNombre(transaccion.getProducto().getNombre());
        }
        if (transaccion.getUsuarioResponsable() != null) {
            dto.setUsuarioResponsableNombre(transaccion.getUsuarioResponsable().getPrimerNombre() + " " + transaccion.getUsuarioResponsable().getApellidoPaterno());
        }
        if (transaccion.getBodegaOrigen() != null) {
            dto.setBodegaOrigenNombre(transaccion.getBodegaOrigen().getNombre());
        }
        if (transaccion.getBodegaDestino() != null) {
            dto.setBodegaDestinoNombre(transaccion.getBodegaDestino().getNombre());
        }
        return dto;
    }

    public TransaccionInventario toEntity(TransaccionInventarioRequestDTO dto) {
        TransaccionInventario transaccion = new TransaccionInventario();
        transaccion.setTipo(dto.getTipo().toUpperCase());
        transaccion.setCantidad(dto.getCantidad());
        transaccion.setMotivo(dto.getMotivo());
        return transaccion;
    }
}