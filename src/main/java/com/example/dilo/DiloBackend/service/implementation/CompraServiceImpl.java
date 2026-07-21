package com.example.dilo.DiloBackend.service.implementation;

import com.example.dilo.DiloBackend.dto.request.CompraRequestDTO;
import com.example.dilo.DiloBackend.dto.request.DetalleCompraRequestDTO;
import com.example.dilo.DiloBackend.dto.response.CompraResponseDTO;
import com.example.dilo.DiloBackend.exception.ResourceNotFoundException;
import com.example.dilo.DiloBackend.model.*;
import com.example.dilo.DiloBackend.repository.*;
import com.example.dilo.DiloBackend.service.CompraService;
import com.example.dilo.DiloBackend.service.mapper.CompraMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompraServiceImpl implements CompraService {

    private final CompraRepository compraRepository;
    private final ProveedorRepository proveedorRepository;
    private final BodegaRepository bodegaRepository;
    private final ProductoRepository productoRepository;
    private final NegocioRepository negocioRepository;
    private final UsuarioRepository usuarioRepository;
    private final LoteRepository loteRepository;
    private final InventarioBodegaRepository inventarioRepository;
    private final TransaccionInventarioRepository transaccionRepository;
    private final CompraMapper compraMapper;

    @Override
    @Transactional
    public CompraResponseDTO registrarCompra(Long negocioId, String emailUsuario, CompraRequestDTO requestDTO) {
        Negocio negocio = negocioRepository.findById(negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Negocio no encontrado"));

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Proveedor proveedor = proveedorRepository.findById(requestDTO.getProveedorId())
                .filter(p -> p.getNegocio().getId().equals(negocioId))
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado o no pertenece al negocio"));

        Bodega bodega = bodegaRepository.findById(requestDTO.getBodegaIngresoId())
                .filter(b -> b.getNegocio().getId().equals(negocioId))
                .orElseThrow(() -> new ResourceNotFoundException("Bodega no encontrada"));

        Compra compra = new Compra();
        compra.setNegocio(negocio);
        compra.setProveedor(proveedor);
        compra.setBodegaIngreso(bodega);
        compra.setNumeroComprobante(requestDTO.getNumeroComprobante());
        compra.setFechaCompra(LocalDateTime.now());

        BigDecimal totalCompra = BigDecimal.ZERO;
        List<Lote> lotesGenerados = new ArrayList<>();

        Compra compraGuardada = compraRepository.save(compra);

        // 🔥 OBTENEMOS LA CANTIDAD DE LOTES DEL NEGOCIO UNA SOLA VEZ ANTES DEL BUCLE
        long cantidadLotesActuales = loteRepository.countByNegocioId(negocioId);

        for (DetalleCompraRequestDTO detalle : requestDTO.getDetalles()) {
            Producto producto = productoRepository.findById(detalle.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + detalle.getProductoId()));

            BigDecimal cantidad = new BigDecimal(detalle.getCantidad());
            BigDecimal costoUnitario = detalle.getCostoUnitario();
            BigDecimal costoTotalLote = costoUnitario.multiply(cantidad);

            totalCompra = totalCompra.add(costoTotalLote);

            // 🔥 AUMENTAMOS EL CONTADOR Y GENERAMOS EL CÓDIGO DEL LOTE
            cantidadLotesActuales++;
            String codigoLoteGenerado = String.format("LOTE-%05d", cantidadLotesActuales);

            Lote lote = new Lote();
            lote.setNegocio(negocio);
            lote.setProducto(producto);
            lote.setBodega(bodega);
            lote.setCompra(compraGuardada);
            lote.setNumeroLote(codigoLoteGenerado); // 🔥 SE LO ASIGNAMOS AQUÍ
            lote.setCantidadInicial(cantidad);
            lote.setCantidadDisponible(cantidad);
            lote.setCostoUnitario(costoUnitario);
            lote.setCostoTotal(costoTotalLote);
            lote.setFechaIngreso(LocalDateTime.now());
            lote.setFechaCaducidad(detalle.getFechaCaducidad());
            lote.setEstado("ACTIVO");

            Lote loteGuardado = loteRepository.save(lote);
            lotesGenerados.add(loteGuardado);

            InventarioBodega inventario = obtenerOCrearInventario(producto, bodega, negocioId);
            int stockAnterior = inventario.getCantidadActual();
            inventario.setCantidadActual(stockAnterior + detalle.getCantidad());
            inventarioRepository.save(inventario);

            recalcularCostoPromedioProducto(producto, cantidad, costoUnitario, inventario.getCantidadActual());

            TransaccionInventario transaccion = new TransaccionInventario();
            transaccion.setNegocio(negocio);
            transaccion.setProducto(producto);
            transaccion.setBodegaDestino(bodega);
            transaccion.setUsuarioResponsable(usuario);
            transaccion.setLote(loteGuardado);
            transaccion.setTipo("INGRESO");
            transaccion.setCantidad(detalle.getCantidad());
            transaccion.setMotivo("Compra a proveedor: " + proveedor.getNombre());
            transaccion.setCostoUnitario(costoUnitario);
            transaccion.setCostoTotal(costoTotalLote);
            transaccion.setMetodoAplicado(negocio.getMetodoCosteo() != null ? negocio.getMetodoCosteo() : "PROMEDIO");
            transaccion.setDocumentoReferencia(requestDTO.getNumeroComprobante());
            transaccion.setFechaTransaccion(LocalDateTime.now());

            transaccionRepository.save(transaccion);
        }

        compraGuardada.setTotalCompra(totalCompra);
        compraGuardada = compraRepository.save(compraGuardada);

        return compraMapper.toDto(compraGuardada, lotesGenerados);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompraResponseDTO> obtenerComprasPorNegocio(Long negocioId) {
        List<Compra> compras = compraRepository.findByNegocioIdOrderByFechaCompraDesc(negocioId);

        return compras.stream()
                .map(compra -> {
                    List<Lote> lotesDeEstaCompra = loteRepository.findByCompraId(compra.getId());
                    return compraMapper.toDto(compra, lotesDeEstaCompra);
                })
                .toList();
    }

    private InventarioBodega obtenerOCrearInventario(Producto producto, Bodega bodega, Long negocioId) {
        return inventarioRepository.findByBodegaIdAndNegocioId(bodega.getId(), negocioId).stream()
                .filter(i -> i.getProducto().getId().equals(producto.getId()))
                .findFirst()
                .orElseGet(() -> {
                    InventarioBodega nuevoInventario = new InventarioBodega();
                    nuevoInventario.setProducto(producto);
                    nuevoInventario.setBodega(bodega);
                    nuevoInventario.setNegocio(bodega.getNegocio());
                    nuevoInventario.setCantidadActual(0);
                    nuevoInventario.setStockMinimo(5);
                    return nuevoInventario;
                });
    }

    private void recalcularCostoPromedioProducto(Producto producto, BigDecimal cantidadIngresada, BigDecimal costoUnitarioIngreso, int nuevoStockFisicoTotal) {
        if (nuevoStockFisicoTotal == 0) return;

        BigDecimal stockAnterior = new BigDecimal(nuevoStockFisicoTotal).subtract(cantidadIngresada);
        BigDecimal costoPromedioAnterior = producto.getCostoPromedioActual() != null ? producto.getCostoPromedioActual() : BigDecimal.ZERO;

        BigDecimal valorInventarioAnterior = stockAnterior.multiply(costoPromedioAnterior);
        BigDecimal valorNuevoIngreso = cantidadIngresada.multiply(costoUnitarioIngreso);

        BigDecimal nuevoCostoPromedio = valorInventarioAnterior.add(valorNuevoIngreso)
                .divide(new BigDecimal(nuevoStockFisicoTotal), 4, RoundingMode.HALF_UP);

        producto.setCostoPromedioActual(nuevoCostoPromedio);
        productoRepository.save(producto);
    }
}