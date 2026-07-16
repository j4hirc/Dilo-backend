package com.example.dilo.DiloBackend.repository;

import com.example.dilo.DiloBackend.model.TransaccionInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransaccionInventarioRepository extends JpaRepository<TransaccionInventario, Long> {

    @Query("SELECT t FROM TransaccionInventario t " +
            "JOIN FETCH t.producto " +
            "JOIN FETCH t.usuarioResponsable " +
            "LEFT JOIN FETCH t.bodegaOrigen " +
            "LEFT JOIN FETCH t.bodegaDestino " +
            "WHERE t.negocio.id = :negocioId ORDER BY t.fechaTransaccion DESC")
    List<TransaccionInventario> obtenerKardexPorNegocio(@Param("negocioId") Long negocioId);

    @Query("SELECT t FROM TransaccionInventario t " +
            "JOIN FETCH t.producto " +
            "JOIN FETCH t.usuarioResponsable " +
            "LEFT JOIN FETCH t.bodegaOrigen " +
            "LEFT JOIN FETCH t.bodegaDestino " +
            "WHERE t.negocio.id = :negocioId AND t.producto.id = :productoId ORDER BY t.fechaTransaccion DESC")
    List<TransaccionInventario> obtenerKardexPorProducto(@Param("negocioId") Long negocioId, @Param("productoId") Long productoId);

}
