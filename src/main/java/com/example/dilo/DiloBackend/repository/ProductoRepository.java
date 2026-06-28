package com.example.dilo.DiloBackend.repository;

import com.example.dilo.DiloBackend.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
}
