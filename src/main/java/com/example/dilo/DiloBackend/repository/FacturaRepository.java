package com.example.dilo.DiloBackend.repository;

import com.example.dilo.DiloBackend.model.Factura;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacturaRepository extends JpaRepository<Factura, Long> {
}
