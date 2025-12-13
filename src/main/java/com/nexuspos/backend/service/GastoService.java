package com.nexuspos.backend.service;

import com.nexuspos.backend.model.Gasto;
import com.nexuspos.backend.repository.GastoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SERVICE DE GASTO
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GastoService {

    private final GastoRepository gastoRepository;
    private final CajaService cajaService;

    /**
     * Obtiene todos los gastos
     */
    @Transactional(readOnly = true)
    public List<Gasto> findAll() {
        log.debug("Obteniendo todos los gastos");
        return gastoRepository.findAllByOrderByFechaDesc();
    }

    /**
     * Busca un gasto por ID
     */
    @Transactional(readOnly = true)
    public Optional<Gasto> findById(Long id) {
        return gastoRepository.findById(id);
    }

    /**
     * Crea un nuevo gasto
     *
     * LÓGICA DE NEGOCIO:
     * 1. Crea el gasto
     * 2. Descuenta de la caja según el tipo de pago
     *
     * @param gasto Gasto a crear
     * @param cajaId ID de la caja de la que se descuenta (puede ser null para no descontar)
     */
    @Transactional
    public Gasto create(Gasto gasto, Long cajaId) {
        log.info("Creando nuevo gasto: {} - ${} - Caja ID: {}",
                gasto.getConcepto(), gasto.getMonto(), cajaId);

        // Guardar gasto
        Gasto saved = gastoRepository.save(gasto);

        // Descontar de caja según tipo de pago (si se especificó caja)
        if (cajaId != null) {
            if (gasto.isEfectivo()) {
                cajaService.registrarGastoEfectivo(cajaId, gasto.getMonto());
            } else if (gasto.isTransferencia()) {
                cajaService.registrarGastoTransferencia(cajaId, gasto.getMonto());
            }
        }

        log.info("Gasto creado exitosamente con id: {}", saved.getId());

        return saved;
    }

    /**
     * Elimina un gasto
     * NOTA: NO devuelve el dinero a la caja
     */
    @Transactional
    public void delete(Long id) {
        log.info("Eliminando gasto con id: {}", id);

        if (!gastoRepository.existsById(id)) {
            throw new IllegalArgumentException("Gasto no encontrado con id: " + id);
        }

        gastoRepository.deleteById(id);
        log.info("Gasto eliminado");
    }

    /**
     * Busca gastos por período
     */
    @Transactional(readOnly = true)
    public List<Gasto> findByPeriodo(String periodo) {
        return gastoRepository.findByPeriodo(periodo);
    }

    /**
     * Busca gastos por tipo de pago
     */
    @Transactional(readOnly = true)
    public List<Gasto> findByTipoPago(String tipoPago) {
        return gastoRepository.findByTipoPago(tipoPago);
    }

    /**
     * Busca gastos entre fechas
     */
    @Transactional(readOnly = true)
    public List<Gasto> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin) {
        return gastoRepository.findByFechaBetween(inicio, fin);
    }

    /**
     * Calcula total de gastos
     */
    @Transactional(readOnly = true)
    public Double calcularTotal() {
        Double total = gastoRepository.calcularTotalGastos();
        return total != null ? total : 0.0;
    }

    /**
     * Calcula total por tipo de pago
     */
    @Transactional(readOnly = true)
    public Double calcularTotalPorTipoPago(String tipoPago) {
        Double total = gastoRepository.calcularTotalPorTipoPago(tipoPago);
        return total != null ? total : 0.0;
    }

    /**
     * Calcula total por período
     */
    @Transactional(readOnly = true)
    public Double calcularTotalPorPeriodo(String periodo) {
        Double total = gastoRepository.calcularTotalPorPeriodo(periodo);
        return total != null ? total : 0.0;
    }

    /**
     * Cuenta total de gastos
     */
    @Transactional(readOnly = true)
    public long count() {
        return gastoRepository.count();
    }
}
