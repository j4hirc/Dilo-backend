package com.example.dilo.DiloBackend.repository;

import com.example.dilo.DiloBackend.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}
