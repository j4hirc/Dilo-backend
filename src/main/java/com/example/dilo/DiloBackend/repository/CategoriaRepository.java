package com.example.dilo.DiloBackend.repository;

import com.example.dilo.DiloBackend.model.Categoria;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    @EntityGraph(attributePaths = {"negocio"})
    List<Categoria> findByNegocioId(Long negocioId);

    @EntityGraph(attributePaths = {"negocio"})
    Optional<Categoria> findByIdAndNegocioId(Long id, Long negocioId);

    @EntityGraph(attributePaths = {"negocio"})
    boolean existsByNombreIgnoreCaseAndNegocioId(String nombre, Long negocioId);
}
