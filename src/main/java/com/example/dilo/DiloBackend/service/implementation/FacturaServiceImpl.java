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
import java.util.UUID;

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

        // IVA_ACTUAL suele guardarse como 0.15 o como 15 → normalizamos a fracción
        BigDecimal porcentajeIva = new BigDecimal(ivaParam.getValor());
        if (porcentajeIva.compareTo(BigDecimal.ONE) > 0) {
            porcentajeIva = porcentajeIva.divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);
        }

        BigDecimal subtotalIva0 = BigDecimal.ZERO;
        BigDecimal subtotalIvaAplicado = BigDecimal.ZERO;
        BigDecimal descuentosItems = BigDecimal.ZERO;

        List<DetalleFactura> detallesParaGuardar = new ArrayList<>();

        for (DetalleFacturaRequestDTO dto : request.getDetalles()) {
            Producto producto = productoRepository.findById(dto.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + dto.getProductoId()));

            InventarioBodega inventario = inventarioRepository
                    .findByBodegaIdAndNegocioIdAndProductoId(dto.getBodegaId(), negocioId, producto.getId())
                    .orElseThrow(() -> new RuntimeException(
                            "El producto '" + producto.getNombre() + "' no está registrado en la bodega seleccionada."));

            if (inventario.getCantidadActual() < dto.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre()
                        + ". Disponible: " + inventario.getCantidadActual());
            }

            // 🔥 PRECIO DE VENTA (PVP) — NUNCA el costo promedio
            // El costo promedio solo se usa en el egreso de inventario (COGS).
            BigDecimal precio = producto.getPrecioUnitario();
            if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException(
                        "El producto '" + producto.getNombre() + "' no tiene precio de venta (PVP) configurado.");
            }

            BigDecimal cantidad = new BigDecimal(dto.getCantidad());
            BigDecimal descuentoItem = dto.getDescuento() != null ? dto.getDescuento() : BigDecimal.ZERO;
            if (descuentoItem.compareTo(BigDecimal.ZERO) < 0) {
                descuentoItem = BigDecimal.ZERO;
            }

            BigDecimal subtotalItem = precio.multiply(cantidad).subtract(descuentoItem)
                    .setScale(2, RoundingMode.HALF_UP);

            if (subtotalItem.compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException(
                        "El descuento no puede ser mayor al valor total del producto: " + producto.getNombre());
            }

            // Precios se tratan SIN IVA incluido (base + IVA = total)
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
            detalle.setPrecioUnitario(precio); // PVP
            detalle.setDescuento(descuentoItem);
            detalle.setSubtotalItem(subtotalItem);

            detallesParaGuardar.add(detalle);
        }

        BigDecimal descuentoGlobal = request.getDescuentoGlobal() != null
                ? request.getDescuentoGlobal()
                : BigDecimal.ZERO;
        if (descuentoGlobal.compareTo(BigDecimal.ZERO) < 0) {
            descuentoGlobal = BigDecimal.ZERO;
        }

        BigDecimal sumaBases = subtotalIva0.add(subtotalIvaAplicado);
        if (descuentoGlobal.compareTo(sumaBases) > 0) {
            throw new RuntimeException("El descuento global no puede superar el total de la factura.");
        }

        // Prorratear descuento global sobre bases gravada / 0%
        BigDecimal baseGravada = subtotalIvaAplicado;
        BigDecimal base0 = subtotalIva0;
        if (descuentoGlobal.compareTo(BigDecimal.ZERO) > 0 && sumaBases.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal descSobreGravada = descuentoGlobal
                    .multiply(subtotalIvaAplicado)
                    .divide(sumaBases, 6, RoundingMode.HALF_UP)
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal descSobre0 = descuentoGlobal.subtract(descSobreGravada).setScale(2, RoundingMode.HALF_UP);
            baseGravada = subtotalIvaAplicado.subtract(descSobreGravada).max(BigDecimal.ZERO);
            base0 = subtotalIva0.subtract(descSobre0).max(BigDecimal.ZERO);
        }

        BigDecimal totalIva = baseGravada.multiply(porcentajeIva).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalFactura = base0.add(baseGravada).add(totalIva).setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalDescuentosGenerales = descuentosItems.add(descuentoGlobal);

        Factura factura = new Factura();
        factura.setNegocio(negocio);
        factura.setCliente(cliente);
        factura.setUsuarioEmisor(usuario);
        factura.setNumeroFactura("TEMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        factura.setFechaEmision(LocalDateTime.now());
        // Bases ANTES del descuento global (lo usual en reportes); el total ya refleja el descuento
        factura.setSubtotalIva0(subtotalIva0);
        factura.setSubtotalIvaAplicado(subtotalIvaAplicado);
        factura.setTotalDescuento(totalDescuentosGenerales);
        factura.setPorcentajeIvaAplicado(porcentajeIva.multiply(new BigDecimal("100")));
        factura.setTotalIva(totalIva);
        factura.setTotalFactura(totalFactura);
        factura.setFormaPago(request.getMetodoPago());
        factura.setEstadoSri("CREADA");
        factura.setNumeroCuotas(request.getNumeroCuotas() != null ? request.getNumeroCuotas() : 0);
        factura.setDetallesTarjeta(request.getTarjeta());

        Factura facturaGuardada = facturaRepository.save(factura);

        for (DetalleFactura detalle : detallesParaGuardar) {
            detalle.setFactura(facturaGuardada);

            TransaccionInventarioRequestDTO egresoVenta = new TransaccionInventarioRequestDTO();
            egresoVenta.setTipo("EGRESO");
            egresoVenta.setProductoId(detalle.getProducto().getId());
            egresoVenta.setBodegaOrigenId(detalle.getBodega().getId());
            egresoVenta.setCantidad(detalle.getCantidad());
            egresoVenta.setMotivo("Venta según Factura #" + facturaGuardada.getNumeroFactura());

            TransaccionInventarioResponseDTO respuestaTx =
                    transaccionService.registrarTransaccion(negocioId, emailUsuario, egresoVenta);

            detalle.setCostoUnitarioReal(respuestaTx.getCostoUnitario());
            detalle.setCostoTotalReal(respuestaTx.getCostoTotal());

            detalleFacturaRepository.save(detalle);
        }

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
