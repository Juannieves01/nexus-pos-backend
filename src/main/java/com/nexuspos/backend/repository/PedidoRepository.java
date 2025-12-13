package com.nexuspos.backend.repository;

import com.nexuspos.backend.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * REPOSITORY DE PEDIDO
 */
@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // Buscar todos los pedidos de una mesa
    List<Pedido> findByMesaId(Long mesaId);

    // Buscar pedidos de un producto específico
    List<Pedido> findByProductoId(Long productoId);

    // Contar pedidos de una mesa
    Long countByMesaId(Long mesaId);

    // Buscar pedidos de una mesa ordenados por fecha
    List<Pedido> findByMesaIdOrderByCreatedAtAsc(Long mesaId);

    // Calcular total de una mesa
    @Query("SELECT SUM(p.subtotal) FROM Pedido p WHERE p.mesa.id = :mesaId")
    Double calcularTotalMesa(@Param("mesaId") Long mesaId);
}
