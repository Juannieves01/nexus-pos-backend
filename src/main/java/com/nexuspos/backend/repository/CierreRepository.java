package com.nexuspos.backend.repository;

import com.nexuspos.backend.model.Cierre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CierreRepository extends JpaRepository<Cierre, Long> {

    // Buscar cierres por rango de fechas
    List<Cierre> findByFechaCierreBetween(LocalDateTime inicio, LocalDateTime fin);

    // Obtener último cierre
    Cierre findFirstByOrderByFechaCierreDesc();

    // Calcular total de ventas acumuladas
    @Query("SELECT SUM(c.totalVentas) FROM Cierre c")
    Double calcularTotalVentasAcumuladas();

    // Contar cierres
    long count();
}
