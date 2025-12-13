package com.nexuspos.backend.repository;

import com.nexuspos.backend.model.Compra;
import com.nexuspos.backend.model.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * REPOSITORY COMPRA
 *
 * Interface para acceso a datos de compras
 */
@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {

    /**
     * Buscar todas las compras ordenadas por fecha (más recientes primero)
     */
    List<Compra> findAllByOrderByCreatedAtDesc();

    /**
     * Buscar compras de un proveedor específico
     */
    List<Compra> findByProveedorOrderByCreatedAtDesc(Proveedor proveedor);

    /**
     * Buscar compras por rango de fechas de entrega
     */
    List<Compra> findByFechaEntregaBetweenOrderByFechaEntregaDesc(LocalDate inicio, LocalDate fin);

    /**
     * Buscar compras por método de pago
     */
    List<Compra> findByMetodoPagoOrderByCreatedAtDesc(String metodoPago);

    /**
     * Buscar compras por número de documento
     */
    List<Compra> findByNumeroDocumentoContainingIgnoreCase(String numeroDocumento);

    /**
     * Obtener últimas N compras
     */
    List<Compra> findTop10ByOrderByCreatedAtDesc();

    /**
     * Calcular total de compras en un periodo
     */
    @Query("SELECT SUM(c.total) FROM Compra c WHERE c.createdAt BETWEEN :inicio AND :fin")
    Double calcularTotalCompras(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    /**
     * Contar compras por método de pago
     */
    long countByMetodoPago(String metodoPago);
}
