package com.example.dilo.DiloBackend.repository;

import com.example.dilo.DiloBackend.model.Cliente;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    @EntityGraph(attributePaths = {"negocio"})
    List<Cliente> findByNegocioId(Long negocioId);

    Optional<Cliente> findByIdAndNegocioId(Long id, Long negocioId);

    boolean existsByDniAndNegocioId(String dni, Long negocioId);

    @Query("SELECT c FROM Cliente c WHERE c.negocio.id = :negocioId AND " +
            "(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(LOWER(CONCAT(c.primerNombre, ' ', c.apellidoPaterno)), 'á', 'a'), 'é', 'e'), 'í', 'i'), 'ó', 'o'), 'ú', 'u'), 'ñ', 'n') LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(LOWER(CONCAT(c.apellidoPaterno, ' ', c.primerNombre)), 'á', 'a'), 'é', 'e'), 'í', 'i'), 'ó', 'o'), 'ú', 'u'), 'ñ', 'n') LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(c.dni) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :term, '%')))")
    List<Cliente> buscarPorTermino(@Param("negocioId") Long negocioId, @Param("term") String term);

    @Query("SELECT c FROM Cliente c WHERE c.negocio.id = :negocioId AND " +
            "(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(LOWER(CONCAT(c.primerNombre, ' ', c.apellidoPaterno)), 'á', 'a'), 'é', 'e'), 'í', 'i'), 'ó', 'o'), 'ú', 'u'), 'ñ', 'n') LIKE CONCAT('%', :termino, '%') OR " +
            "REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(LOWER(CONCAT(c.apellidoPaterno, ' ', c.primerNombre)), 'á', 'a'), 'é', 'e'), 'í', 'i'), 'ó', 'o'), 'ú', 'u'), 'ñ', 'n') LIKE CONCAT('%', :termino, '%') OR " +
            "c.dni LIKE CONCAT('%', :termino, '%'))")
    List<Cliente> buscarPorVoz(@Param("negocioId") Long negocioId, @Param("termino") String termino);

}