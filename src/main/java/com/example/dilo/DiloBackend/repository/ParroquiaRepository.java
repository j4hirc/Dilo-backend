package com.example.dilo.DiloBackend.repository;

import com.example.dilo.DiloBackend.model.Parroquia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParroquiaRepository extends JpaRepository<Parroquia, Long> {
    List<Parroquia> findAllByOrderByNombreAsc();

    boolean existsByNombreIgnoreCase(String nombre);
}
