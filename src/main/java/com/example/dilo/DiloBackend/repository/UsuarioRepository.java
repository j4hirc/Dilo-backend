package com.example.dilo.DiloBackend.repository;

import com.example.dilo.DiloBackend.model.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @EntityGraph(attributePaths = {"parroquia"})
    Optional<Usuario> findByDni(String username);

    @EntityGraph(attributePaths = {"parroquia"})
    Optional<Usuario> findByEmail(String email);
}
