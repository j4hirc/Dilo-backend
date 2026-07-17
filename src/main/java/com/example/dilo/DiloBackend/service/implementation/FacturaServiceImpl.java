package com.example.dilo.DiloBackend.service.implementation;

import com.example.dilo.DiloBackend.dto.request.DetalleFacturaRequestDTO;
import com.example.dilo.DiloBackend.dto.request.FacturaRequestDTO;
import com.example.dilo.DiloBackend.dto.request.TransaccionInventarioRequestDTO;
import com.example.dilo.DiloBackend.dto.response.FacturaResponseDTO;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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

        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        ParametroGlobal ivaParam = parametroGlobalRepository.findById("IVA_ACTUAL")
                .orElseThrow(() -> new RuntimeException("Error Crítico: El parámetro IVA_ACTUAL no está configurado en el sistema. Contacte al administrador."));

        BigDecimal porcentajeIva = new BigDecimal(ivaParam.getValor());

        BigDecimal subtotalIva0 = BigDecimal.ZERO;
        BigDecimal subtotalIvaAplicado = BigDecimal.ZERO;
        BigDecimal totalDescuento = BigDecimal.ZERO;

        List<DetalleFactura> detallesParaGuardar = new ArrayList<>();

        for (DetalleFacturaRequestDTO dto : request.getDetalles()) {
            Producto producto = productoRepository.findById(dto.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + dto.getProductoId()));

            InventarioBodega inventario = inventarioRepository.findByBodegaIdAndNegocioId(dto.getBodegaId(), negocioId).stream()
                    .filter(i -> i.getProducto().getId().equals(producto.getId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("El producto no existe en la bodega seleccionada."));

            if (inventario.getCantidadActual() < dto.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre() +
                        ". Disponible: " + inventario.getCantidadActual());
            }

            BigDecimal precio = producto.getPrecioUnitario();
            BigDecimal cantidad = new BigDecimal(dto.getCantidad());
            BigDecimal subtotalItem = precio.multiply(cantidad).setScale(2, RoundingMode.HALF_UP);

            if (producto.getGrabaIva()) {
                subtotalIvaAplicado = subtotalIvaAplicado.add(subtotalItem);
            } else {
                subtotalIva0 = subtotalIva0.add(subtotalItem);
            }

            DetalleFactura detalle = new DetalleFactura();
            detalle.setProducto(producto);

            Bodega bodega = new Bodega();
            bodega.setId(dto.getBodegaId());
            detalle.setBodega(bodega);

            detalle.setCantidad(dto.getCantidad());
            detalle.setPrecioUnitario(precio);
            detalle.setDescuento(BigDecimal.ZERO);
            detalle.setSubtotalItem(subtotalItem);

            detallesParaGuardar.add(detalle);
        }

        BigDecimal totalIva = subtotalIvaAplicado.multiply(porcentajeIva).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalFactura = subtotalIva0.add(subtotalIvaAplicado).add(totalIva).subtract(totalDescuento);

        Factura factura = new Factura();
        factura.setNegocio(negocio);
        factura.setCliente(cliente);
        factura.setUsuarioEmisor(usuario);
        factura.setNumeroFactura("TEMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        factura.setFechaEmision(LocalDateTime.now());
        factura.setSubtotalIva0(subtotalIva0);
        factura.setSubtotalIvaAplicado(subtotalIvaAplicado);
        factura.setTotalDescuento(totalDescuento);

        factura.setPorcentajeIvaAplicado(porcentajeIva.multiply(new BigDecimal("100")));

        factura.setTotalIva(totalIva);
        factura.setTotalFactura(totalFactura);
        factura.setFormaPago(request.getMetodoPago());
        factura.setEstadoSri("CREADA");

        Factura facturaGuardada = facturaRepository.save(factura);

        for (DetalleFactura detalle : detallesParaGuardar) {
            detalle.setFactura(facturaGuardada);
            detalleFacturaRepository.save(detalle);

            TransaccionInventarioRequestDTO egresoVenta = new TransaccionInventarioRequestDTO();
            egresoVenta.setTipo("EGRESO");
            egresoVenta.setProductoId(detalle.getProducto().getId());
            egresoVenta.setBodegaOrigenId(detalle.getBodega().getId());
            egresoVenta.setCantidad(detalle.getCantidad());
            egresoVenta.setMotivo("Venta según Factura #" + facturaGuardada.getNumeroFactura());

            transaccionService.registrarTransaccion(negocioId, emailUsuario, egresoVenta);
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                sriService.procesarFacturaElectronica(facturaGuardada.getId());
            }
        });

        return facturaMapper.toDto(facturaGuardada, detallesParaGuardar);
    }
}