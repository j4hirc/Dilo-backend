package com.example.dilo.DiloBackend.repository;

import com.example.dilo.DiloBackend.model.DiloSpace;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DiloSpaceRepository extends JpaRepository<DiloSpace, Long> {

    Optional<DiloSpace> findByNombreComercial(String nombreComercial);

    @Query("SELECT d FROM DiloSpace d WHERE d.nombreComercial = :param OR d.ruc = :param OR d.razonSocial = :param")
    Optional<DiloSpace> findByCualquierCampo(@Param("param") String param);
}
