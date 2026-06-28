package com.example.dilo.DiloBackend.repository;

import com.example.dilo.DiloBackend.model.DiloSpace;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface DiloSpaceRepository extends JpaRepository<DiloSpace, Long> {

    Optional<DiloSpace> findByNombreComercial(String nombreComercial);
}
