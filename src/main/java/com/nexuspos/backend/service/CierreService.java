package com.nexuspos.backend.service;

import com.nexuspos.backend.model.Cierre;
import com.nexuspos.backend.repository.CierreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CierreService {

    private final CierreRepository cierreRepository;

    /**
     * Guardar un cierre de caja
     */
    @Transactional
    public Cierre guardarCierre(Cierre cierre) {
        log.info("Guardando cierre de caja");
        return cierreRepository.save(cierre);
    }

    /**
     * Obtener todos los cierres
     */
    public List<Cierre> findAll() {
        return cierreRepository.findAll();
    }

    /**
     * Obtener cierre por ID
     */
    public Optional<Cierre> findById(Long id) {
        return cierreRepository.findById(id);
    }

    /**
     * Obtener cierres por rango de fechas
     */
    public List<Cierre> findByRango(LocalDateTime inicio, LocalDateTime fin) {
        log.info("Buscando cierres entre {} y {}", inicio, fin);
        return cierreRepository.findByFechaCierreBetween(inicio, fin);
    }

    /**
     * Obtener último cierre
     */
    public Optional<Cierre> getUltimoCierre() {
        Cierre ultimo = cierreRepository.findFirstByOrderByFechaCierreDesc();
        return Optional.ofNullable(ultimo);
    }

    /**
     * Contar total de cierres
     */
    public long count() {
        return cierreRepository.count();
    }

    /**
     * Calcular total de ventas acumuladas de todos los cierres
     */
    public Double calcularTotalVentasAcumuladas() {
        Double total = cierreRepository.calcularTotalVentasAcumuladas();
        return total != null ? total : 0.0;
    }

    /**
     * Eliminar cierre (solo para correcciones)
     */
    @Transactional
    public void delete(Long id) {
        log.warn("Eliminando cierre ID: {}", id);
        cierreRepository.deleteById(id);
    }
}
