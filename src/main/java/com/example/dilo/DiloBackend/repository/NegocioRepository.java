package com.example.dilo.DiloBackend.repository;

import com.example.dilo.DiloBackend.model.Negocio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NegocioRepository extends JpaRepository<Negocio, Long> {

    Optional<Negocio> findByRuc(String ruc);

}
