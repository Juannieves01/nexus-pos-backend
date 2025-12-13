package com.nexuspos.backend.repository;

import com.nexuspos.backend.model.Caja;
import com.nexuspos.backend.model.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * REPOSITORY DE CAJA
 *
 * Ahora soporta múltiples cajas con ID Long
 */
@Repository
public interface CajaRepository extends JpaRepository<Caja, Long> {

    /**
     * Buscar todas las cajas abiertas
     */
    List<Caja> findByAbierta(Boolean abierta);

    /**
     * Buscar caja abierta por número de caja
     */
    Optional<Caja> findByNumeroCajaAndAbierta(Integer numeroCaja, Boolean abierta);

    /**
     * Buscar cajas por número de caja (abiertas y cerradas)
     */
    List<Caja> findByNumeroCajaOrderByFechaAperturaDesc(Integer numeroCaja);

    /**
     * Buscar cajas por turno
     */
    List<Caja> findByTurnoAndAbierta(Turno turno, Boolean abierta);

    /**
     * Verificar si existe una caja abierta para un número específico
     */
    boolean existsByNumeroCajaAndAbierta(Integer numeroCaja, Boolean abierta);

    /**
     * Contar cajas abiertas
     */
    long countByAbierta(Boolean abierta);
}
