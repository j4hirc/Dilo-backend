package com.example.dilo.DiloBackend.repository;

import com.example.dilo.DiloBackend.model.MiembroNegocio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MiembroNegocioRepository extends JpaRepository<MiembroNegocio, Long> {

    @Query("SELECT m FROM MiembroNegocio m WHERE m.negocio.id = :negocioId AND m.estadoLaboral != 'Unemployed'")
    List<MiembroNegocio> findPersonalActivoByNegocio(@Param("negocioId") Long negocioId);

    List<MiembroNegocio> findByUsuarioId(Long usuarioId);
}