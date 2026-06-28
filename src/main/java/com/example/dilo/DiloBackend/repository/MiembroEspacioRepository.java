package com.example.dilo.DiloBackend.repository;

import com.example.dilo.DiloBackend.model.MiembroEspacio;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MiembroEspacioRepository extends JpaRepository<MiembroEspacio, Long> {


    @EntityGraph(attributePaths = {"usuario", "espacio", "rol"})
    @Query("SELECT m FROM MiembroEspacio m WHERE m.usuario.id = :usuarioId AND m.espacio IS NULL AND m.rol.nombre = 'SUPER_ADMIN'")
    Optional<MiembroEspacio> findSuperAdminByUsuarioId(@Param("usuarioId") Long usuarioId);

    @EntityGraph(attributePaths = {"usuario", "espacio", "rol"})
    Optional<MiembroEspacio> findById(Long usuarioId, Long espacioId);
}
