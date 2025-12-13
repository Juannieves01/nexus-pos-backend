package com.nexuspos.backend.service;

import com.nexuspos.backend.model.Pedido;
import com.nexuspos.backend.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * SERVICE DE PEDIDO
 *
 * Maneja operaciones básicas de pedidos.
 * La mayor parte de la lógica está en MesaService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PedidoService {

    private final PedidoRepository pedidoRepository;

    @Transactional(readOnly = true)
    public List<Pedido> findAll() {
        return pedidoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Pedido> findById(Long id) {
        return pedidoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Pedido> findByMesaId(Long mesaId) {
        return pedidoRepository.findByMesaIdOrderByCreatedAtAsc(mesaId);
    }

    @Transactional(readOnly = true)
    public Long countByMesaId(Long mesaId) {
        return pedidoRepository.countByMesaId(mesaId);
    }

    @Transactional(readOnly = true)
    public Double calcularTotalMesa(Long mesaId) {
        Double total = pedidoRepository.calcularTotalMesa(mesaId);
        return total != null ? total : 0.0;
    }
}
