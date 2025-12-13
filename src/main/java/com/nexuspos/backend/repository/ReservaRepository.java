package com.nexuspos.backend.repository;

import com.nexuspos.backend.model.EstadoReserva;
import com.nexuspos.backend.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    // Buscar por estado
    List<Reserva> findByEstado(EstadoReserva estado);

    // Buscar por mesa
    List<Reserva> findByMesaId(Long mesaId);

    // Buscar reservas activas (pendiente, confirmada, en_mesa)
    @Query("SELECT r FROM Reserva r WHERE r.estado IN ('PENDIENTE', 'CONFIRMADA', 'EN_MESA')")
    List<Reserva> findReservasActivas();

    // Buscar reservas en un rango de fechas
    @Query("SELECT r FROM Reserva r WHERE r.fechaHoraReserva BETWEEN :inicio AND :fin ORDER BY r.fechaHoraReserva")
    List<Reserva> findByFechaHoraBetween(
        @Param("inicio") LocalDateTime inicio,
        @Param("fin") LocalDateTime fin
    );

    // Buscar reservas de hoy
    @Query("SELECT r FROM Reserva r WHERE " +
           "r.fechaHoraReserva >= :inicioHoy AND " +
           "r.fechaHoraReserva < :finHoy " +
           "ORDER BY r.fechaHoraReserva")
    List<Reserva> findReservasHoy(
        @Param("inicioHoy") LocalDateTime inicioHoy,
        @Param("finHoy") LocalDateTime finHoy
    );

    // Buscar por nombre de cliente
    List<Reserva> findByNombreClienteContainingIgnoreCase(String nombre);

    // Obtener reservas activas de una mesa para verificar conflictos
    @Query("SELECT r FROM Reserva r WHERE " +
           "r.mesa.id = :mesaId AND " +
           "r.estado IN ('PENDIENTE', 'CONFIRMADA', 'EN_MESA')")
    List<Reserva> findReservasActivasPorMesa(@Param("mesaId") Long mesaId);
}
