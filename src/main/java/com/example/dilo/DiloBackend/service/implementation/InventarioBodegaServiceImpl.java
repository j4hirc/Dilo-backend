package com.example.dilo.DiloBackend.service.implementation;

import com.example.dilo.DiloBackend.dto.request.InventarioBodegaRequestDTO;
import com.example.dilo.DiloBackend.dto.response.InventarioBodegaResponseDTO;
import com.example.dilo.DiloBackend.exception.ResourceNotFoundException;
import com.example.dilo.DiloBackend.model.*;
import com.example.dilo.DiloBackend.repository.*;
import com.example.dilo.DiloBackend.service.InventarioBodegaService;
import com.example.dilo.DiloBackend.service.mapper.InventarioBodegaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventarioBodegaServiceImpl implements InventarioBodegaService {

    private final InventarioBodegaRepository inventarioRepository;
    private final ProductoRepository productoRepository;
    private final BodegaRepository bodegaRepository;
    private final NegocioRepository negocioRepository;
    private final InventarioBodegaMapper inventarioMapper;

    // Inyectamos los nuevos repositorios
    private final LoteRepository loteRepository;
    private final TransaccionInventarioRepository transaccionRepository;

    @Override
    public List<InventarioBodegaResponseDTO> obtenerInventarioGeneral(Long negocioId) {
        return inventarioRepository.findByNegocioId(negocioId).stream()
                .map(inventarioMapper::toDto)
                .toList();
    }

    @Override
    public List<InventarioBodegaResponseDTO> obtenerInventarioPorBodega(Long negocioId, Long bodegaId) {
        bodegaRepository.findByIdAndNegocioId(bodegaId, negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Bodega no encontrada en este negocio"));

        return inventarioRepository.findByBodegaIdAndNegocioId(bodegaId, negocioId).stream()
                .map(inventarioMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public InventarioBodegaResponseDTO inicializarInventario(Long negocioId, InventarioBodegaRequestDTO requestDTO) {
        Negocio negocio = negocioRepository.findById(negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Negocio no encontrado"));

        Producto producto = productoRepository.findByIdAndNegocioId(requestDTO.getProductoId(), negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado en este negocio"));

        Bodega bodega = bodegaRepository.findByIdAndNegocioId(requestDTO.getBodegaId(), negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Bodega no encontrada en este negocio"));

        if (inventarioRepository.existsByProductoIdAndBodegaIdAndNegocioId(
                producto.getId(), bodega.getId(), negocioId)) {
            throw new RuntimeException("El producto ya está registrado en esta bodega. Utilice el módulo de transacciones para modificar el stock.");
        }

        InventarioBodega inventario = inventarioMapper.toEntity(requestDTO);
        inventario.setProducto(producto);
        inventario.setBodega(bodega);
        inventario.setNegocio(negocio);

        InventarioBodega guardado = inventarioRepository.save(inventario);

        if (requestDTO.getCantidadActual() > 0) {
            BigDecimal costoUnitario = requestDTO.getCostoUnitarioInicial() != null
                    ? requestDTO.getCostoUnitarioInicial()
                    : BigDecimal.ZERO;

            BigDecimal cantidadBD = new BigDecimal(requestDTO.getCantidadActual());

            Lote loteInicial = new Lote();
            loteInicial.setNegocio(negocio);
            loteInicial.setProducto(producto);
            loteInicial.setBodega(bodega);
            loteInicial.setCantidadInicial(cantidadBD);
            loteInicial.setCantidadDisponible(cantidadBD);
            loteInicial.setCostoUnitario(costoUnitario);
            loteInicial.setCostoTotal(costoUnitario.multiply(cantidadBD));
            loteInicial.setFechaIngreso(LocalDateTime.now());
            loteInicial.setEstado("ACTIVO");

            loteInicial = loteRepository.save(loteInicial);

            if (producto.getCostoPromedioActual() == null || producto.getCostoPromedioActual().compareTo(BigDecimal.ZERO) == 0) {
                producto.setCostoPromedioActual(costoUnitario);
                productoRepository.save(producto);
            }

            TransaccionInventario transaccion = new TransaccionInventario();
            transaccion.setNegocio(negocio);
            transaccion.setProducto(producto);
            transaccion.setBodegaDestino(bodega);
            transaccion.setLote(loteInicial);
            transaccion.setTipo("AJUSTE_POSITIVO");
            transaccion.setCantidad(requestDTO.getCantidadActual());
            transaccion.setMotivo("Inicialización de inventario");
            transaccion.setCostoUnitario(costoUnitario);
            transaccion.setCostoTotal(loteInicial.getCostoTotal());
            transaccion.setMetodoAplicado(negocio.getMetodoCosteo());
            transaccion.setDocumentoReferencia("INIT-001");
            transaccion.setFechaTransaccion(LocalDateTime.now());

            transaccionRepository.save(transaccion);
        }

        return inventarioMapper.toDto(guardado);
    }

    @Override
    @Transactional
    public InventarioBodegaResponseDTO actualizarStockMinimo(Long negocioId, Long id, Integer nuevoStockMinimo) {
        InventarioBodega inventario = inventarioRepository.findByIdAndNegocioId(id, negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Registro de inventario no encontrado"));

        inventario.setStockMinimo(nuevoStockMinimo);
        InventarioBodega actualizado = inventarioRepository.save(inventario);

        return inventarioMapper.toDto(actualizado);
    }

    @Override
    @Transactional
    public InventarioBodegaResponseDTO actualizarCantidadActual(Long negocioId, Long id, Integer nuevaCantidad) {
        InventarioBodega inventario = inventarioRepository.findByIdAndNegocioId(id, negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Registro de inventario no encontrado"));

        if (nuevaCantidad < 0) {
            throw new RuntimeException("El stock físico no puede ser negativo");
        }

        // ATENCIÓN: Al implementar lotes, modificar el stock directamente desde aquí desbalancea
        // la relación física (cantidad_actual) vs la contable (lotes disponibles).
        // Se mantiene este método por compatibilidad, pero en el futuro los ajustes manuales
        // deberían pasar por TransaccionInventarioService (tipo AJUSTE).
        inventario.setCantidadActual(nuevaCantidad);
        InventarioBodega actualizado = inventarioRepository.save(inventario);

        return inventarioMapper.toDto(actualizado);
    }
}