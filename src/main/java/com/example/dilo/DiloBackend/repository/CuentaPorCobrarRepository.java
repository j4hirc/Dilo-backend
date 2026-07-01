package com.example.dilo.DiloBackend.repository;

import com.example.dilo.DiloBackend.model.CuentasPorCobrar;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CuentaPorCobrarRepository extends JpaRepository<CuentasPorCobrar, Long> {
}
