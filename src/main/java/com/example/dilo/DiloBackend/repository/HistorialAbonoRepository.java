package com.example.dilo.DiloBackend.repository;

import com.example.dilo.DiloBackend.model.HistorialAbono;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistorialAbonoRepository extends JpaRepository<HistorialAbono, Long> {
}