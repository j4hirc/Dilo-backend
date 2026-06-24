package com.example.dilo.DiloBackend.repository;

import com.example.dilo.DiloBackend.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    @Query("SELECT r FROM Role r WHERE r.nombre = :nombre")
    Optional<Role> findByNombre(@Param("nombre") String nombre);
}
