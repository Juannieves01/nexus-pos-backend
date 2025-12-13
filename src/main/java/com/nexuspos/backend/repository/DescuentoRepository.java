package com.nexuspos.backend.repository;

import com.nexuspos.backend.model.Descuento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DescuentoRepository extends JpaRepository<Descuento, Long> {

    // Buscar descuentos activos
    List<Descuento> findByActivo(Boolean activo);

    // Buscar por nombre (contiene, ignorando mayúsculas/minúsculas)
    List<Descuento> findByNombreContainingIgnoreCase(String nombre);

    // Obtener descuentos activos y válidos actualmente
    @Query("SELECT d FROM Descuento d WHERE d.activo = true AND " +
           "(d.fechaInicio IS NULL OR d.fechaInicio <= CURRENT_TIMESTAMP) AND " +
           "(d.fechaFin IS NULL OR d.fechaFin >= CURRENT_TIMESTAMP)")
    List<Descuento> findDescuentosValidos();
}
