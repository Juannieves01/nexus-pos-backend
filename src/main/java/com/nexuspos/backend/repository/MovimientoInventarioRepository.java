package com.nexuspos.backend.repository;

import com.nexuspos.backend.model.MovimientoInventario;
import com.nexuspos.backend.model.Producto;
import com.nexuspos.backend.model.TipoMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REPOSITORIO MOVIMIENTO INVENTARIO
 *
 * Interface para acceso a datos de movimientos de inventario
 */
@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    /**
     * Buscar movimientos por producto
     */
    List<MovimientoInventario> findByProductoOrderByFechaDesc(Producto producto);

    /**
     * Buscar movimientos por tipo
     */
    List<MovimientoInventario> findByTipoOrderByFechaDesc(TipoMovimiento tipo);

    /**
     * Buscar movimientos en un rango de fechas
     */
    List<MovimientoInventario> findByFechaBetweenOrderByFechaDesc(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Buscar movimientos por producto y rango de fechas
     */
    List<MovimientoInventario> findByProductoAndFechaBetweenOrderByFechaDesc(
            Producto producto,
            LocalDateTime inicio,
            LocalDateTime fin
    );

    /**
     * Últimos N movimientos
     */
    List<MovimientoInventario> findTop10ByOrderByFechaDesc();

    /**
     * Calcular costo total de entradas en un rango
     */
    @Query("SELECT SUM(m.costoTotal) FROM MovimientoInventario m " +
           "WHERE m.tipo = 'ENTRADA' AND m.fecha BETWEEN :inicio AND :fin")
    Double calcularTotalEntradas(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Contar movimientos por tipo
     */
    long countByTipo(TipoMovimiento tipo);
}
