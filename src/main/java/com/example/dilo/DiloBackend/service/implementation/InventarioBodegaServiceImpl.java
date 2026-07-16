package com.example.dilo.DiloBackend.service.implementation;

import com.example.dilo.DiloBackend.dto.request.InventarioBodegaRequestDTO;
import com.example.dilo.DiloBackend.dto.response.InventarioBodegaResponseDTO;
import com.example.dilo.DiloBackend.exception.ResourceNotFoundException;
import com.example.dilo.DiloBackend.model.Bodega;
import com.example.dilo.DiloBackend.model.InventarioBodega;
import com.example.dilo.DiloBackend.model.Negocio;
import com.example.dilo.DiloBackend.model.Producto;
import com.example.dilo.DiloBackend.repository.BodegaRepository;
import com.example.dilo.DiloBackend.repository.InventarioBodegaRepository;
import com.example.dilo.DiloBackend.repository.NegocioRepository;
import com.example.dilo.DiloBackend.repository.ProductoRepository;
import com.example.dilo.DiloBackend.service.InventarioBodegaService;
import com.example.dilo.DiloBackend.service.mapper.InventarioBodegaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventarioBodegaServiceImpl implements InventarioBodegaService {

    private final InventarioBodegaRepository inventarioRepository;
    private final ProductoRepository productoRepository;
    private final BodegaRepository bodegaRepository;
    private final NegocioRepository negocioRepository;
    private final InventarioBodegaMapper inventarioMapper;

    @Override
    public List<InventarioBodegaResponseDTO> obtenerInventarioGeneral(Long negocioId) {
        return inventarioRepository.findByNegocioId(negocioId).stream()
                .map(inventarioMapper::toDto)
                .toList();
    }

    @Override
    public List<InventarioBodegaResponseDTO> obtenerInventarioPorBodega(Long negocioId, Long bodegaId) {
        // Validamos que la bodega exista y sea del negocio
        bodegaRepository.findByIdAndNegocioId(bodegaId, negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Bodega no encontrada en este negocio"));

        return inventarioRepository.findByBodegaIdAndNegocioId(bodegaId, negocioId).stream()
                .map(inventarioMapper::toDto)
                .toList();
    }

    @Override
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
        return inventarioMapper.toDto(guardado);
    }

    @Override
    public InventarioBodegaResponseDTO actualizarStockMinimo(Long negocioId, Long id, Integer nuevoStockMinimo) {
        InventarioBodega inventario = inventarioRepository.findByIdAndNegocioId(id, negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Registro de inventario no encontrado"));

        inventario.setStockMinimo(nuevoStockMinimo);
        InventarioBodega actualizado = inventarioRepository.save(inventario);

        return inventarioMapper.toDto(actualizado);
    }


    @Override
    public InventarioBodegaResponseDTO actualizarCantidadActual(Long negocioId, Long id, Integer nuevaCantidad) {
        InventarioBodega inventario = inventarioRepository.findByIdAndNegocioId(id, negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Registro de inventario no encontrado"));

        if (nuevaCantidad < 0) {
            throw new RuntimeException("El stock físico no puede ser negativo");
        }

        inventario.setCantidadActual(nuevaCantidad);
        InventarioBodega actualizado = inventarioRepository.save(inventario);

        return inventarioMapper.toDto(actualizado);
    }


}