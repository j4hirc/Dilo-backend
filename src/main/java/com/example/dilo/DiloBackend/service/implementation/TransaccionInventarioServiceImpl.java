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
import java.util.List;

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

    @Override
    public List<TransaccionInventarioResponseDTO> obtenerKardexGeneral(Long negocioId) {
        return transaccionRepository.obtenerKardexPorNegocio(negocioId).stream()
                .map(transaccionMapper::toDto)
                .toList();
    }

    @Override
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
        transaccion.setFechaTransaccion(LocalDateTime.now());

        // Asignar el método configurado en el negocio
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
                throw new RuntimeException("Tipo de transacción no válido. Use INGRESO, EGRESO o TRANSFERENCIA.");
        }

        TransaccionInventario guardada = transaccionRepository.save(transaccion);
        return transaccionMapper.toDto(guardada);
    }

    private void procesarIngreso(TransaccionInventario transaccion, TransaccionInventarioRequestDTO dto, Long negocioId, Producto producto) {
        Bodega bodegaDestino = buscarBodega(dto.getBodegaDestinoId(), negocioId, "destino");
        transaccion.setBodegaDestino(bodegaDestino);

        InventarioBodega inventario = obtenerOCrearInventario(producto, bodegaDestino, negocioId);
        inventario.setCantidadActual(inventario.getCantidadActual() + dto.getCantidad());
        inventarioRepository.save(inventario);

        // LÓGICA DE LOTES: Crear un lote para este ingreso manual
        BigDecimal costoUnitario = dto.getCostoUnitario() != null ? dto.getCostoUnitario() : producto.getCostoPromedioActual();
        BigDecimal cantidad = new BigDecimal(dto.getCantidad());
        BigDecimal costoTotal = costoUnitario.multiply(cantidad);

        Lote nuevoLote = new Lote();
        nuevoLote.setNegocio(bodegaDestino.getNegocio());
        nuevoLote.setProducto(producto);
        nuevoLote.setBodega(bodegaDestino);
        nuevoLote.setCantidadInicial(cantidad);
        nuevoLote.setCantidadDisponible(cantidad);
        nuevoLote.setCostoUnitario(costoUnitario);
        nuevoLote.setCostoTotal(costoTotal);
        nuevoLote.setFechaIngreso(LocalDateTime.now());
        nuevoLote.setEstado("ACTIVO");

        Lote loteGuardado = loteRepository.save(nuevoLote);

        // Recalcular el Promedio Ponderado del producto si es un ingreso valorado
        recalcularCostoPromedioProducto(producto, cantidad, costoUnitario, inventario.getCantidadActual());

        // Actualizar transacción
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

        // LÓGICA DE LOTES: Descontar según el método (FIFO, LIFO, PROMEDIO)
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

            // Descontar del lote
            lote.setCantidadDisponible(lote.getCantidadDisponible().subtract(new BigDecimal(cantidadATomar)));
            if (lote.getCantidadDisponible().compareTo(BigDecimal.ZERO) == 0) {
                lote.setEstado("AGOTADO");
            }
            loteRepository.save(lote);

            // Calcular el costo de esta fracción extraída
            BigDecimal costoAAplicar = "PROMEDIO".equals(metodoCosteo) ? producto.getCostoPromedioActual() : lote.getCostoUnitario();
            costoTotalEgreso = costoTotalEgreso.add(costoAAplicar.multiply(new BigDecimal(cantidadATomar)));

            cantidadRequerida -= cantidadATomar;
            ultimoLoteTocado = lote;
        }

        if (cantidadRequerida > 0) {
            throw new RuntimeException("Inconsistencia: El stock físico no coincide con los lotes disponibles.");
        }

        // Actualizar stock físico general
        inventario.setCantidadActual(inventario.getCantidadActual() - dto.getCantidad());
        inventarioRepository.save(inventario);

        // Guardar costos en la transacción
        transaccion.setCostoTotal(costoTotalEgreso);
        transaccion.setCostoUnitario(costoTotalEgreso.divide(new BigDecimal(dto.getCantidad()), 4, RoundingMode.HALF_UP));
        transaccion.setLote(ultimoLoteTocado);

        verificarStockCritico(inventario, negocioId);
    }

    private void procesarTransferencia(TransaccionInventario transaccion, TransaccionInventarioRequestDTO dto, Long negocioId, Producto producto, String metodoCosteo) {
        // Una transferencia es esencialmente un Egreso de la Bodega A, y un Ingreso exacto en la Bodega B.
        // Reutilizamos la lógica matemática de Egreso para sacar el costo exacto de los lotes de origen.

        Bodega bodegaOrigen = buscarBodega(dto.getBodegaOrigenId(), negocioId, "origen");
        Bodega bodegaDestino = buscarBodega(dto.getBodegaDestinoId(), negocioId, "destino");

        if (bodegaOrigen.getId().equals(bodegaDestino.getId())) {
            throw new RuntimeException("La bodega de origen y destino no pueden ser la misma.");
        }

        transaccion.setBodegaOrigen(bodegaOrigen);
        transaccion.setBodegaDestino(bodegaDestino);

        // 1. Efectuar el Egreso lógico de origen (esto deduce los lotes y calcula el costo real transferido)
        procesarEgreso(transaccion, dto, negocioId, producto, metodoCosteo);

        // En este punto, 'transaccion' ya tiene el costoUnitario y costoTotal extraído de la bodega Origen.

        // 2. Efectuar el Ingreso en el destino
        InventarioBodega inventarioDestino = obtenerOCrearInventario(producto, bodegaDestino, negocioId);
        inventarioDestino.setCantidadActual(inventarioDestino.getCantidadActual() + dto.getCantidad());
        inventarioRepository.save(inventarioDestino);

        // 3. Crear el nuevo Lote en la bodega destino conservando el costo de transferencia
        BigDecimal cantidadBD = new BigDecimal(dto.getCantidad());
        Lote loteTransferido = new Lote();
        loteTransferido.setNegocio(bodegaDestino.getNegocio());
        loteTransferido.setProducto(producto);
        loteTransferido.setBodega(bodegaDestino);
        loteTransferido.setCantidadInicial(cantidadBD);
        loteTransferido.setCantidadDisponible(cantidadBD);
        loteTransferido.setCostoUnitario(transaccion.getCostoUnitario()); // Costo heredado
        loteTransferido.setCostoTotal(transaccion.getCostoTotal());
        loteTransferido.setFechaIngreso(LocalDateTime.now());
        loteTransferido.setEstado("ACTIVO");

        loteRepository.save(loteTransferido);
    }

    private void recalcularCostoPromedioProducto(Producto producto, BigDecimal cantidadIngresada, BigDecimal costoUnitarioIngreso, int nuevoStockFisicoTotal) {
        // Fórmula Promedio Ponderado:
        // Nuevo Costo = [ (Stock Anterior * Costo Anterior) + (Cant Ingresada * Costo Ingreso) ] / Nuevo Stock Físico

        if (nuevoStockFisicoTotal == 0) return; // Prevención división por cero

        BigDecimal stockAnterior = new BigDecimal(nuevoStockFisicoTotal).subtract(cantidadIngresada);
        BigDecimal costoPromedioAnterior = producto.getCostoPromedioActual() != null ? producto.getCostoPromedioActual() : BigDecimal.ZERO;

        BigDecimal valorInventarioAnterior = stockAnterior.multiply(costoPromedioAnterior);
        BigDecimal valorNuevoIngreso = cantidadIngresada.multiply(costoUnitarioIngreso);

        BigDecimal nuevoCostoPromedio = valorInventarioAnterior.add(valorNuevoIngreso)
                .divide(new BigDecimal(nuevoStockFisicoTotal), 4, RoundingMode.HALF_UP);

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
                    nuevoInventario.setStockMinimo(5); // Valor por defecto
                    return nuevoInventario;
                });
    }

    private void verificarStockCritico(InventarioBodega inventario, Long negocioId) {
        if (inventario.getCantidadActual() <= inventario.getStockMinimo()) {

            List<String> rolesNotificacion = List.of("PROPIETARIO", "ROLE_PROPIETARIO", "BODEGUERO", "ROLE_BODEGUERO");
            List<String> destinatarios = miembroNegocioRepository.findCorreosByNegocioAndRoles(negocioId, rolesNotificacion);

            System.out.println("🔍 Correos reales listos para recibir alerta: " + destinatarios);

            if (destinatarios.isEmpty()) {
                destinatarios = List.of("castroelkin2020@gmail.com");
                System.out.println("⚠️ No se encontraron propietarios, enviando a correo de soporte: " + destinatarios);
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