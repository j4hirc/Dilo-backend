package com.example.dilo.DiloBackend.repository;

import com.example.dilo.DiloBackend.model.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {
    List<Compra> findByNegocioIdOrderByFechaCompraDesc(Long negocioId);
    List<Compra> findByNegocioId(Long negocioId);
}