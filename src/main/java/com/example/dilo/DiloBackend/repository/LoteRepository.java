package com.example.dilo.DiloBackend.repository;

import com.example.dilo.DiloBackend.model.Lote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoteRepository extends JpaRepository<Lote, Long> {

    @Query("SELECT l FROM Lote l WHERE l.producto.id = :productoId AND l.bodega.id = :bodegaId " +
            "AND l.negocio.id = :negocioId AND l.estado = 'ACTIVO' AND l.cantidadDisponible > 0 " +
            "ORDER BY l.fechaIngreso ASC")
    List<Lote> findLotesActivosFIFO(@Param("productoId") Long productoId,
                                    @Param("bodegaId") Long bodegaId,
                                    @Param("negocioId") Long negocioId);

    @Query("SELECT l FROM Lote l WHERE l.producto.id = :productoId AND l.bodega.id = :bodegaId " +
            "AND l.negocio.id = :negocioId AND l.estado = 'ACTIVO' AND l.cantidadDisponible > 0 " +
            "ORDER BY l.fechaIngreso DESC")
    List<Lote> findLotesActivosLIFO(@Param("productoId") Long productoId,
                                    @Param("bodegaId") Long bodegaId,
                                    @Param("negocioId") Long negocioId);

    Lote findFirstByProductoIdAndNegocioIdOrderByFechaIngresoDesc(Long productoId, Long negocioId);
}