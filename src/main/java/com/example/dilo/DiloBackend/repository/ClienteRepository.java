package com.example.dilo.DiloBackend.repository;

import com.example.dilo.DiloBackend.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}
