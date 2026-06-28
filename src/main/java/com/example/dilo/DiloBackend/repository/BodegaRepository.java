package com.example.dilo.DiloBackend.repository;

import com.example.dilo.DiloBackend.model.Bodega;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BodegaRepository  extends JpaRepository<Bodega, Long> {

    @EntityGraph(attributePaths = {"espacio"})
    Optional<Bodega> findByNombre(String nombre);

}
