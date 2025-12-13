package com.nexuspos.backend.repository;

import com.nexuspos.backend.model.Gasto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REPOSITORY DE GASTO
 */
@Repository
public interface GastoRepository extends JpaRepository<Gasto, Long> {

    // Buscar gastos por período
    List<Gasto> findByPeriodo(String periodo);

    // Buscar gastos por tipo de pago
    List<Gasto> findByTipoPago(String tipoPago);

    // Buscar gastos por categoría
    List<Gasto> findByCategoria(String categoria);

    // Buscar gastos entre fechas
    List<Gasto> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);

    // Calcular total de gastos
    @Query("SELECT SUM(g.monto) FROM Gasto g")
    Double calcularTotalGastos();

    // Calcular total de gastos por tipo de pago
    @Query("SELECT SUM(g.monto) FROM Gasto g WHERE g.tipoPago = :tipoPago")
    Double calcularTotalPorTipoPago(@Param("tipoPago") String tipoPago);

    // Calcular total de gastos por período
    @Query("SELECT SUM(g.monto) FROM Gasto g WHERE g.periodo = :periodo")
    Double calcularTotalPorPeriodo(@Param("periodo") String periodo);

    // Obtener gastos ordenados por fecha descendente
    List<Gasto> findAllByOrderByFechaDesc();
}
