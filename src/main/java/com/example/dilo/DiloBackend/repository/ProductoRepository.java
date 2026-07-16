package com.example.dilo.DiloBackend.repository;

import com.example.dilo.DiloBackend.model.Producto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    @EntityGraph(attributePaths = {"categoria", "negocio"})
    List<Producto> findByNegocioId(Long negocioId);

    @EntityGraph(attributePaths = {"categoria", "negocio"})
    Optional<Producto> findByIdAndNegocioId(Long id, Long negocioId);

    boolean existsByCodigoPrincipalAndNegocioId(String codigoPrincipal, Long negocioId);

    @Query("SELECT p FROM Producto p WHERE p.negocio.id = :negocioId AND " +
            "(LOWER(p.nombre) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(p.codigoPrincipal) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(p.marca) LIKE LOWER(CONCAT('%', :term, '%')))")
    List<Producto> buscarPorTermino(@Param("negocioId") Long negocioId, @Param("term") String term);
}
