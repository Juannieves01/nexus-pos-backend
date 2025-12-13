package com.nexuspos.backend.repository;

import com.nexuspos.backend.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    // Buscar ventas por rango de fechas
    List<Venta> findByCreatedAtBetween(LocalDateTime inicio, LocalDateTime fin);

    // Calcular total de ventas
    @Query("SELECT SUM(v.total) FROM Venta v")
    Double calcularTotalVentas();

    // Calcular total de efectivo
    @Query("SELECT SUM(v.efectivo) FROM Venta v")
    Double calcularTotalEfectivo();

    // Calcular total de transferencias
    @Query("SELECT SUM(v.transferencias) FROM Venta v")
    Double calcularTotalTransferencias();

    // Contar ventas
    long count();

    // ========= NUEVAS CONSULTAS PARA ESTADÍSTICAS =========

    // Obtener ventas de los últimos N días
    @Query("SELECT v FROM Venta v WHERE v.createdAt >= :fechaInicio ORDER BY v.createdAt DESC")
    List<Venta> findVentasUltimosDias(@Param("fechaInicio") LocalDateTime fechaInicio);

    // Calcular total de ventas por rango de fechas
    @Query("SELECT SUM(v.total) FROM Venta v WHERE v.createdAt BETWEEN :inicio AND :fin")
    Double calcularTotalVentasEnRango(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    // Contar ventas por rango de fechas
    @Query("SELECT COUNT(v) FROM Venta v WHERE v.createdAt BETWEEN :inicio AND :fin")
    Long contarVentasEnRango(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);
}
