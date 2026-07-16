package com.example.dilo.DiloBackend.repository;

import com.example.dilo.DiloBackend.model.Categoria;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    @EntityGraph(attributePaths = {"negocio"})
    List<Categoria> findByNegocioId(Long negocioId);

    @EntityGraph(attributePaths = {"negocio"})
    Optional<Categoria> findByIdAndNegocioId(Long id, Long negocioId);

    boolean existsByNombreIgnoreCaseAndNegocioId(String nombre, Long negocioId);

    @Query("SELECT c FROM Categoria c WHERE c.negocio.id = :negocioId AND " +
            "(LOWER(c.nombre) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(c.descripcion) LIKE LOWER(CONCAT('%', :term, '%')))")
    List<Categoria> buscarPorTermino(@Param("negocioId") Long negocioId, @Param("term") String term);
}
