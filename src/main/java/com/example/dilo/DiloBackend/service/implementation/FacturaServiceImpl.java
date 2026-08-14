package com.example.dilo.DiloBackend.service.implementation;

import com.example.dilo.DiloBackend.dto.request.DetalleFacturaRequestDTO;
import com.example.dilo.DiloBackend.dto.request.FacturaRequestDTO;
import com.example.dilo.DiloBackend.dto.request.TransaccionInventarioRequestDTO;
import com.example.dilo.DiloBackend.dto.response.FacturaResponseDTO;
import com.example.dilo.DiloBackend.dto.response.TransaccionInventarioResponseDTO;
import com.example.dilo.DiloBackend.exception.ResourceNotFoundException;
import com.example.dilo.DiloBackend.model.*;
import com.example.dilo.DiloBackend.repository.*;
import com.example.dilo.DiloBackend.service.FacturaService;
import com.example.dilo.DiloBackend.service.SriService;
import com.example.dilo.DiloBackend.service.TransaccionInventarioService;
import com.example.dilo.DiloBackend.service.mapper.FacturaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FacturaServiceImpl implements FacturaService {

    private final FacturaRepository facturaRepository;
    private final DetalleFacturaRepository detalleFacturaRepository;
    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;
    private final NegocioRepository negocioRepository;
    private final UsuarioRepository usuarioRepository;
    private final InventarioBodegaRepository inventarioRepository;
    private final TransaccionInventarioService transaccionService;
    private final FacturaMapper facturaMapper;
    private final SriService sriService;
    private final ParametroGlobalRepository parametroGlobalRepository;

    @Override
    @Transactional
    public FacturaResponseDTO generarFactura(Long negocioId, String emailUsuario, FacturaRequestDTO request) {

        Negocio negocio = negocioRepository.findById(negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Negocio no encontrado"));

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario emisor no encontrado"));

        Cliente cliente = null;
        if (request.getClienteId() != null) {
            cliente = clienteRepository.findById(request.getClienteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
        }

        ParametroGlobal ivaParam = parametroGlobalRepository.findById("IVA_ACTUAL")
                .orElseThrow(() -> new RuntimeException("Error Crítico: El parámetro IVA_ACTUAL no está configurado en el sistema."));

        BigDecimal porcentajeIva = new BigDecimal(ivaParam.getValor());

        BigDecimal subtotalIva0 = BigDecimal.ZERO;
        BigDecimal subtotalIvaAplicado = BigDecimal.ZERO;
        BigDecimal descuentosItems = BigDecimal.ZERO;

        List<DetalleFactura> detallesParaGuardar = new ArrayList<>();

        List<Long> productoIds = request.getDetalles().stream()
                .map(DetalleFacturaRequestDTO::getProductoId)
                .distinct()
                .toList();

        Map<Long, Producto> productosMap = productoRepository.findAllById(productoIds).stream()
                .collect(Collectors.toMap(Producto::getId, p -> p));

        for (DetalleFacturaRequestDTO dto : request.getDetalles()) {

            Producto producto = productosMap.get(dto.getProductoId());
            if (producto == null) {
                throw new ResourceNotFoundException("Producto no encontrado: " + dto.getProductoId());
            }

            InventarioBodega inventario = inventarioRepository
                    .findByBodegaIdAndNegocioIdAndProductoId(dto.getBodegaId(), negocioId, producto.getId())
                    .orElseThrow(() -> new RuntimeException("El producto '" + producto.getNombre() + "' no está registrado en la bodega seleccionada."));

            if (inventario.getCantidadActual() < dto.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre() + ". Disponible: " + inventario.getCantidadActual());
            }

            BigDecimal precio = producto.getCostoPromedioActual();
            if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
                precio = producto.getPrecioUnitario();
            }

            BigDecimal cantidad = new BigDecimal(dto.getCantidad());
            BigDecimal descuentoItem = dto.getDescuento() != null ? dto.getDescuento() : BigDecimal.ZERO;

            BigDecimal subtotalItem = precio.multiply(cantidad).subtract(descuentoItem).setScale(2, RoundingMode.HALF_UP);

            if (subtotalItem.compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException("El descuento no puede ser mayor al valor total del producto: " + producto.getNombre());
            }

            if (Boolean.TRUE.equals(producto.getGrabaIva())) {
                subtotalIvaAplicado = subtotalIvaAplicado.add(subtotalItem);
            } else {
                subtotalIva0 = subtotalIva0.add(subtotalItem);
            }

            descuentosItems = descuentosItems.add(descuentoItem);

            DetalleFactura detalle = new DetalleFactura();
            detalle.setProducto(producto);
            Bodega bodega = new Bodega();
            bodega.setId(dto.getBodegaId());
            detalle.setBodega(bodega);
            detalle.setCantidad(dto.getCantidad());
            detalle.setPrecioUnitario(precio);
            detalle.setDescuento(descuentoItem);
            detalle.setSubtotalItem(subtotalItem);

            detallesParaGuardar.add(detalle);
        }

        // ========== DESCUENTO GLOBAL (prorrateado como en el frontend) ==========
        BigDecimal descuentoGlobal = request.getDescuentoGlobal() != null
                ? request.getDescuentoGlobal()
                : BigDecimal.ZERO;

        if (descuentoGlobal.compareTo(BigDecimal.ZERO) < 0) {
            descuentoGlobal = BigDecimal.ZERO;
        }

        BigDecimal totalBruto = subtotalIva0.add(subtotalIvaAplicado);

        // No permitir que el descuento global supere el bruto
        if (descuentoGlobal.compareTo(totalBruto) > 0) {
            descuentoGlobal = totalBruto;
        }

        BigDecimal descSobreGravado = BigDecimal.ZERO;
        BigDecimal descSobreExento = BigDecimal.ZERO;

        if (totalBruto.compareTo(BigDecimal.ZERO) > 0 && descuentoGlobal.compareTo(BigDecimal.ZERO) > 0) {
            // Prorrateo proporcional
            descSobreGravado = descuentoGlobal
                    .multiply(subtotalIvaAplicado)
                    .divide(totalBruto, 4, RoundingMode.HALF_UP)
                    .setScale(2, RoundingMode.HALF_UP);

            descSobreExento = descuentoGlobal.subtract(descSobreGravado).setScale(2, RoundingMode.HALF_UP);
        }

        // Bases netas (después del descuento global)
        BigDecimal baseImponible = subtotalIvaAplicado.subtract(descSobreGravado).max(BigDecimal.ZERO);
        BigDecimal baseExenta   = subtotalIva0.subtract(descSobreExento).max(BigDecimal.ZERO);

        // IVA solo sobre la base gravada neta
        BigDecimal totalIva = baseImponible.multiply(porcentajeIva).setScale(2, RoundingMode.HALF_UP);

        // Total final
        BigDecimal totalFactura = baseImponible.add(baseExenta).add(totalIva);

        // Total de descuentos (ítems + global) para persistir
        BigDecimal totalDescuentosGenerales = descuentosItems.add(descuentoGlobal);

        // ========== PERSISTENCIA ==========
        Factura factura = new Factura();
        factura.setNegocio(negocio);
        factura.setCliente(cliente);
        factura.setUsuarioEmisor(usuario);
        factura.setNumeroFactura("TEMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        factura.setFechaEmision(LocalDateTime.now());

        // Guardamos las bases YA netas (después del descuento global)
        // Así el listado y el PDF coinciden con lo que vio el usuario
        factura.setSubtotalIva0(baseExenta);
        factura.setSubtotalIvaAplicado(baseImponible);
        factura.setTotalDescuento(totalDescuentosGenerales);
        factura.setPorcentajeIvaAplicado(porcentajeIva.multiply(new BigDecimal("100")));
        factura.setTotalIva(totalIva);
        factura.setTotalFactura(totalFactura);
        factura.setFormaPago(request.getMetodoPago());
        factura.setEstadoSri("CREADA");
        factura.setNumeroCuotas(request.getNumeroCuotas() != null ? request.getNumeroCuotas() : 0);
        factura.setDetallesTarjeta(request.getTarjeta());

        Factura facturaGuardada = facturaRepository.save(factura);

        List<TransaccionInventarioRequestDTO> egresosVenta = new ArrayList<>();

        for (DetalleFactura detalle : detallesParaGuardar) {
            detalle.setFactura(facturaGuardada);

            TransaccionInventarioRequestDTO egresoVenta = new TransaccionInventarioRequestDTO();
            egresoVenta.setTipo("EGRESO");
            egresoVenta.setProductoId(detalle.getProducto().getId());
            egresoVenta.setBodegaOrigenId(detalle.getBodega().getId());
            egresoVenta.setCantidad(detalle.getCantidad());
            egresoVenta.setMotivo("Venta según Factura #" + facturaGuardada.getNumeroFactura());

            egresosVenta.add(egresoVenta);
        }

        List<TransaccionInventarioResponseDTO> respuestasTx =
                transaccionService.registrarEgresosBatch(negocioId, emailUsuario, egresosVenta);

        for (int i = 0; i < detallesParaGuardar.size(); i++) {
            detallesParaGuardar.get(i).setCostoUnitarioReal(respuestasTx.get(i).getCostoUnitario());
            detallesParaGuardar.get(i).setCostoTotalReal(respuestasTx.get(i).getCostoTotal());
        }

        detalleFacturaRepository.saveAll(detallesParaGuardar);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                sriService.procesarFacturaElectronica(facturaGuardada.getId());
            }
        });

        return facturaMapper.toDto(facturaGuardada, detallesParaGuardar);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacturaResponseDTO> obtenerFacturasPorNegocio(Long negocioId) {
        negocioRepository.findById(negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Negocio no encontrado"));

        List<Factura> facturas = facturaRepository.findByNegocioIdOrderByFechaEmisionDesc(negocioId);

        return facturas.stream()
                .map(factura -> {
                    List<DetalleFactura> detalles = detalleFacturaRepository.findByFacturaId(factura.getId());
                    return facturaMapper.toDto(factura, detalles);
                })
                .toList();
    }
}