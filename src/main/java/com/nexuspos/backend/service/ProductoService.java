package com.nexuspos.backend.service;

import com.nexuspos.backend.model.Producto;
import com.nexuspos.backend.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * SERVICE DE PRODUCTO
 *
 * ¿Por qué necesitamos un Service si ya tenemos Repository?
 * ------------------------------------------------------
 *
 * SEPARACIÓN DE RESPONSABILIDADES:
 *
 * - Repository: Solo acceso a datos (CRUD básico)
 *   → "Tráeme todos los productos"
 *   → "Guarda este producto"
 *
 * - Service: Lógica de negocio compleja
 *   → "Crea un producto, pero primero valida que no exista"
 *   → "Reduce el stock, pero verifica que haya suficiente"
 *   → "Elimina el producto solo si no está en pedidos activos"
 *
 * VENTAJAS:
 * 1. Controllers delgados (solo reciben/envían datos)
 * 2. Lógica reutilizable (varios controllers pueden usar el mismo service)
 * 3. Fácil de testear (mock del repository)
 * 4. Transacciones centralizadas
 *
 * ANOTACIONES:
 *
 * @Service - Marca esta clase como un componente de servicio
 *           Spring lo registra automáticamente en el contenedor IoC
 *
 * @RequiredArgsConstructor (Lombok) - Genera constructor con campos final
 *           Inyección de dependencias por constructor (mejor práctica)
 *
 * @Slf4j (Lombok) - Genera automáticamente un logger
 *           Puedes usar: log.info(), log.error(), log.debug()
 *
 * @Transactional - Manejo automático de transacciones de base de datos
 *                  Si algo falla, todo se revierte (ROLLBACK)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductoService {

    /**
     * INYECCIÓN DE DEPENDENCIAS
     *
     * ¿Qué significa 'final'?
     * - Se inicializa una vez (en el constructor)
     * - No se puede cambiar después
     * - Inmutable = más seguro
     *
     * ¿Cómo funciona la inyección?
     * 1. Spring crea ProductoRepository automáticamente
     * 2. @RequiredArgsConstructor genera: ProductoService(ProductoRepository repo)
     * 3. Spring llama al constructor y pasa el repository
     * 4. ¡Magia! No usas 'new' nunca
     *
     * ANTES (sin Spring):
     * ProductoRepository repo = new ProductoRepositoryImpl();
     *
     * AHORA (con Spring):
     * ProductoService service = ... Spring lo inyecta automáticamente
     */
    private final ProductoRepository productoRepository;

    // =========================================================================
    // MÉTODOS CRUD BÁSICOS
    // =========================================================================

    /**
     * Obtiene todos los productos
     *
     * @Transactional(readOnly = true)
     * - readOnly = true: Optimización para consultas (no escribe)
     * - Mejor rendimiento
     * - PostgreSQL puede optimizar queries de solo lectura
     *
     * log.debug():
     * - Solo se muestra si logging.level está en DEBUG
     * - Útil para desarrollo, no molesta en producción
     *
     * @return lista de todos los productos
     */
    @Transactional(readOnly = true)
    public List<Producto> findAll() {
        log.debug("Obteniendo todos los productos");
        return productoRepository.findAll();
    }

    /**
     * Busca un producto por ID
     *
     * @param id identificador del producto
     * @return Optional con el producto o vacío si no existe
     */
    @Transactional(readOnly = true)
    public Optional<Producto> findById(Long id) {
        log.debug("Buscando producto con id: {}", id);
        return productoRepository.findById(id);
    }

    /**
     * Crea un nuevo producto
     *
     * LÓGICA DE NEGOCIO:
     * 1. Verifica que no exista un producto con el mismo nombre
     * 2. Si existe, lanza excepción
     * 3. Si no existe, lo guarda
     *
     * @Transactional (sin readOnly)
     * - Abre una transacción de escritura
     * - Si algo falla, hace ROLLBACK automático
     * - Si termina bien, hace COMMIT
     *
     * @param producto producto a crear
     * @return producto guardado con ID generado
     * @throws IllegalArgumentException si ya existe un producto con ese nombre
     */
    @Transactional
    public Producto create(Producto producto) {
        log.info("Creando nuevo producto: {}", producto.getNombre());

        // Validación de negocio: nombre único
        if (productoRepository.existsByNombre(producto.getNombre())) {
            log.error("Ya existe un producto con el nombre: {}", producto.getNombre());
            throw new IllegalArgumentException(
                "Ya existe un producto con el nombre: " + producto.getNombre()
            );
        }

        // Guardar y retornar
        Producto savedProducto = productoRepository.save(producto);
        log.info("Producto creado exitosamente con id: {}", savedProducto.getId());

        return savedProducto;
    }

    /**
     * Actualiza un producto existente
     *
     * LÓGICA DE NEGOCIO:
     * 1. Verifica que el producto exista
     * 2. Si se cambió el nombre, verifica que no esté duplicado
     * 3. Actualiza los campos
     * 4. Guarda
     *
     * @param id identificador del producto a actualizar
     * @param productoActualizado datos nuevos
     * @return producto actualizado
     * @throws IllegalArgumentException si el producto no existe
     */
    @Transactional
    public Producto update(Long id, Producto productoActualizado) {
        log.info("Actualizando producto con id: {}", id);

        // Buscar producto existente
        Producto productoExistente = productoRepository.findById(id)
            .orElseThrow(() -> {
                log.error("Producto no encontrado con id: {}", id);
                return new IllegalArgumentException("Producto no encontrado con id: " + id);
            });

        // Validar nombre único (si cambió)
        if (!productoExistente.getNombre().equals(productoActualizado.getNombre())) {
            if (productoRepository.existsByNombre(productoActualizado.getNombre())) {
                log.error("Ya existe otro producto con el nombre: {}", productoActualizado.getNombre());
                throw new IllegalArgumentException(
                    "Ya existe otro producto con el nombre: " + productoActualizado.getNombre()
                );
            }
        }

        // Actualizar campos
        productoExistente.setNombre(productoActualizado.getNombre());
        productoExistente.setCategoria(productoActualizado.getCategoria());
        productoExistente.setPrecio(productoActualizado.getPrecio());
        productoExistente.setStock(productoActualizado.getStock());

        // Guardar (save hace UPDATE porque ya tiene ID)
        Producto updated = productoRepository.save(productoExistente);
        log.info("Producto actualizado exitosamente: {}", updated.getId());

        return updated;
    }

    /**
     * Elimina un producto
     *
     * LÓGICA DE NEGOCIO:
     * - Solo elimina si existe
     * - En un sistema real, verificarías que no esté en pedidos activos
     *
     * @param id identificador del producto a eliminar
     * @throws IllegalArgumentException si el producto no existe
     */
    @Transactional
    public void delete(Long id) {
        log.info("Eliminando producto con id: {}", id);

        if (!productoRepository.existsById(id)) {
            log.error("No se puede eliminar. Producto no encontrado con id: {}", id);
            throw new IllegalArgumentException("Producto no encontrado con id: " + id);
        }

        productoRepository.deleteById(id);
        log.info("Producto eliminado exitosamente con id: {}", id);
    }

    // =========================================================================
    // MÉTODOS DE NEGOCIO ESPECÍFICOS
    // =========================================================================

    /**
     * Busca productos por categoría
     *
     * @param categoria categoría a buscar
     * @return lista de productos en esa categoría
     */
    @Transactional(readOnly = true)
    public List<Producto> findByCategoria(String categoria) {
        log.debug("Buscando productos en categoría: {}", categoria);
        return productoRepository.findByCategoria(categoria);
    }

    /**
     * Busca productos por nombre (búsqueda parcial, case-insensitive)
     *
     * @param nombre texto a buscar
     * @return productos que contienen ese texto
     */
    @Transactional(readOnly = true)
    public List<Producto> searchByNombre(String nombre) {
        log.debug("Buscando productos con nombre que contenga: {}", nombre);
        return productoRepository.findByNombreContainingIgnoreCase(nombre);
    }

    /**
     * Obtiene productos con stock bajo (menos de 20 unidades)
     *
     * @return productos con stock bajo
     */
    @Transactional(readOnly = true)
    public List<Producto> findStockBajo() {
        log.debug("Obteniendo productos con stock bajo");
        return productoRepository.findByStockLessThan(20);
    }

    /**
     * Obtiene productos con stock crítico (menos de 10 unidades)
     *
     * @return productos con stock crítico
     */
    @Transactional(readOnly = true)
    public List<Producto> findStockCritico() {
        log.debug("Obteniendo productos con stock crítico");
        return productoRepository.findProductosStockCritico();
    }

    /**
     * Obtiene todas las categorías disponibles
     *
     * @return lista de categorías únicas
     */
    @Transactional(readOnly = true)
    public List<String> findAllCategorias() {
        log.debug("Obteniendo todas las categorías");
        return productoRepository.findAllCategorias();
    }

    /**
     * Agrega stock a un producto
     *
     * LÓGICA DE NEGOCIO:
     * - Busca el producto
     * - Usa el método agregarStock() de la entidad
     * - Guarda
     *
     * @param id identificador del producto
     * @param cantidad cantidad a agregar
     * @return producto actualizado
     */
    @Transactional
    public Producto agregarStock(Long id, Integer cantidad) {
        log.info("Agregando {} unidades de stock al producto id: {}", cantidad, id);

        Producto producto = productoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        producto.agregarStock(cantidad);
        Producto updated = productoRepository.save(producto);

        log.info("Stock actualizado. Nuevo stock: {}", updated.getStock());
        return updated;
    }

    /**
     * Reduce stock de un producto (usado al hacer ventas)
     *
     * LÓGICA DE NEGOCIO:
     * - Verifica que haya suficiente stock
     * - Reduce el stock
     * - Guarda
     *
     * @param id identificador del producto
     * @param cantidad cantidad a reducir
     * @return producto actualizado
     * @throws IllegalArgumentException si no hay suficiente stock
     */
    @Transactional
    public Producto reducirStock(Long id, Integer cantidad) {
        log.info("Reduciendo {} unidades de stock al producto id: {}", cantidad, id);

        Producto producto = productoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        // Este método de la entidad lanza excepción si no hay suficiente stock
        producto.reducirStock(cantidad);
        Producto updated = productoRepository.save(producto);

        log.info("Stock actualizado. Nuevo stock: {}", updated.getStock());
        return updated;
    }

    /**
     * Cuenta total de productos
     *
     * @return cantidad total de productos
     */
    @Transactional(readOnly = true)
    public long count() {
        return productoRepository.count();
    }
}
