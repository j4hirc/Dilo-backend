package com.example.dilo.DiloBackend.service.implementation;

import com.example.dilo.DiloBackend.dto.response.CuentaPorCobrarResponseDTO;
import com.example.dilo.DiloBackend.exception.ResourceNotFoundException;
import com.example.dilo.DiloBackend.model.CuentasPorCobrar;
import com.example.dilo.DiloBackend.model.Cuota;
import com.example.dilo.DiloBackend.model.Factura;
import com.example.dilo.DiloBackend.repository.CuentaPorCobrarRepository;
import com.example.dilo.DiloBackend.repository.CuotaRepository;
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
    @Transactional(readOnly = true)
    public List<CuentaPorCobrarResponseDTO> listarPorNegocio(Long negocioId) {
        return cuentaRepository.findByNegocioIdOrderByFechaVencimientoAsc(negocioId)
                .stream()
                .map(cuentaMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CuentaPorCobrarResponseDTO obtenerDetalle(Long id) {
        CuentasPorCobrar cuenta = cuentaRepository.findByIdWithCuotas(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta por cobrar no encontrada"));

        return cuentaMapper.toDto(cuenta);
    }


    @Override
    @Transactional
    public void registrarPagoCuota(Long cuentaId, BigDecimal montoPago) {

        CuentasPorCobrar cuentaTotal = cuentaRepository.findById(cuentaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta por cobrar no encontrada"));

        if (cuentaTotal.getEstado().equals("PAGADA")) {
            throw new RuntimeException("La deuda total de esta factura ya está completamente pagada.");
        }

        if (montoPago.compareTo(cuentaTotal.getSaldoPendiente()) > 0) {
            throw new RuntimeException("El monto a abonar ($" + montoPago + ") es mayor a la deuda total restante ($" + cuentaTotal.getSaldoPendiente() + ").");
        }

        List<Cuota> cuotasPendientes = cuentaTotal.getCuotas().stream()
                .filter(c -> !c.getEstado().equals("PAGADA"))
                .sorted(java.util.Comparator.comparing(Cuota::getNumeroCuota))
                .toList();

        BigDecimal abonoRestante = montoPago;

        for (Cuota cuota : cuotasPendientes) {
            if (abonoRestante.compareTo(BigDecimal.ZERO) <= 0) break; // Ya se repartió todo el dinero

            BigDecimal saldoCuota = cuota.getSaldoPendienteCuota();
            BigDecimal montoADescontar;

            if (abonoRestante.compareTo(saldoCuota) >= 0) {
                montoADescontar = saldoCuota;
            } else {
                montoADescontar = abonoRestante;
            }

            cuota.setSaldoPendienteCuota(saldoCuota.subtract(montoADescontar));
            abonoRestante = abonoRestante.subtract(montoADescontar);

            if (cuota.getSaldoPendienteCuota().compareTo(BigDecimal.ZERO) == 0) {
                cuota.setEstado("PAGADA");
            }
        }

        cuentaTotal.setSaldoPendiente(cuentaTotal.getSaldoPendiente().subtract(montoPago));

        if (cuentaTotal.getSaldoPendiente().compareTo(BigDecimal.ZERO) == 0) {
            cuentaTotal.setEstado("PAGADA");
        }

        cuentaRepository.save(cuentaTotal);
    }
}