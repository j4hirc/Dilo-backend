package com.example.dilo.DiloBackend.repository;

import com.example.dilo.DiloBackend.model.InventarioBodega;
import jakarta.persistence.Entity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventarioBodegaRepository extends JpaRepository<InventarioBodega, Long> {
    @Query("SELECT ib FROM InventarioBodega ib JOIN FETCH ib.producto JOIN FETCH ib.bodega WHERE ib.negocio.id = :negocioId")
    List<InventarioBodega> findByNegocioId(@Param("negocioId") Long negocioId);

    @Query("SELECT ib FROM InventarioBodega ib JOIN FETCH ib.producto JOIN FETCH ib.bodega WHERE ib.bodega.id = :bodegaId AND ib.negocio.id = :negocioId")
    List<InventarioBodega> findByBodegaIdAndNegocioId(@Param("bodegaId") Long bodegaId, @Param("negocioId") Long negocioId);


    @Query("SELECT ib FROM InventarioBodega ib JOIN FETCH ib.producto JOIN FETCH ib.bodega " +
            "WHERE ib.bodega.id = :bodegaId AND ib.negocio.id = :negocioId AND ib.producto.id = :productoId")
    Optional<InventarioBodega> findByBodegaIdAndNegocioIdAndProductoId(
            @Param("bodegaId") Long bodegaId,
            @Param("negocioId") Long negocioId,
            @Param("productoId") Long productoId);

    @Query("SELECT ib FROM InventarioBodega ib JOIN FETCH ib.producto JOIN FETCH ib.bodega WHERE ib.id = :id AND ib.negocio.id = :negocioId")
    Optional<InventarioBodega> findByIdAndNegocioId(@Param("id") Long id, @Param("negocioId") Long negocioId);

    @Query("SELECT COUNT(ib) > 0 FROM InventarioBodega ib WHERE ib.producto.id = :productoId AND ib.bodega.id = :bodegaId AND ib.negocio.id = :negocioId")
    boolean existsByProductoIdAndBodegaIdAndNegocioId(
            @Param("productoId") Long productoId,
            @Param("bodegaId") Long bodegaId,
            @Param("negocioId") Long negocioId);
}