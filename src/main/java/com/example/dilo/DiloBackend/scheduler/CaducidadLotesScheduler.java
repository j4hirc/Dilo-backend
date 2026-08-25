package com.example.dilo.DiloBackend.scheduler;

import com.example.dilo.DiloBackend.model.InventarioBodega;
import com.example.dilo.DiloBackend.model.Lote;
import com.example.dilo.DiloBackend.model.TransaccionInventario;
import com.example.dilo.DiloBackend.model.Usuario;
import com.example.dilo.DiloBackend.repository.InventarioBodegaRepository;
import com.example.dilo.DiloBackend.repository.LoteRepository;
import com.example.dilo.DiloBackend.repository.TransaccionInventarioRepository;
import com.example.dilo.DiloBackend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaducidadLotesScheduler {

    private final LoteRepository loteRepository;
    private final TransaccionInventarioRepository transaccionRepository;
    private final InventarioBodegaRepository inventarioRepository;
    private final UsuarioRepository usuarioRepository;

    private static final ZoneId ZONA_ECUADOR = ZoneId.of("America/Guayaquil");

    @Scheduled(cron = "0 0 */2 * * ?", zone = "America/Guayaquil")
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void procesarLotesCaducados() {
        log.info("Iniciando revisión automática de lotes caducados...");

        LocalDate hoy = LocalDate.now(ZONA_ECUADOR);

        List<Lote> lotesCaducados = loteRepository.findLotesActivosPorCaducar(hoy);

        if (lotesCaducados.isEmpty()) {
            log.info("No se encontraron lotes caducados.");
            return;
        }

        for (Lote lote : lotesCaducados) {
            int cantidadPerdida = lote.getCantidadDisponible().intValue();

            lote.setEstado("CADUCADO");
            lote.setCantidadDisponible(BigDecimal.ZERO);
            loteRepository.save(lote);

            if (cantidadPerdida <= 0) {
                continue;
            }

            Long negocioId = lote.getNegocio().getId();
            List<Usuario> usuariosDelNegocio = usuarioRepository.findUsuariosByNegocioId(negocioId);

            Usuario usuarioResponsable = usuariosDelNegocio.stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("No se encontró ningún usuario para el negocio ID: " + negocioId));

            TransaccionInventario transaccion = new TransaccionInventario();
            transaccion.setTipo("EGRESO");
            transaccion.setProducto(lote.getProducto());
            transaccion.setUsuarioResponsable(usuarioResponsable);
            transaccion.setBodegaOrigen(lote.getBodega());
            transaccion.setBodegaDestino(null);
            transaccion.setCantidad(cantidadPerdida);

            transaccion.setFechaTransaccion(java.time.LocalDateTime.now(ZONA_ECUADOR));

            transaccion.setMotivo("Baja automática por caducidad");
            transaccion.setNegocio(lote.getNegocio());

            BigDecimal costoUnitario = lote.getCostoUnitario() != null ? lote.getCostoUnitario() : BigDecimal.ZERO;
            transaccion.setCostoUnitario(costoUnitario);
            transaccion.setCostoTotal(costoUnitario.multiply(new BigDecimal(cantidadPerdida)));

            String metodoCosteo = lote.getNegocio().getMetodoCosteo() != null ? lote.getNegocio().getMetodoCosteo() : "PROMEDIO";
            transaccion.setMetodoAplicado(metodoCosteo);
            transaccion.setDocumentoReferencia("CADUCIDAD-" + lote.getNumeroLote());
            transaccion.setLote(lote);

            transaccionRepository.save(transaccion);

            InventarioBodega inventario = inventarioRepository.findByBodegaIdAndNegocioId(
                            lote.getBodega().getId(), lote.getNegocio().getId())
                    .stream()
                    .filter(i -> i.getProducto().getId().equals(lote.getProducto().getId()))
                    .findFirst()
                    .orElse(null);

            if (inventario != null) {
                int nuevoStock = inventario.getCantidadActual() - cantidadPerdida;
                inventario.setCantidadActual(Math.max(nuevoStock, 0));
                inventarioRepository.save(inventario);
            }
        }

        log.info("Proceso finalizado. Se dieron de baja {} lotes caducados.", lotesCaducados.size());
    }
}