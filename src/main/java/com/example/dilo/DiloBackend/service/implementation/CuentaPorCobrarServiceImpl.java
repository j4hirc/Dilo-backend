package com.example.dilo.DiloBackend.service.implementation;

import com.example.dilo.DiloBackend.dto.response.CuentaPorCobrarResponseDTO;
import com.example.dilo.DiloBackend.exception.ResourceNotFoundException;
import com.example.dilo.DiloBackend.model.CuentasPorCobrar;
import com.example.dilo.DiloBackend.model.Cuota;
import com.example.dilo.DiloBackend.model.Factura;
import com.example.dilo.DiloBackend.model.HistorialAbono;
import com.example.dilo.DiloBackend.repository.CuentaPorCobrarRepository;
import com.example.dilo.DiloBackend.repository.CuotaRepository;
import com.example.dilo.DiloBackend.repository.HistorialAbonoRepository;
import com.example.dilo.DiloBackend.service.CuentaPorCobrarService;
import com.example.dilo.DiloBackend.service.mapper.CuentaPorCobrarMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CuentaPorCobrarServiceImpl implements CuentaPorCobrarService {

    private final CuentaPorCobrarRepository cuentaRepository;

    private final CuentaPorCobrarMapper cuentaMapper;
    private final CuotaRepository cuotaRepository;
    private final HistorialAbonoRepository abonoRepository; // <--- NUEVO

    @Override
    @Transactional
    public void generarCuentaPorCobrar(Factura factura, int numeroCuotas) {
        if (numeroCuotas <= 0) return;

        BigDecimal montoTotal = factura.getTotalFactura();

        CuentasPorCobrar cuenta = new CuentasPorCobrar();
        cuenta.setFactura(factura);
        cuenta.setNegocio(factura.getNegocio());
        cuenta.setMontoTotal(montoTotal);
        cuenta.setSaldoPendiente(montoTotal);
        cuenta.setEstado("PENDIENTE");
        cuenta.setFechaVencimiento(factura.getFechaEmision().plusMonths(numeroCuotas)); // Vence con la última cuota

        List<Cuota> cuotas = new ArrayList<>();

        BigDecimal cuotaBase = montoTotal.divide(new BigDecimal(numeroCuotas), 2, RoundingMode.HALF_UP);
        BigDecimal sumaCuotas = BigDecimal.ZERO;

        for (int i = 1; i <= numeroCuotas; i++) {
            Cuota cuota = new Cuota();
            cuota.setCuentaPorCobrar(cuenta);
            cuota.setNumeroCuota(i);
            cuota.setEstado("PENDIENTE");

            cuota.setFechaVencimiento(factura.getFechaEmision().plusMonths(i));

            if (i == numeroCuotas) {
                BigDecimal resto = montoTotal.subtract(sumaCuotas);
                cuota.setMontoCuota(resto);
                cuota.setSaldoPendienteCuota(resto);
            } else {
                cuota.setMontoCuota(cuotaBase);
                cuota.setSaldoPendienteCuota(cuotaBase);
                sumaCuotas = sumaCuotas.add(cuotaBase);
            }
            cuotas.add(cuota);
        }

        cuenta.setCuotas(cuotas);
        cuentaRepository.save(cuenta);
    }

    @Override
    @Transactional
    public List<CuentaPorCobrarResponseDTO> listarPorNegocio(Long negocioId) {
        List<CuentasPorCobrar> cuentas = cuentaRepository.findByNegocioIdOrderByFechaVencimientoAsc(negocioId);

        java.time.LocalDateTime ahora = java.time.LocalDateTime.now();
        boolean huboCambios = false;

        for (CuentasPorCobrar cuenta : cuentas) {
            // Actualizar estado general de la cuenta si está vencida
            if ("PENDIENTE".equals(cuenta.getEstado()) &&
                    cuenta.getFechaVencimiento() != null &&
                    cuenta.getFechaVencimiento().isBefore(ahora)) {

                cuenta.setEstado("VENCIDA");
                huboCambios = true;
            }

            // Actualizar estado individual de cada cuota si está vencida
            if (cuenta.getCuotas() != null) {
                for (Cuota cuota : cuenta.getCuotas()) {
                    if ("PENDIENTE".equals(cuota.getEstado()) &&
                            cuota.getFechaVencimiento() != null &&
                            cuota.getFechaVencimiento().isBefore(ahora)) {

                        cuota.setEstado("VENCIDA");
                        huboCambios = true;
                    }
                }
            }
        }

        if (huboCambios) {
            cuentaRepository.saveAll(cuentas);
        }

        return cuentas.stream()
                .map(cuentaMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional // ¡Ojo! Debe llevar @Transactional ahora
    public CuentaPorCobrarResponseDTO obtenerDetalle(Long id) {
        CuentasPorCobrar cuenta = cuentaRepository.findByIdWithCuotas(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta por cobrar no encontrada"));

        java.time.LocalDateTime ahora = java.time.LocalDateTime.now();
        boolean huboCambios = false;

        if ("PENDIENTE".equals(cuenta.getEstado()) &&
                cuenta.getFechaVencimiento() != null &&
                cuenta.getFechaVencimiento().isBefore(ahora)) {
            cuenta.setEstado("VENCIDA");
            huboCambios = true;
        }

        if (cuenta.getCuotas() != null) {
            for (Cuota cuota : cuenta.getCuotas()) {
                if ("PENDIENTE".equals(cuota.getEstado()) &&
                        cuota.getFechaVencimiento() != null &&
                        cuota.getFechaVencimiento().isBefore(ahora)) {
                    cuota.setEstado("VENCIDA");
                    huboCambios = true;
                }
            }
        }

        if (huboCambios) {
            cuentaRepository.save(cuenta);
        }

        return cuentaMapper.toDto(cuenta);
    }


    @Override
    @Transactional
    public void registrarPagoCuota(Long cuentaId, BigDecimal montoPago, String metodoPago, String referencia, String usuarioLogueado) {

        CuentasPorCobrar cuentaTotal = cuentaRepository.findById(cuentaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta por cobrar no encontrada"));

        if ("PAGADA".equals(cuentaTotal.getEstado())) {
            throw new RuntimeException("La deuda total ya está pagada.");
        }
        if (montoPago.compareTo(cuentaTotal.getSaldoPendiente()) > 0) {
            throw new RuntimeException("El monto a abonar es mayor a la deuda total.");
        }

        HistorialAbono recibo = new HistorialAbono();
        recibo.setCuentaPorCobrar(cuentaTotal);
        recibo.setMontoAbonado(montoPago);
        recibo.setMetodoPago(metodoPago != null ? metodoPago : "EFECTIVO");
        recibo.setReferencia(referencia);
        recibo.setUsuarioRecibio(usuarioLogueado);
        abonoRepository.save(recibo);

        // 2. REPARTIR EL DINERO EN LAS CUOTAS
        List<Cuota> cuotasPendientes = cuentaTotal.getCuotas().stream()
                .filter(c -> !"PAGADA".equals(c.getEstado()))
                .sorted(java.util.Comparator.comparing(Cuota::getNumeroCuota))
                .toList();

        BigDecimal abonoRestante = montoPago;

        for (Cuota cuota : cuotasPendientes) {
            if (abonoRestante.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal saldoCuota = cuota.getSaldoPendienteCuota();
            BigDecimal montoADescontar = (abonoRestante.compareTo(saldoCuota) >= 0) ? saldoCuota : abonoRestante;

            cuota.setSaldoPendienteCuota(saldoCuota.subtract(montoADescontar));
            abonoRestante = abonoRestante.subtract(montoADescontar);

            if (cuota.getSaldoPendienteCuota().compareTo(BigDecimal.ZERO) == 0) {
                cuota.setEstado("PAGADA");
            } else if (cuota.getFechaVencimiento() != null && cuota.getFechaVencimiento().isBefore(java.time.LocalDateTime.now())) {
                cuota.setEstado("VENCIDA");
            } else {
                cuota.setEstado("PENDIENTE");
            }
        }

        // 3. ACTUALIZAR LA CUENTA GENERAL
        cuentaTotal.setSaldoPendiente(cuentaTotal.getSaldoPendiente().subtract(montoPago));

        if (cuentaTotal.getSaldoPendiente().compareTo(BigDecimal.ZERO) == 0) {
            cuentaTotal.setEstado("PAGADA");
        } else if (cuentaTotal.getFechaVencimiento() != null && cuentaTotal.getFechaVencimiento().isBefore(java.time.LocalDateTime.now())) {
            cuentaTotal.setEstado("VENCIDA");
        } else {
            cuentaTotal.setEstado("PENDIENTE");
        }

        cuentaRepository.save(cuentaTotal);
    }
}