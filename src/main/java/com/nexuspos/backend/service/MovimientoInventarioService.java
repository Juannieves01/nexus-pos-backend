package com.nexuspos.backend.service;

import com.nexuspos.backend.model.MovimientoInventario;
import com.nexuspos.backend.model.Producto;
import com.nexuspos.backend.model.TipoMovimiento;
import com.nexuspos.backend.repository.MovimientoInventarioRepository;
import com.nexuspos.backend.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SERVICIO MOVIMIENTO INVENTARIO
 *
 * Lógica de negocio para gestión de movimientos de inventario
 */
@Service
public class MovimientoInventarioService {

    @Autowired
    private MovimientoInventarioRepository movimientoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    /**
     * Obtener todos los movimientos
     */
    public List<MovimientoInventario> findAll() {
        return movimientoRepository.findAll();
    }

    /**
     * Obtener últimos 10 movimientos
     */
    public List<MovimientoInventario> getUltimosMovimientos() {
        return movimientoRepository.findTop10ByOrderByFechaDesc();
    }

    /**
     * Buscar movimientos por producto
     */
    public List<MovimientoInventario> findByProducto(Long productoId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        return movimientoRepository.findByProductoOrderByFechaDesc(producto);
    }

    /**
     * Buscar movimientos por tipo
     */
    public List<MovimientoInventario> findByTipo(TipoMovimiento tipo) {
        return movimientoRepository.findByTipoOrderByFechaDesc(tipo);
    }

    /**
     * Buscar movimientos por rango de fechas
     */
    public List<MovimientoInventario> findByRango(LocalDateTime inicio, LocalDateTime fin) {
        return movimientoRepository.findByFechaBetweenOrderByFechaDesc(inicio, fin);
    }

    /**
     * Registrar una ENTRADA de inventario
     */
    @Transactional
    public MovimientoInventario registrarEntrada(
            Long productoId,
            Integer cantidad,
            Double costoUnitario,
            String motivo,
            String usuario
    ) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser positiva");
        }

        // Guardar stock anterior
        Integer stockAnterior = producto.getStock();

        // Actualizar stock del producto
        producto.agregarStock(cantidad);
        productoRepository.save(producto);

        // Crear el movimiento
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProducto(producto);
        movimiento.setTipo(TipoMovimiento.ENTRADA);
        movimiento.setCantidad(cantidad);
        movimiento.setStockAnterior(stockAnterior);
        movimiento.setStockNuevo(producto.getStock());
        movimiento.setCostoUnitario(costoUnitario);
        movimiento.setCostoTotal(costoUnitario != null ? costoUnitario * cantidad : null);
        movimiento.setMotivo(motivo);
        movimiento.setUsuario(usuario);

        return movimientoRepository.save(movimiento);
    }

    /**
     * Registrar una SALIDA de inventario
     */
    @Transactional
    public MovimientoInventario registrarSalida(
            Long productoId,
            Integer cantidad,
            String motivo,
            String usuario
    ) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser positiva");
        }

        // Guardar stock anterior
        Integer stockAnterior = producto.getStock();

        // Reducir stock del producto (lanza excepción si no hay suficiente)
        producto.reducirStock(cantidad);
        productoRepository.save(producto);

        // Crear el movimiento
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProducto(producto);
        movimiento.setTipo(TipoMovimiento.SALIDA);
        movimiento.setCantidad(cantidad);
        movimiento.setStockAnterior(stockAnterior);
        movimiento.setStockNuevo(producto.getStock());
        movimiento.setMotivo(motivo);
        movimiento.setUsuario(usuario);

        return movimientoRepository.save(movimiento);
    }

    /**
     * Registrar un AJUSTE de inventario
     */
    @Transactional
    public MovimientoInventario registrarAjuste(
            Long productoId,
            Integer cantidad,
            boolean esPositivo,
            String motivo,
            String usuario
    ) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser positiva");
        }

        // Guardar stock anterior
        Integer stockAnterior = producto.getStock();

        // Ajustar stock
        if (esPositivo) {
            producto.agregarStock(cantidad);
        } else {
            producto.reducirStock(cantidad);
        }
        productoRepository.save(producto);

        // Crear el movimiento
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProducto(producto);
        movimiento.setTipo(esPositivo ? TipoMovimiento.AJUSTE_POSITIVO : TipoMovimiento.AJUSTE_NEGATIVO);
        movimiento.setCantidad(cantidad);
        movimiento.setStockAnterior(stockAnterior);
        movimiento.setStockNuevo(producto.getStock());
        movimiento.setMotivo(motivo);
        movimiento.setUsuario(usuario);

        return movimientoRepository.save(movimiento);
    }

    /**
     * Calcular total de entradas en un periodo
     */
    public Double calcularTotalEntradas(LocalDateTime inicio, LocalDateTime fin) {
        Double total = movimientoRepository.calcularTotalEntradas(inicio, fin);
        return total != null ? total : 0.0;
    }

    /**
     * Obtener estadísticas de movimientos
     */
    public long contarPorTipo(TipoMovimiento tipo) {
        return movimientoRepository.countByTipo(tipo);
    }
}
