package com.example.dilo.DiloBackend.repository;

import com.example.dilo.DiloBackend.model.MiembroNegocio;
import com.example.dilo.DiloBackend.model.Negocio;
import com.example.dilo.DiloBackend.model.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MiembroNegocioRepository extends JpaRepository<MiembroNegocio, Long> {

    @EntityGraph(attributePaths = {"usuario", "rol", "negocio"})
    Optional<MiembroNegocio> findById(Long id);

    @Query("SELECT m FROM MiembroNegocio m WHERE m.negocio.id = :negocioId AND m.estadoLaboral != 'Unemployed'")
    List<MiembroNegocio> findPersonalActivoByNegocio(@Param("negocioId") Long negocioId);

    @EntityGraph(attributePaths = {"usuario", "rol", "negocio"})
    List<MiembroNegocio> findByUsuarioId(Long usuarioId);

    @EntityGraph(attributePaths = {"usuario", "rol", "negocio"})
    List<MiembroNegocio> findByUsuarioEmail(String email);


    @EntityGraph(attributePaths = {"usuario", "rol", "negocio"})
    List<MiembroNegocio> findByNegocioId(Long negocioId);


    @EntityGraph(attributePaths = {"usuario", "rol", "negocio"})
    boolean existsByUsuarioIdAndNegocioId(Long usuarioId, Long negocioId);

    @Query("SELECT u.email FROM MiembroNegocio mn JOIN mn.usuario u JOIN mn.rol r WHERE mn.negocio.id = :negocioId AND r.nombre IN :roles AND mn.estadoLaboral = 'ACTIVO'")
    List<String> findCorreosByNegocioAndRoles(@Param("negocioId") Long negocioId, @Param("roles") List<String> roles);

}