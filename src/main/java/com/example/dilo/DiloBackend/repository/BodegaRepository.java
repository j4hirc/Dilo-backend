package com.example.dilo.DiloBackend.repository;

import com.example.dilo.DiloBackend.model.Bodega;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BodegaRepository  extends JpaRepository<Bodega, Long> {

    @EntityGraph(attributePaths = {"negocio"})
    List<Bodega> findByNegocioId(Long negocioId);

    @EntityGraph(attributePaths = {"negocio"})
    Optional<Bodega> findByIdAndNegocioId(Long id, Long negocioId);

    boolean existsByNombreIgnoreCaseAndNegocioId(String nombre, Long negocioId);

    @Query("SELECT b FROM Bodega b WHERE b.negocio.id = :negocioId AND " +
            "(LOWER(b.nombre) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(b.direccion) LIKE LOWER(CONCAT('%', :term, '%')))")
    List<Bodega> buscarPorTermino(@Param("negocioId") Long negocioId, @Param("term") String term);

}
