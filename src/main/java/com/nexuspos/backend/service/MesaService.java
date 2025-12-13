package com.nexuspos.backend.service;

import com.nexuspos.backend.model.Mesa;
import com.nexuspos.backend.model.Pedido;
import com.nexuspos.backend.model.Producto;
import com.nexuspos.backend.repository.MesaRepository;
import com.nexuspos.backend.repository.PedidoRepository;
import com.nexuspos.backend.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * SERVICE DE MESA
 *
 * Maneja toda la lógica de negocio relacionada con mesas:
 * - CRUD básico
 * - Agregar/quitar pedidos
 * - Cambiar estado (ocupar/liberar)
 * - Calcular totales
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MesaService {

    private final MesaRepository mesaRepository;
    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;

    // =========================================================================
    // CRUD BÁSICO
    // =========================================================================

    @Transactional(readOnly = true)
    public List<Mesa> findAll() {
        log.debug("Obteniendo todas las mesas");
        return mesaRepository.findAllByOrderByNumeroAsc();
    }

    @Transactional(readOnly = true)
    public Optional<Mesa> findById(Long id) {
        log.debug("Buscando mesa con id: {}", id);
        return mesaRepository.findById(id);
    }

    @Transactional
    public Mesa create(Mesa mesa) {
        log.info("Creando nueva mesa: {}", mesa.getNombre());

        // Validar que no exista una mesa con el mismo número
        if (mesaRepository.existsByNumero(mesa.getNumero())) {
            throw new IllegalArgumentException("Ya existe una mesa con el número: " + mesa.getNumero());
        }

        // Establecer estado inicial
        if (mesa.getEstado() == null || mesa.getEstado().isBlank()) {
            mesa.setEstado("libre");
        }

        // Establecer total inicial
        if (mesa.getTotal() == null) {
            mesa.setTotal(0.0);
        }

        Mesa savedMesa = mesaRepository.save(mesa);
        log.info("Mesa creada exitosamente con id: {}", savedMesa.getId());

        return savedMesa;
    }

    @Transactional
    public Mesa update(Long id, Mesa mesaActualizada) {
        log.info("Actualizando mesa con id: {}", id);

        Mesa mesaExistente = mesaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Mesa no encontrada con id: " + id));

        // Validar número único (si cambió)
        if (!mesaExistente.getNumero().equals(mesaActualizada.getNumero())) {
            if (mesaRepository.existsByNumero(mesaActualizada.getNumero())) {
                throw new IllegalArgumentException("Ya existe otra mesa con el número: " + mesaActualizada.getNumero());
            }
        }

        // Actualizar campos
        mesaExistente.setNumero(mesaActualizada.getNumero());
        mesaExistente.setNombre(mesaActualizada.getNombre());
        // NO actualizamos estado ni total aquí (se manejan con métodos específicos)

        Mesa updated = mesaRepository.save(mesaExistente);
        log.info("Mesa actualizada exitosamente");

        return updated;
    }

    @Transactional
    public void delete(Long id) {
        log.info("Eliminando mesa con id: {}", id);

        Mesa mesa = mesaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Mesa no encontrada con id: " + id));

        // Validar que no esté ocupada
        if (mesa.isOcupada()) {
            throw new IllegalArgumentException("No se puede eliminar una mesa ocupada");
        }

        mesaRepository.deleteById(id);
        log.info("Mesa eliminada exitosamente");
    }

    // =========================================================================
    // OPERACIONES DE ESTADO
    // =========================================================================

    @Transactional
    public Mesa ocupar(Long id) {
        log.info("Ocupando mesa con id: {}", id);

        Mesa mesa = mesaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Mesa no encontrada"));

        mesa.ocupar();
        return mesaRepository.save(mesa);
    }

    @Transactional
    public Mesa liberar(Long id) {
        log.info("Liberando mesa con id: {}", id);

        Mesa mesa = mesaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Mesa no encontrada"));

        // Limpiar pedidos antes de liberar
        mesa.limpiarPedidos();
        return mesaRepository.save(mesa);
    }

    // =========================================================================
    // OPERACIONES DE PEDIDOS
    // =========================================================================

    /**
     * Agrega un pedido a la mesa
     *
     * LÓGICA DE NEGOCIO:
     * 1. Verifica que la mesa exista
     * 2. Verifica que el producto exista
     * 3. Crea el pedido con precio actual del producto
     * 4. Agrega el pedido a la mesa
     * 5. Marca la mesa como ocupada
     * 6. Reduce el stock del producto
     * 7. Guarda todo
     *
     * @param mesaId ID de la mesa
     * @param productoId ID del producto
     * @param cantidad Cantidad de productos
     * @return Mesa actualizada con el nuevo pedido
     */
    @Transactional
    public Mesa agregarPedido(Long mesaId, Long productoId, Integer cantidad) {
        log.info("Agregando pedido a mesa {}: {} x{}", mesaId, productoId, cantidad);

        // Buscar mesa
        Mesa mesa = mesaRepository.findById(mesaId)
            .orElseThrow(() -> new IllegalArgumentException("Mesa no encontrada"));

        // Buscar producto
        Producto producto = productoRepository.findById(productoId)
            .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        // Validar stock
        if (producto.getStock() < cantidad) {
            throw new IllegalArgumentException(
                String.format("Stock insuficiente. Disponible: %d, Solicitado: %d",
                    producto.getStock(), cantidad)
            );
        }

        // Crear pedido
        Pedido pedido = new Pedido();
        pedido.setProducto(producto);
        pedido.setNombreProducto(producto.getNombre()); // Snapshot del nombre
        pedido.setCantidad(cantidad);
        pedido.setPrecioUnitario(producto.getPrecio()); // Snapshot del precio

        // IMPORTANTE: Calcular subtotal ANTES de agregar a la mesa
        // porque recalcularTotal() se ejecuta antes del @PrePersist
        pedido.setSubtotal(cantidad * producto.getPrecio());

        // Agregar pedido a la mesa
        mesa.agregarPedido(pedido);

        // Reducir stock del producto
        producto.reducirStock(cantidad);
        productoRepository.save(producto);

        // Guardar todo (gracias a cascade)
        Mesa savedMesa = mesaRepository.save(mesa);

        log.info("Pedido agregado exitosamente. Total mesa: {}", savedMesa.getTotal());

        return savedMesa;
    }

    /**
     * Quita un pedido de la mesa
     * Devuelve el stock al producto
     */
    @Transactional
    public Mesa quitarPedido(Long mesaId, Long pedidoId) {
        log.info("Quitando pedido {} de mesa {}", pedidoId, mesaId);

        Mesa mesa = mesaRepository.findById(mesaId)
            .orElseThrow(() -> new IllegalArgumentException("Mesa no encontrada"));

        Pedido pedido = pedidoRepository.findById(pedidoId)
            .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        // Devolver stock al producto
        Producto producto = pedido.getProducto();
        producto.agregarStock(pedido.getCantidad());
        productoRepository.save(producto);

        // Quitar pedido de la mesa
        mesa.quitarPedido(pedido);

        // Guardar
        Mesa savedMesa = mesaRepository.save(mesa);

        log.info("Pedido quitado. Total mesa: {}", savedMesa.getTotal());

        return savedMesa;
    }

    /**
     * Actualiza la cantidad de un pedido
     */
    @Transactional
    public Mesa actualizarCantidadPedido(Long mesaId, Long pedidoId, Integer nuevaCantidad) {
        log.info("Actualizando cantidad de pedido {} a {}", pedidoId, nuevaCantidad);

        Mesa mesa = mesaRepository.findById(mesaId)
            .orElseThrow(() -> new IllegalArgumentException("Mesa no encontrada"));

        Pedido pedido = pedidoRepository.findById(pedidoId)
            .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        Producto producto = pedido.getProducto();

        // Calcular diferencia de stock
        int diferenciaStock = nuevaCantidad - pedido.getCantidad();

        if (diferenciaStock > 0) {
            // Aumentó cantidad → reducir stock
            if (producto.getStock() < diferenciaStock) {
                throw new IllegalArgumentException("Stock insuficiente");
            }
            producto.reducirStock(diferenciaStock);
        } else if (diferenciaStock < 0) {
            // Disminuyó cantidad → devolver stock
            producto.agregarStock(Math.abs(diferenciaStock));
        }

        // Actualizar pedido
        pedido.actualizarCantidad(nuevaCantidad);
        pedidoRepository.save(pedido);
        productoRepository.save(producto);

        // Recalcular total de la mesa
        mesa.recalcularTotal();
        Mesa savedMesa = mesaRepository.save(mesa);

        log.info("Cantidad actualizada. Total mesa: {}", savedMesa.getTotal());

        return savedMesa;
    }

    // =========================================================================
    // CONSULTAS ESPECÍFICAS
    // =========================================================================

    @Transactional(readOnly = true)
    public List<Mesa> findMesasOcupadas() {
        return mesaRepository.findMesasOcupadas();
    }

    @Transactional(readOnly = true)
    public List<Mesa> findMesasLibres() {
        return mesaRepository.findMesasLibres();
    }

    @Transactional(readOnly = true)
    public long countByEstado(String estado) {
        return mesaRepository.countByEstado(estado);
    }

    @Transactional(readOnly = true)
    public long count() {
        return mesaRepository.count();
    }
}
