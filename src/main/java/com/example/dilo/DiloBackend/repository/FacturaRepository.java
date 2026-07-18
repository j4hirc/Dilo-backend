package com.example.dilo.DiloBackend.repository;

import com.example.dilo.DiloBackend.model.Factura;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FacturaRepository extends JpaRepository<Factura, Long> {

    List<Factura> findByNegocioIdOrderByFechaEmisionDesc(Long negocioId);

}
