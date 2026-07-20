package com.example.dilo.DiloBackend.repository;

import com.example.dilo.DiloBackend.model.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
    List<Proveedor> findByNegocioId(Long negocioId);
    Optional<Proveedor> findByDni(String dni);
    boolean existsByDni(String dni);
}
