package com.example.dilo.DiloBackend.repository;

import com.example.dilo.DiloBackend.model.Negocio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NegocioRepository extends JpaRepository<Negocio, Long> {

    @Query("SELECT n FROM Negocio n WHERE " +
            "LOWER(n.ruc) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(n.razonSocial) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(n.nombreComercial) LIKE LOWER(CONCAT('%', :term, '%'))")
    List<Negocio> buscarPorTermino(@Param("term") String term);

}
