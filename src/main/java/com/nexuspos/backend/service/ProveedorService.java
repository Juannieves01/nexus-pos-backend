package com.nexuspos.backend.service;

import com.nexuspos.backend.model.Proveedor;
import com.nexuspos.backend.repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * SERVICE PROVEEDOR
 *
 * Lógica de negocio para gestión de proveedores
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;

    // =========================================================================
    // CRUD BÁSICO
    // =========================================================================

    @Transactional(readOnly = true)
    public List<Proveedor> findAll() {
        log.debug("Obteniendo todos los proveedores");
        return proveedorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Proveedor> findActivos() {
        log.debug("Obteniendo proveedores activos");
        return proveedorRepository.findByActivoTrueOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public Optional<Proveedor> findById(Long id) {
        log.debug("Buscando proveedor con id: {}", id);
        return proveedorRepository.findById(id);
    }

    @Transactional
    public Proveedor create(Proveedor proveedor) {
        log.info("Creando proveedor: {}", proveedor.getNombre());

        // Validar nombre único
        if (proveedorRepository.existsByNombre(proveedor.getNombre())) {
            throw new IllegalArgumentException("Ya existe un proveedor con el nombre: " + proveedor.getNombre());
        }

        Proveedor saved = proveedorRepository.save(proveedor);
        log.info("Proveedor creado exitosamente con id: {}", saved.getId());

        return saved;
    }

    @Transactional
    public Proveedor update(Long id, Proveedor proveedorActualizado) {
        log.info("Actualizando proveedor con id: {}", id);

        Proveedor existente = proveedorRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado con id: " + id));

        // Validar nombre único (si cambió)
        if (!existente.getNombre().equals(proveedorActualizado.getNombre())) {
            if (proveedorRepository.existsByNombre(proveedorActualizado.getNombre())) {
                throw new IllegalArgumentException("Ya existe otro proveedor con el nombre: " + proveedorActualizado.getNombre());
            }
        }

        existente.setNombre(proveedorActualizado.getNombre());
        existente.setTelefono(proveedorActualizado.getTelefono());
        existente.setEmail(proveedorActualizado.getEmail());
        existente.setActivo(proveedorActualizado.getActivo());

        Proveedor updated = proveedorRepository.save(existente);
        log.info("Proveedor actualizado exitosamente");

        return updated;
    }

    @Transactional
    public void delete(Long id) {
        log.info("Eliminando proveedor con id: {}", id);

        Proveedor proveedor = proveedorRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado con id: " + id));

        proveedorRepository.deleteById(id);
        log.info("Proveedor eliminado exitosamente");
    }

    @Transactional
    public Proveedor toggleActivo(Long id) {
        log.info("Cambiando estado de proveedor con id: {}", id);

        Proveedor proveedor = proveedorRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado con id: " + id));

        proveedor.setActivo(!proveedor.getActivo());
        return proveedorRepository.save(proveedor);
    }

    // =========================================================================
    // BÚSQUEDAS
    // =========================================================================

    @Transactional(readOnly = true)
    public List<Proveedor> search(String nombre) {
        log.debug("Buscando proveedores por nombre: {}", nombre);
        return proveedorRepository.findByNombreContainingIgnoreCaseOrderByNombreAsc(nombre);
    }

    @Transactional(readOnly = true)
    public long countActivos() {
        return proveedorRepository.countByActivoTrue();
    }
}
