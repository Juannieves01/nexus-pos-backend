package com.nexuspos.backend.repository;

import com.nexuspos.backend.model.DetalleCompra;
import com.nexuspos.backend.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * REPOSITORY DETALLE COMPRA
 *
 * Interface para acceso a datos de detalles de compra
 */
@Repository
public interface DetalleCompraRepository extends JpaRepository<DetalleCompra, Long> {

    /**
     * Buscar detalles de una compra específica
     */
    List<DetalleCompra> findByCompraId(Long compraId);

    /**
     * Buscar detalles por producto
     */
    List<DetalleCompra> findByProductoOrderByCompra_CreatedAtDesc(Producto producto);

    /**
     * Calcular cantidad total comprada de un producto
     */
    @Query("SELECT SUM(d.cantidad) FROM DetalleCompra d WHERE d.producto.id = :productoId")
    Integer calcularTotalCompradoProducto(@Param("productoId") Long productoId);
}
