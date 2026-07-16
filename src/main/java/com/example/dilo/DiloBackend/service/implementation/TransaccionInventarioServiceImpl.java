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

        String tipo = requestDTO.getTipo().toUpperCase();

        switch (tipo) {
            case "INGRESO":
                procesarIngreso(transaccion, requestDTO, negocioId, producto);
                break;
            case "EGRESO":
                procesarEgreso(transaccion, requestDTO, negocioId, producto);
                break;
            case "TRANSFERENCIA":
                procesarTransferencia(transaccion, requestDTO, negocioId, producto);
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
    }

    private void procesarEgreso(TransaccionInventario transaccion, TransaccionInventarioRequestDTO dto, Long negocioId, Producto producto) {
        Bodega bodegaOrigen = buscarBodega(dto.getBodegaOrigenId(), negocioId, "origen");
        transaccion.setBodegaOrigen(bodegaOrigen);

        InventarioBodega inventario = inventarioRepository.findByBodegaIdAndNegocioId(bodegaOrigen.getId(), negocioId).stream()
                .filter(i -> i.getProducto().getId().equals(producto.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("El producto no existe en la bodega de origen."));

        if (inventario.getCantidadActual() < dto.getCantidad()) {
            throw new RuntimeException("Stock insuficiente en la bodega de origen. Disponible: " + inventario.getCantidadActual());
        }

        inventario.setCantidadActual(inventario.getCantidadActual() - dto.getCantidad());
        inventarioRepository.save(inventario);

        // NUEVO: Verificar si al hacer el egreso el stock quedó en nivel crítico
        verificarStockCritico(inventario, negocioId);
    }

    private void procesarTransferencia(TransaccionInventario transaccion, TransaccionInventarioRequestDTO dto, Long negocioId, Producto producto) {
        Bodega bodegaOrigen = buscarBodega(dto.getBodegaOrigenId(), negocioId, "origen");
        Bodega bodegaDestino = buscarBodega(dto.getBodegaDestinoId(), negocioId, "destino");

        if (bodegaOrigen.getId().equals(bodegaDestino.getId())) {
            throw new RuntimeException("La bodega de origen y destino no pueden ser la misma.");
        }

        transaccion.setBodegaOrigen(bodegaOrigen);
        transaccion.setBodegaDestino(bodegaDestino);

        InventarioBodega inventarioOrigen = inventarioRepository.findByBodegaIdAndNegocioId(bodegaOrigen.getId(), negocioId).stream()
                .filter(i -> i.getProducto().getId().equals(producto.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("El producto no existe en la bodega de origen."));

        if (inventarioOrigen.getCantidadActual() < dto.getCantidad()) {
            throw new RuntimeException("Stock insuficiente para transferir. Disponible: " + inventarioOrigen.getCantidadActual());
        }
        inventarioOrigen.setCantidadActual(inventarioOrigen.getCantidadActual() - dto.getCantidad());
        inventarioRepository.save(inventarioOrigen);

        // NUEVO: Verificar si la bodega a la que le quitamos stock quedó en nivel crítico
        verificarStockCritico(inventarioOrigen, negocioId);

        InventarioBodega inventarioDestino = obtenerOCrearInventario(producto, bodegaDestino, negocioId);
        inventarioDestino.setCantidadActual(inventarioDestino.getCantidadActual() + dto.getCantidad());
        inventarioRepository.save(inventarioDestino);
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