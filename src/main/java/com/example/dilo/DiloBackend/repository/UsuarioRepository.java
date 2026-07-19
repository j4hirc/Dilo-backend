package com.example.dilo.DiloBackend.repository;

import com.example.dilo.DiloBackend.dto.response.UsuarioResponseDTO;
import com.example.dilo.DiloBackend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    @Query("SELECT u FROM Usuario u WHERE u.estadoLaboral != 'Unemployed'")
    List<Usuario> findPersonalActivo();

    @Query("SELECT m.usuario FROM MiembroNegocio m WHERE m.negocio.id = :negocioId")
    List<Usuario> findUsuariosByNegocioId(@Param("negocioId") Long negocioId);



}