package com.example.dilo.DiloBackend.repository;

import com.example.dilo.DiloBackend.model.CuentasPorCobrar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CuentaPorCobrarRepository extends JpaRepository<CuentasPorCobrar, Long> {

    List<CuentasPorCobrar> findByNegocioIdOrderByFechaVencimientoAsc(Long negocioId);

    @Query("SELECT c FROM CuentasPorCobrar c LEFT JOIN FETCH c.cuotas WHERE c.id = :id")
    Optional<CuentasPorCobrar> findByIdWithCuotas(@Param("id") Long id);

}
