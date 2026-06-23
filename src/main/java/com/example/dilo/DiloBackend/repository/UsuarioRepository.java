package com.example.dilo.DiloBackend.repository;

import com.example.dilo.DiloBackend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
