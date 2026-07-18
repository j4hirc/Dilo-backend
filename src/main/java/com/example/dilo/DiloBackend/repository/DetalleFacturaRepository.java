package com.example.dilo.DiloBackend.repository;

import com.example.dilo.DiloBackend.model.DetalleFactura;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetalleFacturaRepository extends JpaRepository<DetalleFactura, Long> {

    @EntityGraph(attributePaths = {"producto", "bodega", "factura"})
    List<DetalleFactura> findByFacturaId(Long facturaId);


}
