package com.nexuspos.backend.service;

import com.nexuspos.backend.model.Caja;
import com.nexuspos.backend.model.Cierre;
import com.nexuspos.backend.model.Turno;
import com.nexuspos.backend.repository.CajaRepository;
import com.nexuspos.backend.repository.CierreRepository;
import com.nexuspos.backend.repository.GastoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * SERVICE DE CAJA
 *
 * Maneja la lógica de múltiples cajas y turnos
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CajaService {

    private final CajaRepository cajaRepository;
    private final CierreRepository cierreRepository;
    private final GastoRepository gastoRepository;

    /**
     * Obtiene todas las cajas abiertas
     */
    @Transactional(readOnly = true)
    public List<Caja> getCajasAbiertas() {
        log.debug("Obteniendo todas las cajas abiertas");
        return cajaRepository.findByAbierta(true);
    }

    /**
     * Obtiene una caja por ID
     */
    @Transactional(readOnly = true)
    public Optional<Caja> getCajaById(Long id) {
        log.debug("Obteniendo caja con ID: {}", id);
        return cajaRepository.findById(id);
    }

    /**
     * Obtiene caja abierta por número de caja
     */
    @Transactional(readOnly = true)
    public Optional<Caja> getCajaAbiertaPorNumero(Integer numeroCaja) {
        log.debug("Obteniendo caja abierta número: {}", numeroCaja);
        return cajaRepository.findByNumeroCajaAndAbierta(numeroCaja, true);
    }

    /**
     * Obtiene todas las cajas (abiertas y cerradas)
     */
    @Transactional(readOnly = true)
    public List<Caja> getAllCajas() {
        return cajaRepository.findAll();
    }

    /**
     * Abre una nueva caja
     */
    @Transactional
    public Caja abrirCaja(Integer numeroCaja, Turno turno, Double montoInicial, String usuario) {
        log.info("Abriendo caja {} - Turno: {} - Usuario: {}", numeroCaja, turno, usuario);

        // Verificar si ya existe una caja abierta con ese número
        if (cajaRepository.existsByNumeroCajaAndAbierta(numeroCaja, true)) {
            throw new IllegalStateException("Ya existe una caja abierta con el número " + numeroCaja);
        }

        Caja caja = new Caja();
        caja.abrir(numeroCaja, turno, montoInicial, usuario);

        Caja saved = cajaRepository.save(caja);
        log.info("Caja {} abierta exitosamente con ID: {}", numeroCaja, saved.getId());

        return saved;
    }

    /**
     * Cierra una caja específica y guarda el histórico
     */
    @Transactional
    public Caja cerrarCaja(Long cajaId, String usuario) {
        log.info("Cerrando caja ID: {} - Usuario: {}", cajaId, usuario);

        Caja caja = cajaRepository.findById(cajaId)
            .orElseThrow(() -> new IllegalStateException("No existe una caja con ID: " + cajaId));

        if (!caja.isAbierta()) {
            throw new IllegalStateException("La caja ya está cerrada");
        }

        // Calcular totales de gastos (solo del período actual)
        // TODO: Filtrar gastos solo del período de esta caja
        Double totalGastos = gastoRepository.calcularTotalGastos() != null ? gastoRepository.calcularTotalGastos() : 0.0;

        // Crear registro de cierre para histórico
        Cierre cierre = new Cierre();
        cierre.setFechaApertura(caja.getFechaApertura());
        cierre.setBaseInicial(caja.getBaseInicial());
        cierre.setEfectivo(caja.getEfectivo());
        cierre.setTransferencias(caja.getTransferencias());
        cierre.setTotalVentas(caja.getTotal() - caja.getBaseInicial()); // Total vendido sin contar base
        cierre.setTotalGastos(totalGastos);
        cierre.setSaldoPorCobrar(caja.getSaldoPorCobrar());
        cierre.setBaseSiguiente(caja.getBaseInicial()); // Reutilizar la misma base
        cierre.setReporte(String.format("Cierre Caja %d - Turno: %s - Usuario: %s",
            caja.getNumeroCaja(), caja.getTurno(), usuario));

        cierreRepository.save(cierre);
        log.info("Registro de cierre guardado con ID: {}", cierre.getId());

        // Cerrar caja
        caja.cerrar(usuario);
        Caja saved = cajaRepository.save(caja);

        log.info("Caja {} cerrada. Total final: {}", caja.getNumeroCaja(), saved.getTotal());

        return saved;
    }

    /**
     * Registra una venta en efectivo en una caja específica
     */
    @Transactional
    public Caja registrarVentaEfectivo(Long cajaId, Double monto) {
        log.debug("Registrando venta en efectivo: {} en caja ID: {}", monto, cajaId);

        Caja caja = cajaRepository.findById(cajaId)
            .orElseThrow(() -> new IllegalStateException("La caja no existe"));

        caja.registrarVentaEfectivo(monto);
        return cajaRepository.save(caja);
    }

    /**
     * Registra una venta por transferencia en una caja específica
     */
    @Transactional
    public Caja registrarVentaTransferencia(Long cajaId, Double monto) {
        log.debug("Registrando venta por transferencia: {} en caja ID: {}", monto, cajaId);

        Caja caja = cajaRepository.findById(cajaId)
            .orElseThrow(() -> new IllegalStateException("La caja no existe"));

        caja.registrarVentaTransferencia(monto);
        return cajaRepository.save(caja);
    }

    /**
     * Registra un gasto en efectivo en una caja específica
     */
    @Transactional
    public Caja registrarGastoEfectivo(Long cajaId, Double monto) {
        log.debug("Registrando gasto en efectivo: {} en caja ID: {}", monto, cajaId);

        Caja caja = cajaRepository.findById(cajaId)
            .orElseThrow(() -> new IllegalStateException("La caja no existe"));

        caja.registrarGastoEfectivo(monto);
        return cajaRepository.save(caja);
    }

    /**
     * Registra un gasto por transferencia en una caja específica
     */
    @Transactional
    public Caja registrarGastoTransferencia(Long cajaId, Double monto) {
        log.debug("Registrando gasto por transferencia: {} en caja ID: {}", monto, cajaId);

        Caja caja = cajaRepository.findById(cajaId)
            .orElseThrow(() -> new IllegalStateException("La caja no existe"));

        caja.registrarGastoTransferencia(monto);
        return cajaRepository.save(caja);
    }

    /**
     * Actualiza el saldo por cobrar de una caja específica
     */
    @Transactional
    public Caja actualizarSaldoPorCobrar(Long cajaId, Double nuevoSaldo) {
        log.debug("Actualizando saldo por cobrar: {} en caja ID: {}", nuevoSaldo, cajaId);

        Caja caja = cajaRepository.findById(cajaId)
            .orElseThrow(() -> new IllegalStateException("La caja no existe"));

        caja.setSaldoPorCobrar(nuevoSaldo);
        return cajaRepository.save(caja);
    }

    /**
     * Obtiene el historial de una caja específica
     */
    @Transactional(readOnly = true)
    public List<Caja> getHistorialCaja(Integer numeroCaja) {
        return cajaRepository.findByNumeroCajaOrderByFechaAperturaDesc(numeroCaja);
    }

    /**
     * Cuenta las cajas abiertas
     */
    @Transactional(readOnly = true)
    public long contarCajasAbiertas() {
        return cajaRepository.countByAbierta(true);
    }
}
