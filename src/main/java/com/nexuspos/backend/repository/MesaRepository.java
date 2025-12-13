package com.nexuspos.backend.repository;

import com.nexuspos.backend.model.Mesa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * REPOSITORY DE MESA
 */
@Repository
public interface MesaRepository extends JpaRepository<Mesa, Long> {

    // Buscar mesa por número
    Optional<Mesa> findByNumero(Integer numero);

    // Verificar si existe una mesa con ese número
    boolean existsByNumero(Integer numero);

    // Buscar mesas por estado
    List<Mesa> findByEstado(String estado);

    // Buscar mesas ocupadas
    @Query("SELECT m FROM Mesa m WHERE LOWER(m.estado) = 'ocupada'")
    List<Mesa> findMesasOcupadas();

    // Buscar mesas libres
    @Query("SELECT m FROM Mesa m WHERE LOWER(m.estado) = 'libre' ORDER BY m.numero ASC")
    List<Mesa> findMesasLibres();

    // Contar mesas por estado
    Long countByEstado(String estado);

    // Buscar mesas ordenadas por número
    List<Mesa> findAllByOrderByNumeroAsc();
}
