package com.nexuspos.backend.service;

import com.nexuspos.backend.dto.CompraDTO;
import com.nexuspos.backend.dto.DetalleCompraDTO;
import com.nexuspos.backend.model.*;
import com.nexuspos.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SERVICE COMPRA
 *
 * Lógica de negocio para gestión de compras.
 * CORE LOGIC: Registro de compras con actualización automática de inventario.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CompraService {

    private final CompraRepository compraRepository;
    private final DetalleCompraRepository detalleCompraRepository;
    private final ProveedorRepository proveedorRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoInventarioService movimientoInventarioService;

    // =========================================================================
    // CORE BUSINESS LOGIC: REGISTRAR COMPRA
    // =========================================================================

    /**
     * Registra una nueva compra con actualización automática de inventario
     *
     * FLUJO TRANSACCIONAL:
     * 1. Validar proveedor existe y está activo
     * 2. Crear entidad Compra (master)
     * 3. Para cada detalle:
     *    a. Validar producto existe
     *    b. Crear DetalleCompra con snapshot (nombre, precio)
     *    c. Agregar stock al producto (Producto.agregarStock)
     *    d. Registrar movimiento de inventario (ENTRADA)
     * 4. Recalcular total de la compra
     * 5. Guardar todo (gracias a cascade)
     *
     * @param compraDTO DTO con datos de la compra
     * @return Compra registrada con todos los detalles
     */
    @Transactional
    public Compra registrarCompra(CompraDTO compraDTO) {
        log.info("Registrando nueva compra - Proveedor: {}, Documento: {}",
                 compraDTO.getProveedorId(), compraDTO.getNumeroDocumento());

        // 1. Validar y obtener proveedor
        Proveedor proveedor = proveedorRepository.findById(compraDTO.getProveedorId())
            .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));

        if (!proveedor.getActivo()) {
            throw new IllegalArgumentException("El proveedor está inactivo");
        }

        // 2. Crear Compra (master)
        Compra compra = new Compra();
        compra.setProveedor(proveedor);
        compra.setNumeroDocumento(compraDTO.getNumeroDocumento());
        compra.setFechaEntrega(compraDTO.getFechaEntrega());
        compra.setMetodoPago(compraDTO.getMetodoPago());
        compra.setObservaciones(compraDTO.getObservaciones());
        compra.setUsuario(compraDTO.getUsuario());

        // 3. Procesar detalles
        for (DetalleCompraDTO detalleDTO : compraDTO.getDetalles()) {
            // a. Validar producto
            Producto producto = productoRepository.findById(detalleDTO.getProductoId())
                .orElseThrow(() -> new IllegalArgumentException(
                    "Producto no encontrado con id: " + detalleDTO.getProductoId()));

            // b. Crear detalle con SNAPSHOT
            DetalleCompra detalle = new DetalleCompra();
            detalle.setProducto(producto);
            detalle.setNombreProducto(producto.getNombre()); // SNAPSHOT
            detalle.setCantidad(detalleDTO.getCantidad());
            detalle.setPrecioUnitario(detalleDTO.getPrecioUnitario()); // SNAPSHOT
            // subtotal se calcula automáticamente en @PrePersist

            // Agregar detalle a compra (establece relación bidireccional)
            compra.agregarDetalle(detalle);

            // c. Actualizar stock del producto
            log.debug("Agregando {} unidades de {} al inventario",
                     detalleDTO.getCantidad(), producto.getNombre());
            producto.agregarStock(detalleDTO.getCantidad());
            productoRepository.save(producto);

            // d. Registrar movimiento de inventario
            movimientoInventarioService.registrarEntrada(
                producto.getId(),
                detalleDTO.getCantidad(),
                detalleDTO.getPrecioUnitario(),
                "Compra #" + compraDTO.getNumeroDocumento() + " - " + proveedor.getNombre(),
                compraDTO.getUsuario()
            );

            log.info("Detalle procesado: {} x {} = ${}",
                    producto.getNombre(),
                    detalleDTO.getCantidad(),
                    detalle.getSubtotal());
        }

        // 4. Guardar compra (cascade guarda detalles automáticamente)
        Compra saved = compraRepository.save(compra);

        log.info("Compra registrada exitosamente - ID: {}, Total: ${}",
                saved.getId(), saved.getTotal());

        return saved;
    }

    // =========================================================================
    // CONSULTAS
    // =========================================================================

    @Transactional(readOnly = true)
    public List<Compra> findAll() {
        log.debug("Obteniendo todas las compras");
        return compraRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Optional<Compra> findById(Long id) {
        log.debug("Buscando compra con id: {}", id);
        return compraRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Compra> findByProveedor(Long proveedorId) {
        Proveedor proveedor = proveedorRepository.findById(proveedorId)
            .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));
        return compraRepository.findByProveedorOrderByCreatedAtDesc(proveedor);
    }

    @Transactional(readOnly = true)
    public List<Compra> findByRangoFechas(LocalDate inicio, LocalDate fin) {
        return compraRepository.findByFechaEntregaBetweenOrderByFechaEntregaDesc(inicio, fin);
    }

    @Transactional(readOnly = true)
    public List<Compra> findByMetodoPago(String metodoPago) {
        return compraRepository.findByMetodoPagoOrderByCreatedAtDesc(metodoPago);
    }

    @Transactional(readOnly = true)
    public List<Compra> findUltimas() {
        return compraRepository.findTop10ByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<Compra> searchByDocumento(String numeroDocumento) {
        return compraRepository.findByNumeroDocumentoContainingIgnoreCase(numeroDocumento);
    }

    // =========================================================================
    // ESTADÍSTICAS
    // =========================================================================

    @Transactional(readOnly = true)
    public Double calcularTotalCompras(LocalDateTime inicio, LocalDateTime fin) {
        Double total = compraRepository.calcularTotalCompras(inicio, fin);
        return total != null ? total : 0.0;
    }

    @Transactional(readOnly = true)
    public long count() {
        return compraRepository.count();
    }

    @Transactional(readOnly = true)
    public long countByMetodoPago(String metodoPago) {
        return compraRepository.countByMetodoPago(metodoPago);
    }
}
