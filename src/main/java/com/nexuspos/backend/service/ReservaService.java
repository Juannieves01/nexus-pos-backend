package com.nexuspos.backend.service;

import com.nexuspos.backend.model.EstadoReserva;
import com.nexuspos.backend.model.Mesa;
import com.nexuspos.backend.model.Reserva;
import com.nexuspos.backend.repository.MesaRepository;
import com.nexuspos.backend.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final MesaRepository mesaRepository;

    /**
     * Obtener todas las reservas
     */
    public List<Reserva> findAll() {
        return reservaRepository.findAll();
    }

    /**
     * Obtener reserva por ID
     */
    public Optional<Reserva> findById(Long id) {
        return reservaRepository.findById(id);
    }

    /**
     * Obtener reservas activas
     */
    public List<Reserva> findReservasActivas() {
        return reservaRepository.findReservasActivas();
    }

    /**
     * Obtener reservas de hoy
     */
    public List<Reserva> findReservasHoy() {
        LocalDateTime inicioHoy = LocalDate.now().atStartOfDay();
        LocalDateTime finHoy = inicioHoy.plusDays(1);
        return reservaRepository.findReservasHoy(inicioHoy, finHoy);
    }

    /**
     * Obtener reservas en un rango de fechas
     */
    public List<Reserva> findReservasEnRango(LocalDateTime inicio, LocalDateTime fin) {
        return reservaRepository.findByFechaHoraBetween(inicio, fin);
    }

    /**
     * Buscar reservas por nombre de cliente
     */
    public List<Reserva> buscarPorCliente(String nombre) {
        return reservaRepository.findByNombreClienteContainingIgnoreCase(nombre);
    }

    /**
     * Verificar si hay conflicto de horario
     */
    private boolean existeConflictoHorario(Long mesaId, LocalDateTime inicio, LocalDateTime fin) {
        List<Reserva> reservasActivas = reservaRepository.findReservasActivasPorMesa(mesaId);

        for (Reserva r : reservasActivas) {
            LocalDateTime reservaInicio = r.getFechaHoraReserva();
            LocalDateTime reservaFin = reservaInicio.plusMinutes(r.getDuracionMinutos());

            // Verificar si hay solapamiento
            // Caso 1: La nueva reserva empieza durante una reserva existente
            // Caso 2: La nueva reserva termina durante una reserva existente
            // Caso 3: La nueva reserva contiene completamente una reserva existente
            if ((inicio.isBefore(reservaFin) && fin.isAfter(reservaInicio))) {
                return true;
            }
        }

        return false;
    }

    /**
     * Crear nueva reserva
     */
    @Transactional
    public Reserva create(Reserva reserva) {
        log.info("Creando reserva para {} en mesa {}",
                 reserva.getNombreCliente(), reserva.getMesa().getId());

        // Validar que la mesa existe
        Mesa mesa = mesaRepository.findById(reserva.getMesa().getId())
            .orElseThrow(() -> new IllegalArgumentException("Mesa no encontrada"));

        reserva.setMesa(mesa);

        // Validar que la fecha de reserva sea futura
        if (reserva.getFechaHoraReserva().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La fecha de reserva debe ser futura");
        }

        // Verificar disponibilidad de la mesa
        LocalDateTime inicio = reserva.getFechaHoraReserva();
        LocalDateTime fin = inicio.plusMinutes(reserva.getDuracionMinutos());

        if (existeConflictoHorario(mesa.getId(), inicio, fin)) {
            throw new IllegalStateException(
                "La mesa ya tiene una reserva en ese horario"
            );
        }

        return reservaRepository.save(reserva);
    }

    /**
     * Actualizar reserva existente
     */
    @Transactional
    public Reserva update(Long id, Reserva reservaActualizada) {
        Reserva reserva = reservaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        // Actualizar campos
        reserva.setNombreCliente(reservaActualizada.getNombreCliente());
        reserva.setTelefonoCliente(reservaActualizada.getTelefonoCliente());
        reserva.setEmailCliente(reservaActualizada.getEmailCliente());
        reserva.setFechaHoraReserva(reservaActualizada.getFechaHoraReserva());
        reserva.setDuracionMinutos(reservaActualizada.getDuracionMinutos());
        reserva.setNumeroPersonas(reservaActualizada.getNumeroPersonas());
        reserva.setNotas(reservaActualizada.getNotas());

        // Si cambia la mesa, actualizar
        if (reservaActualizada.getMesa() != null &&
            !reserva.getMesa().getId().equals(reservaActualizada.getMesa().getId())) {

            Mesa nuevaMesa = mesaRepository.findById(reservaActualizada.getMesa().getId())
                .orElseThrow(() -> new IllegalArgumentException("Mesa no encontrada"));

            reserva.setMesa(nuevaMesa);
        }

        return reservaRepository.save(reserva);
    }

    /**
     * Cambiar estado de reserva
     */
    @Transactional
    public Reserva cambiarEstado(Long id, EstadoReserva nuevoEstado) {
        Reserva reserva = reservaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        log.info("Cambiando estado de reserva {} de {} a {}",
                 id, reserva.getEstado(), nuevoEstado);

        reserva.setEstado(nuevoEstado);

        // Si el estado cambia a EN_MESA, ocupar la mesa
        if (nuevoEstado == EstadoReserva.EN_MESA) {
            Mesa mesa = reserva.getMesa();
            if (!mesa.getEstado().equals("ocupada")) {
                mesa.setEstado("ocupada");
                mesaRepository.save(mesa);
                log.info("Mesa {} marcada como ocupada por reserva", mesa.getId());
            }
        }

        return reservaRepository.save(reserva);
    }

    /**
     * Confirmar reserva
     */
    public Reserva confirmar(Long id) {
        return cambiarEstado(id, EstadoReserva.CONFIRMADA);
    }

    /**
     * Marcar cliente como llegado (en mesa)
     */
    public Reserva marcarEnMesa(Long id) {
        return cambiarEstado(id, EstadoReserva.EN_MESA);
    }

    /**
     * Cancelar reserva
     */
    public Reserva cancelar(Long id) {
        return cambiarEstado(id, EstadoReserva.CANCELADA);
    }

    /**
     * Marcar como no asistió
     */
    public Reserva marcarNoAsistio(Long id) {
        return cambiarEstado(id, EstadoReserva.NO_ASISTIO);
    }

    /**
     * Eliminar reserva
     */
    @Transactional
    public void delete(Long id) {
        Reserva reserva = reservaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        log.info("Eliminando reserva {} para {}", id, reserva.getNombreCliente());
        reservaRepository.delete(reserva);
    }

    /**
     * Verificar disponibilidad de mesa en un horario
     */
    public boolean verificarDisponibilidad(Long mesaId, LocalDateTime inicio, LocalDateTime fin) {
        return !existeConflictoHorario(mesaId, inicio, fin);
    }
}
