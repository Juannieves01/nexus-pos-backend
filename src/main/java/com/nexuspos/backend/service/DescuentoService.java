package com.nexuspos.backend.service;

import com.nexuspos.backend.model.Descuento;
import com.nexuspos.backend.repository.DescuentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DescuentoService {

    private final DescuentoRepository descuentoRepository;

    /**
     * Obtener todos los descuentos
     */
    public List<Descuento> findAll() {
        return descuentoRepository.findAll();
    }

    /**
     * Obtener descuento por ID
     */
    public Optional<Descuento> findById(Long id) {
        return descuentoRepository.findById(id);
    }

    /**
     * Obtener descuentos activos
     */
    public List<Descuento> findActivos() {
        return descuentoRepository.findByActivo(true);
    }

    /**
     * Obtener descuentos válidos actualmente
     */
    public List<Descuento> findValidos() {
        return descuentoRepository.findDescuentosValidos();
    }

    /**
     * Buscar descuentos por nombre
     */
    public List<Descuento> buscarPorNombre(String nombre) {
        return descuentoRepository.findByNombreContainingIgnoreCase(nombre);
    }

    /**
     * Crear descuento
     */
    @Transactional
    public Descuento create(Descuento descuento) {
        log.info("Creando descuento: {}", descuento.getNombre());

        // Validaciones
        if (descuento.getValor() <= 0) {
            throw new IllegalArgumentException("El valor del descuento debe ser mayor a 0");
        }

        if (descuento.getTipo() == com.nexuspos.backend.model.TipoDescuento.PORCENTAJE) {
            if (descuento.getValor() > 100) {
                throw new IllegalArgumentException("El porcentaje no puede ser mayor a 100");
            }
        }

        if (descuento.getCompraMinima() != null && descuento.getCompraMinima() < 0) {
            throw new IllegalArgumentException("La compra mínima no puede ser negativa");
        }

        if (descuento.getFechaInicio() != null &&
            descuento.getFechaFin() != null &&
            descuento.getFechaFin().isBefore(descuento.getFechaInicio())) {
            throw new IllegalArgumentException("La fecha de fin debe ser posterior a la fecha de inicio");
        }

        return descuentoRepository.save(descuento);
    }

    /**
     * Actualizar descuento
     */
    @Transactional
    public Descuento update(Long id, Descuento descuentoActualizado) {
        Descuento descuento = descuentoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Descuento no encontrado"));

        log.info("Actualizando descuento {}", id);

        // Validar valor
        if (descuentoActualizado.getValor() != null) {
            if (descuentoActualizado.getValor() <= 0) {
                throw new IllegalArgumentException("El valor del descuento debe ser mayor a 0");
            }
            descuento.setValor(descuentoActualizado.getValor());
        }

        // Actualizar campos
        if (descuentoActualizado.getNombre() != null) {
            descuento.setNombre(descuentoActualizado.getNombre());
        }
        if (descuentoActualizado.getDescripcion() != null) {
            descuento.setDescripcion(descuentoActualizado.getDescripcion());
        }
        if (descuentoActualizado.getTipo() != null) {
            descuento.setTipo(descuentoActualizado.getTipo());
        }
        if (descuentoActualizado.getCompraMinima() != null) {
            descuento.setCompraMinima(descuentoActualizado.getCompraMinima());
        }
        if (descuentoActualizado.getActivo() != null) {
            descuento.setActivo(descuentoActualizado.getActivo());
        }
        if (descuentoActualizado.getFechaInicio() != null) {
            descuento.setFechaInicio(descuentoActualizado.getFechaInicio());
        }
        if (descuentoActualizado.getFechaFin() != null) {
            descuento.setFechaFin(descuentoActualizado.getFechaFin());
        }

        return descuentoRepository.save(descuento);
    }

    /**
     * Activar/desactivar descuento
     */
    @Transactional
    public Descuento toggleActivo(Long id) {
        Descuento descuento = descuentoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Descuento no encontrado"));

        descuento.setActivo(!descuento.getActivo());
        log.info("Descuento {} ahora está {}", id, descuento.getActivo() ? "activo" : "inactivo");

        return descuentoRepository.save(descuento);
    }

    /**
     * Eliminar descuento
     */
    @Transactional
    public void delete(Long id) {
        Descuento descuento = descuentoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Descuento no encontrado"));

        log.info("Eliminando descuento {} - {}", id, descuento.getNombre());
        descuentoRepository.delete(descuento);
    }

    /**
     * Calcular el mejor descuento aplicable a un total
     */
    public Optional<Descuento> encontrarMejorDescuento(double total) {
        List<Descuento> descuentosValidos = findValidos();

        return descuentosValidos.stream()
            .filter(d -> d.getCompraMinima() == null || total >= d.getCompraMinima())
            .max((d1, d2) -> Double.compare(d1.calcularDescuento(total), d2.calcularDescuento(total)));
    }
}
