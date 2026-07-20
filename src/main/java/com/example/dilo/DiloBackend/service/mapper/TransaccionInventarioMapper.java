package com.example.dilo.DiloBackend.service.mapper;

import com.example.dilo.DiloBackend.dto.request.TransaccionInventarioRequestDTO;
import com.example.dilo.DiloBackend.dto.response.TransaccionInventarioResponseDTO;
import com.example.dilo.DiloBackend.model.TransaccionInventario;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

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

        // --- MAPEO DE NUEVOS CAMPOS ---
        dto.setCostoUnitario(transaccion.getCostoUnitario());
        dto.setCostoTotal(transaccion.getCostoTotal());
        dto.setMetodoAplicado(transaccion.getMetodoAplicado());
        dto.setDocumentoReferencia(transaccion.getDocumentoReferencia());

        if (transaccion.getLote() != null) {
            dto.setNumeroLote(transaccion.getLote().getNumeroLote());
        }

        return dto;
    }

    public TransaccionInventario toEntity(TransaccionInventarioRequestDTO dto) {
        TransaccionInventario transaccion = new TransaccionInventario();
        transaccion.setTipo(dto.getTipo().toUpperCase());
        transaccion.setCantidad(dto.getCantidad());
        transaccion.setMotivo(dto.getMotivo());

        // --- MAPEO DE NUEVOS CAMPOS ---
        transaccion.setCostoUnitario(dto.getCostoUnitario());
        transaccion.setDocumentoReferencia(dto.getDocumentoReferencia());

        // Cálculo preliminar del costo total (si el service no lo sobrescribe después)
        if (dto.getCostoUnitario() != null && dto.getCantidad() != null) {
            transaccion.setCostoTotal(dto.getCostoUnitario().multiply(new BigDecimal(dto.getCantidad())));
        }

        return transaccion;
    }
}