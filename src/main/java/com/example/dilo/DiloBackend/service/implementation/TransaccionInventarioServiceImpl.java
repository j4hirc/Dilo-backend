package com.example.dilo.DiloBackend.service.implementation;

import com.example.dilo.DiloBackend.dto.request.TransaccionInventarioRequestDTO;
import com.example.dilo.DiloBackend.dto.response.TransaccionInventarioResponseDTO;
import com.example.dilo.DiloBackend.exception.ResourceNotFoundException;
import com.example.dilo.DiloBackend.model.*;
import com.example.dilo.DiloBackend.repository.*;
import com.example.dilo.DiloBackend.service.EmailService;
import com.example.dilo.DiloBackend.service.TransaccionInventarioService;
import com.example.dilo.DiloBackend.service.mapper.TransaccionInventarioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransaccionInventarioServiceImpl implements TransaccionInventarioService {

    private final TransaccionInventarioRepository transaccionRepository;
    private final InventarioBodegaRepository inventarioRepository;
    private final ProductoRepository productoRepository;
    private final BodegaRepository bodegaRepository;
    private final NegocioRepository negocioRepository;
    private final UsuarioRepository usuarioRepository;
    private final TransaccionInventarioMapper transaccionMapper;
    private final EmailService emailService;
    private final MiembroNegocioRepository miembroNegocioRepository;
    private final LoteRepository loteRepository;
    private static final ZoneId ZONA_ECUADOR = ZoneId.of("America/Guayaquil");

    @Override
    @Transactional(readOnly = true)
    public List<TransaccionInventarioResponseDTO> obtenerKardexGeneral(Long negocioId) {
        return transaccionRepository.obtenerKardexPorNegocio(negocioId).stream()
                .map(transaccionMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransaccionInventarioResponseDTO> obtenerKardexPorProducto(Long negocioId, Long productoId) {
        return transaccionRepository.obtenerKardexPorProducto(negocioId, productoId).stream()
                .map(transaccionMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public TransaccionInventarioResponseDTO registrarTransaccion(Long negocioId, String emailUsuario, TransaccionInventarioRequestDTO requestDTO) {
        Negocio negocio = negocioRepository.findById(negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Negocio no encontrado"));

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario responsable no encontrado"));

        Producto producto = productoRepository.findById(requestDTO.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        TransaccionInventario transaccion = transaccionMapper.toEntity(requestDTO);
        transaccion.setNegocio(negocio);
        transaccion.setProducto(producto);
        transaccion.setUsuarioResponsable(usuario);
        transaccion.setFechaTransaccion(LocalDateTime.now(ZONA_ECUADOR));

        String metodoCosteo = (negocio.getMetodoCosteo() != null) ? negocio.getMetodoCosteo() : "PROMEDIO";
        transaccion.setMetodoAplicado(metodoCosteo);

        String tipo = requestDTO.getTipo().toUpperCase();

        switch (tipo) {
            case "INGRESO":
                procesarIngreso(transaccion, requestDTO, negocioId, producto);
                break;
            case "EGRESO":
                procesarEgreso(transaccion, requestDTO, negocioId, producto, metodoCosteo);
                break;
            case "TRANSFERENCIA":
                procesarTransferencia(transaccion, requestDTO, negocioId, producto, metodoCosteo);
                break;
            default:
                throw new RuntimeException("Tipo de transacción no válido.");
        }

        TransaccionInventario guardada = transaccionRepository.save(transaccion);
        return transaccionMapper.toDto(guardada);
    }

    @Override
    @Transactional
    public List<TransaccionInventarioResponseDTO> registrarEgresosBatch(Long negocioId, String emailUsuario, List<TransaccionInventarioRequestDTO> requests) {
        if (requests == null || requests.isEmpty()) return new ArrayList<>();

        Negocio negocio = negocioRepository.findById(negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Negocio no encontrado"));

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario responsable no encontrado"));

        String metodoCosteo = (negocio.getMetodoCosteo() != null) ? negocio.getMetodoCosteo() : "PROMEDIO";

        List<Long> productoIds = requests.stream().map(TransaccionInventarioRequestDTO::getProductoId).distinct().toList();
        List<Long> bodegaIds = requests.stream().map(TransaccionInventarioRequestDTO::getBodegaOrigenId).distinct().toList();

        Map<Long, Producto> productosMap = productoRepository.findAllById(productoIds).stream()
                .collect(Collectors.toMap(Producto::getId, p -> p));

        Map<Long, Bodega> bodegasMap = bodegaRepository.findAllById(bodegaIds).stream()
                .collect(Collectors.toMap(Bodega::getId, b -> b));

        Map<String, InventarioBodega> inventariosMap = inventarioRepository
                .findByNegocioBodegasAndProductos(negocioId, bodegaIds, productoIds).stream()
                .collect(Collectors.toMap(i -> i.getBodega().getId() + "_" + i.getProducto().getId(), i -> i));

        Map<String, List<Lote>> lotesMap = loteRepository
                .findLotesActivosBatch(negocioId, bodegaIds, productoIds).stream()
                .collect(Collectors.groupingBy(l -> l.getBodega().getId() + "_" + l.getProducto().getId()));

        List<TransaccionInventario> transaccionesParaGuardar = new ArrayList<>();
        Set<InventarioBodega> inventariosParaGuardar = new HashSet<>();
        Set<Lote> lotesParaGuardar = new HashSet<>();

        for (TransaccionInventarioRequestDTO dto : requests) {
            Producto producto = productosMap.get(dto.getProductoId());
            Bodega bodegaOrigen = bodegasMap.get(dto.getBodegaOrigenId());
            String key = dto.getBodegaOrigenId() + "_" + dto.getProductoId();

            InventarioBodega inventario = inventariosMap.get(key);
            if (inventario == null || inventario.getCantidadActual() < dto.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre());
            }

            List<Lote> lotesDisponibles = lotesMap.getOrDefault(key, new ArrayList<>());
            if ("LIFO".equals(metodoCosteo)) {
                lotesDisponibles.sort((a, b) -> b.getFechaIngreso().compareTo(a.getFechaIngreso())); // DESC
            } else {
                lotesDisponibles.sort(Comparator.comparing(Lote::getFechaIngreso)); // ASC
            }

            int cantidadRequerida = dto.getCantidad();
            BigDecimal costoTotalEgreso = BigDecimal.ZERO;
            Lote ultimoLoteTocado = null;

            for (Lote lote : lotesDisponibles) {
                if (cantidadRequerida <= 0) break;
                if (lote.getCantidadDisponible().compareTo(BigDecimal.ZERO) <= 0) continue; // Saltar si ya se agotó en RAM

                int cantidadEnLote = lote.getCantidadDisponible().intValue();
                int cantidadATomar = Math.min(cantidadRequerida, cantidadEnLote);

                lote.setCantidadDisponible(lote.getCantidadDisponible().subtract(new BigDecimal(cantidadATomar)));
                if (lote.getCantidadDisponible().compareTo(BigDecimal.ZERO) == 0) {
                    lote.setEstado("AGOTADO");
                }
                lotesParaGuardar.add(lote);

                BigDecimal costoAAplicar = "PROMEDIO".equals(metodoCosteo) ? producto.getCostoPromedioActual() : lote.getCostoUnitario();
                costoTotalEgreso = costoTotalEgreso.add(costoAAplicar.multiply(new BigDecimal(cantidadATomar)));

                cantidadRequerida -= cantidadATomar;
                ultimoLoteTocado = lote;
            }

            if (cantidadRequerida > 0) {
                System.out.println("ADVERTENCIA: Inconsistencia de lotes en producto ID " + producto.getId() + ". Faltan " + cantidadRequerida);
                BigDecimal costoFaltante = producto.getCostoPromedioActual() != null ? producto.getCostoPromedioActual() : BigDecimal.ZERO;
                costoTotalEgreso = costoTotalEgreso.add(costoFaltante.multiply(new BigDecimal(cantidadRequerida)));
            }

            inventario.setCantidadActual(inventario.getCantidadActual() - dto.getCantidad());
            inventariosParaGuardar.add(inventario);

            TransaccionInventario transaccion = transaccionMapper.toEntity(dto);
            transaccion.setNegocio(negocio);
            transaccion.setProducto(producto);
            transaccion.setBodegaOrigen(bodegaOrigen);
            transaccion.setUsuarioResponsable(usuario);
            transaccion.setFechaTransaccion(LocalDateTime.now(ZONA_ECUADOR));
            transaccion.setMetodoAplicado(metodoCosteo);
            transaccion.setTipo("EGRESO");
            transaccion.setCantidad(dto.getCantidad());
            transaccion.setMotivo(dto.getMotivo());
            transaccion.setCostoTotal(costoTotalEgreso);
            transaccion.setCostoUnitario(costoTotalEgreso.divide(new BigDecimal(dto.getCantidad()), 4, RoundingMode.HALF_UP));
            transaccion.setLote(ultimoLoteTocado);

            transaccionesParaGuardar.add(transaccion);

            verificarStockCritico(inventario, negocioId);
        }

        loteRepository.saveAll(lotesParaGuardar);
        inventarioRepository.saveAll(inventariosParaGuardar);
        List<TransaccionInventario> guardadas = transaccionRepository.saveAll(transaccionesParaGuardar);

        return guardadas.stream().map(transaccionMapper::toDto).toList();
    }

    private void procesarIngreso(TransaccionInventario transaccion, TransaccionInventarioRequestDTO dto, Long negocioId, Producto producto) {
        Bodega bodegaDestino = buscarBodega(dto.getBodegaDestinoId(), negocioId, "destino");
        transaccion.setBodegaDestino(bodegaDestino);

        InventarioBodega inventario = obtenerOCrearInventario(producto, bodegaDestino, negocioId);
        inventario.setCantidadActual(inventario.getCantidadActual() + dto.getCantidad());
        inventarioRepository.save(inventario);

        BigDecimal costoUnitario = dto.getCostoUnitario() != null ? dto.getCostoUnitario() : producto.getCostoPromedioActual();
        BigDecimal cantidad = new BigDecimal(dto.getCantidad());
        BigDecimal costoTotal = costoUnitario.multiply(cantidad);

        long cantidadLotesActuales = loteRepository.countByNegocioId(negocioId);
        String codigoLoteGenerado = String.format("LOTE-%05d", cantidadLotesActuales + 1);

        Lote nuevoLote = new Lote();
        nuevoLote.setNegocio(bodegaDestino.getNegocio());
        nuevoLote.setProducto(producto);
        nuevoLote.setBodega(bodegaDestino);
        nuevoLote.setNumeroLote(codigoLoteGenerado);
        nuevoLote.setCantidadInicial(cantidad);
        nuevoLote.setCantidadDisponible(cantidad);
        nuevoLote.setCostoUnitario(costoUnitario);
        nuevoLote.setCostoTotal(costoTotal);
        nuevoLote.setFechaIngreso(LocalDateTime.now(ZONA_ECUADOR));
        nuevoLote.setEstado("ACTIVO");

        Lote loteGuardado = loteRepository.save(nuevoLote);

        recalcularCostoPromedioProducto(producto, cantidad, costoUnitario, negocioId);

        transaccion.setCostoUnitario(costoUnitario);
        transaccion.setCostoTotal(costoTotal);
        transaccion.setLote(loteGuardado);
    }

    private void procesarEgreso(TransaccionInventario transaccion, TransaccionInventarioRequestDTO dto, Long negocioId, Producto producto, String metodoCosteo) {
        Bodega bodegaOrigen = buscarBodega(dto.getBodegaOrigenId(), negocioId, "origen");
        transaccion.setBodegaOrigen(bodegaOrigen);

        InventarioBodega inventario = inventarioRepository.findByBodegaIdAndNegocioId(bodegaOrigen.getId(), negocioId).stream()
                .filter(i -> i.getProducto().getId().equals(producto.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("El producto no existe en la bodega de origen."));

        if (inventario.getCantidadActual() < dto.getCantidad()) {
            throw new RuntimeException("Stock insuficiente en la bodega física. Disponible: " + inventario.getCantidadActual());
        }

        List<Lote> lotesDisponibles = "LIFO".equals(metodoCosteo)
                ? loteRepository.findLotesActivosLIFO(producto.getId(), bodegaOrigen.getId(), negocioId)
                : loteRepository.findLotesActivosFIFO(producto.getId(), bodegaOrigen.getId(), negocioId);

        int cantidadRequerida = dto.getCantidad();
        BigDecimal costoTotalEgreso = BigDecimal.ZERO;
        Lote ultimoLoteTocado = null;

        for (Lote lote : lotesDisponibles) {
            if (cantidadRequerida <= 0) break;

            int cantidadEnLote = lote.getCantidadDisponible().intValue();
            int cantidadATomar = Math.min(cantidadRequerida, cantidadEnLote);

            lote.setCantidadDisponible(lote.getCantidadDisponible().subtract(new BigDecimal(cantidadATomar)));
            if (lote.getCantidadDisponible().compareTo(BigDecimal.ZERO) == 0) {
                lote.setEstado("AGOTADO");
            }
            loteRepository.save(lote);

            BigDecimal costoAAplicar = "PROMEDIO".equals(metodoCosteo) ? producto.getCostoPromedioActual() : lote.getCostoUnitario();
            costoTotalEgreso = costoTotalEgreso.add(costoAAplicar.multiply(new BigDecimal(cantidadATomar)));

            cantidadRequerida -= cantidadATomar;
            ultimoLoteTocado = lote;
        }

        if (cantidadRequerida > 0) {
            System.out.println("ADVERTENCIA: Inconsistencia de lotes en producto ID " + producto.getId() + ". Faltan " + cantidadRequerida + " unidades en lote.");

            BigDecimal costoFaltante = producto.getCostoPromedioActual() != null ? producto.getCostoPromedioActual() : BigDecimal.ZERO;
            costoTotalEgreso = costoTotalEgreso.add(costoFaltante.multiply(new BigDecimal(cantidadRequerida)));
        }

        inventario.setCantidadActual(inventario.getCantidadActual() - dto.getCantidad());
        inventarioRepository.save(inventario);

        transaccion.setCostoTotal(costoTotalEgreso);
        transaccion.setCostoUnitario(costoTotalEgreso.divide(new BigDecimal(dto.getCantidad()), 4, RoundingMode.HALF_UP));
        transaccion.setLote(ultimoLoteTocado);

        verificarStockCritico(inventario, negocioId);
    }

    private void procesarTransferencia(TransaccionInventario transaccion, TransaccionInventarioRequestDTO dto, Long negocioId, Producto producto, String metodoCosteo) {
        Bodega bodegaOrigen = buscarBodega(dto.getBodegaOrigenId(), negocioId, "origen");
        Bodega bodegaDestino = buscarBodega(dto.getBodegaDestinoId(), negocioId, "destino");

        if (bodegaOrigen.getId().equals(bodegaDestino.getId())) {
            throw new RuntimeException("La bodega de origen y destino no pueden ser la misma.");
        }

        transaccion.setBodegaOrigen(bodegaOrigen);
        transaccion.setBodegaDestino(bodegaDestino);

        procesarEgreso(transaccion, dto, negocioId, producto, metodoCosteo);

        InventarioBodega inventarioDestino = obtenerOCrearInventario(producto, bodegaDestino, negocioId);
        inventarioDestino.setCantidadActual(inventarioDestino.getCantidadActual() + dto.getCantidad());
        inventarioRepository.save(inventarioDestino);

        long cantidadLotesActuales = loteRepository.countByNegocioId(negocioId);
        String codigoLoteGenerado = String.format("LOTE-%05d", cantidadLotesActuales + 1);

        BigDecimal cantidadBD = new BigDecimal(dto.getCantidad());
        Lote loteTransferido = new Lote();
        loteTransferido.setNegocio(bodegaDestino.getNegocio());
        loteTransferido.setProducto(producto);
        loteTransferido.setBodega(bodegaDestino);
        loteTransferido.setNumeroLote(codigoLoteGenerado);
        loteTransferido.setCantidadInicial(cantidadBD);
        loteTransferido.setCantidadDisponible(cantidadBD);
        loteTransferido.setCostoUnitario(transaccion.getCostoUnitario());
        loteTransferido.setCostoTotal(transaccion.getCostoTotal());
        loteTransferido.setFechaIngreso(LocalDateTime.now(ZONA_ECUADOR));
        loteTransferido.setEstado("ACTIVO");

        loteRepository.save(loteTransferido);
    }


    private void recalcularCostoPromedioProducto(
            Producto producto,
            BigDecimal cantidadIngresada,
            BigDecimal costoUnitarioIngreso,
            Long negocioId
    ) {
        if (cantidadIngresada == null || cantidadIngresada.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        if (costoUnitarioIngreso == null) {
            costoUnitarioIngreso = BigDecimal.ZERO;
        }

        Integer stockTotalActualInt = inventarioRepository
                .sumCantidadByProductoAndNegocio(producto.getId(), negocioId);
        int stockTotalActual = stockTotalActualInt != null ? stockTotalActualInt : 0;

        if (stockTotalActual <= 0) {
            producto.setCostoPromedioActual(costoUnitarioIngreso.setScale(4, RoundingMode.HALF_UP));
            productoRepository.save(producto);
            return;
        }

        BigDecimal stockTotalActualBd = new BigDecimal(stockTotalActual);
        BigDecimal stockAnterior = stockTotalActualBd.subtract(cantidadIngresada);
        if (stockAnterior.compareTo(BigDecimal.ZERO) < 0) {
            stockAnterior = BigDecimal.ZERO;
        }

        BigDecimal costoPromedioAnterior = producto.getCostoPromedioActual() != null
                ? producto.getCostoPromedioActual()
                : BigDecimal.ZERO;

        BigDecimal valorInventarioAnterior = stockAnterior.multiply(costoPromedioAnterior);
        BigDecimal valorNuevoIngreso = cantidadIngresada.multiply(costoUnitarioIngreso);

        BigDecimal nuevoCostoPromedio = valorInventarioAnterior.add(valorNuevoIngreso)
                .divide(stockTotalActualBd, 4, RoundingMode.HALF_UP);

        producto.setCostoPromedioActual(nuevoCostoPromedio);
        productoRepository.save(producto);
    }

    private Bodega buscarBodega(Long id, Long negocioId, String tipo) {
        if (id == null) throw new RuntimeException("Debe especificar la bodega de " + tipo);
        return bodegaRepository.findById(id)
                .filter(b -> b.getNegocio().getId().equals(negocioId))
                .orElseThrow(() -> new ResourceNotFoundException("Bodega de " + tipo + " no encontrada o no pertenece al negocio"));
    }

    private InventarioBodega obtenerOCrearInventario(Producto producto, Bodega bodega, Long negocioId) {
        return inventarioRepository.findByBodegaIdAndNegocioId(bodega.getId(), negocioId).stream()
                .filter(i -> i.getProducto().getId().equals(producto.getId()))
                .findFirst()
                .orElseGet(() -> {
                    InventarioBodega nuevoInventario = new InventarioBodega();
                    nuevoInventario.setProducto(producto);
                    nuevoInventario.setBodega(bodega);
                    nuevoInventario.setNegocio(negocioRepository.getReferenceById(negocioId));
                    nuevoInventario.setCantidadActual(0);
                    nuevoInventario.setStockMinimo(5);
                    return nuevoInventario;
                });
    }

    private void verificarStockCritico(InventarioBodega inventario, Long negocioId) {
        if (inventario.getCantidadActual() <= inventario.getStockMinimo()) {
            List<String> rolesNotificacion = List.of("PROPIETARIO", "ROLE_PROPIETARIO", "BODEGUERO", "ROLE_BODEGUERO");
            List<String> destinatarios = miembroNegocioRepository.findCorreosByNegocioAndRoles(negocioId, rolesNotificacion);

            if (destinatarios.isEmpty()) {
                destinatarios = List.of("castroelkin2020@gmail.com");
            }

            emailService.enviarAlertaStockMinimo(
                    destinatarios,
                    inventario.getProducto().getNombre(),
                    inventario.getBodega().getNombre(),
                    inventario.getCantidadActual(),
                    inventario.getStockMinimo()
            );
        }
    }


}