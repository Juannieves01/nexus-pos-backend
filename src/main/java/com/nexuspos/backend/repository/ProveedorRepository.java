package com.nexuspos.backend.repository;

import com.nexuspos.backend.model.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * REPOSITORY PROVEEDOR
 *
 * Interface para acceso a datos de proveedores
 */
@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    /**
     * Buscar proveedores activos
     */
    List<Proveedor> findByActivoTrueOrderByNombreAsc();

    /**
     * Buscar proveedor por nombre (exacto)
     */
    Optional<Proveedor> findByNombre(String nombre);

    /**
     * Buscar proveedores por nombre (parcial, case-insensitive)
     */
    List<Proveedor> findByNombreContainingIgnoreCaseOrderByNombreAsc(String nombre);

    /**
     * Verificar si existe un proveedor con ese nombre
     */
    boolean existsByNombre(String nombre);

    /**
     * Contar proveedores activos
     */
    long countByActivoTrue();
}
