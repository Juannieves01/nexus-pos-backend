package com.nexuspos.backend.repository;

import com.nexuspos.backend.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * REPOSITORY DE PRODUCTO
 *
 * ¿Qué hace esta interfaz?
 * Proporciona métodos para interactuar con la tabla 'productos'.
 *
 * ¿Por qué es una INTERFAZ y no una CLASE?
 * Spring Data JPA genera automáticamente la implementación en runtime.
 * TÚ NO ESCRIBES EL CÓDIGO, Spring lo hace por ti.
 *
 * HERENCIA DE JpaRepository<Producto, Long>:
 * ------------------------------------------
 * - Producto: La entidad que manejamos
 * - Long: El tipo de dato del ID
 *
 * Al heredar de JpaRepository, obtienes GRATIS estos métodos:
 *
 * - save(producto)           → INSERT o UPDATE
 * - findById(id)             → SELECT * FROM productos WHERE id = ?
 * - findAll()                → SELECT * FROM productos
 * - deleteById(id)           → DELETE FROM productos WHERE id = ?
 * - count()                  → SELECT COUNT(*) FROM productos
 * - existsById(id)           → Verifica si existe
 * - Y muchos más...
 *
 * ¡TODO ESTO SIN ESCRIBIR UNA LÍNEA DE SQL!
 *
 * @Repository - Marca esta interfaz como un componente de Spring
 *               (aunque es opcional con JpaRepository)
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // =========================================================================
    // QUERY METHODS - Métodos que Spring convierte a SQL automáticamente
    // =========================================================================

    /**
     * Busca productos por categoría
     *
     * NAMING CONVENTION de Spring Data JPA:
     * - findBy + NombreCampo
     *
     * Spring traduce esto automáticamente a:
     * SELECT * FROM productos WHERE categoria = ?
     *
     * ¿Cómo sabe qué hacer?
     * 1. Lee el nombre del método: "findByCategoria"
     * 2. Extrae el campo: "Categoria"
     * 3. Busca el campo en la entidad Producto
     * 4. Genera el SQL correspondiente
     *
     * @param categoria nombre de la categoría
     * @return lista de productos en esa categoría
     */
    List<Producto> findByCategoria(String categoria);

    /**
     * Busca productos cuyo nombre contenga un texto (case-insensitive)
     *
     * Containing + IgnoreCase:
     * SELECT * FROM productos WHERE LOWER(nombre) LIKE LOWER('%?%')
     *
     * Ejemplo de uso:
     * findByNombreContainingIgnoreCase("coca")
     * → Encuentra: "Coca Cola", "coca cola", "COCA ZERO"
     *
     * @param nombre texto a buscar
     * @return productos que contienen ese texto en el nombre
     */
    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    /**
     * Busca productos con stock menor a un valor
     *
     * LessThan:
     * SELECT * FROM productos WHERE stock < ?
     *
     * Uso: findByStockLessThan(20) → Productos con stock bajo
     *
     * @param cantidad umbral de stock
     * @return productos con stock menor al especificado
     */
    List<Producto> findByStockLessThan(Integer cantidad);

    /**
     * Busca productos por categoría y stock bajo
     *
     * COMBINACIÓN de criterios con AND:
     * SELECT * FROM productos WHERE categoria = ? AND stock < ?
     *
     * También puedes usar OR:
     * findByCategoriaOrStockLessThan(...)
     *
     * @param categoria categoría del producto
     * @param stock umbral de stock
     * @return productos que cumplen ambas condiciones
     */
    List<Producto> findByCategoriaAndStockLessThan(String categoria, Integer stock);

    /**
     * Busca productos con precio entre dos valores
     *
     * Between:
     * SELECT * FROM productos WHERE precio BETWEEN ? AND ?
     *
     * @param min precio mínimo
     * @param max precio máximo
     * @return productos en ese rango de precio
     */
    List<Producto> findByPrecioBetween(Double min, Double max);

    /**
     * Busca un producto por nombre exacto
     *
     * Optional<T>:
     * - Si encuentra el producto → Optional.of(producto)
     * - Si no encuentra → Optional.empty()
     *
     * ¿Por qué Optional y no null?
     * - Evita NullPointerException
     * - Obliga a manejar el caso "no encontrado"
     * - Código más seguro y legible
     *
     * Uso:
     * Optional<Producto> opt = repository.findByNombre("Coca Cola");
     * if (opt.isPresent()) {
     *     Producto p = opt.get();
     *     // usar producto
     * } else {
     *     // manejar no encontrado
     * }
     *
     * @param nombre nombre exacto del producto
     * @return Optional con el producto o vacío
     */
    Optional<Producto> findByNombre(String nombre);

    // =========================================================================
    // CUSTOM QUERIES - Consultas SQL/JPQL personalizadas
    // =========================================================================

    /**
     * Obtiene todas las categorías únicas
     *
     * @Query - Permite escribir JPQL (similar a SQL pero orientado a objetos)
     *
     * JPQL vs SQL:
     * - JPQL: SELECT DISTINCT p.categoria FROM Producto p
     * - SQL:  SELECT DISTINCT categoria FROM productos
     *
     * Diferencias:
     * - JPQL usa nombres de CLASES y ATRIBUTOS (Producto, categoria)
     * - SQL usa nombres de TABLAS y COLUMNAS (productos, categoria)
     *
     * @return lista de categorías sin duplicados
     */
    @Query("SELECT DISTINCT p.categoria FROM Producto p WHERE p.categoria IS NOT NULL ORDER BY p.categoria")
    List<String> findAllCategorias();

    /**
     * Cuenta productos por categoría
     *
     * SQL Nativo con @Query(nativeQuery = true):
     * Escribes SQL puro de PostgreSQL
     *
     * ¿Cuándo usar SQL nativo?
     * - Queries muy complejas
     * - Funciones específicas de PostgreSQL
     * - Optimizaciones avanzadas
     *
     * :categoria es un parámetro nombrado (se reemplaza con el valor)
     *
     * @param categoria categoría a contar
     * @return cantidad de productos en esa categoría
     */
    @Query(value = "SELECT COUNT(*) FROM productos WHERE categoria = :categoria", nativeQuery = true)
    Long countByCategoria(@Param("categoria") String categoria);

    /**
     * Busca productos con stock crítico (menos de 10)
     *
     * Combina JPQL con ordenamiento
     *
     * ORDER BY p.stock ASC:
     * - ASC: ascendente (menor a mayor)
     * - DESC: descendente (mayor a menor)
     *
     * @return productos con stock crítico, ordenados por stock
     */
    @Query("SELECT p FROM Producto p WHERE p.stock < 10 ORDER BY p.stock ASC")
    List<Producto> findProductosStockCritico();

    // =========================================================================
    // MÁS EJEMPLOS DE NAMING CONVENTIONS
    // =========================================================================

    // Buscar por múltiples categorías
    // SELECT * FROM productos WHERE categoria IN (?, ?, ?)
    List<Producto> findByCategoriaIn(List<String> categorias);

    // Ordenar por precio ascendente
    // SELECT * FROM productos ORDER BY precio ASC
    List<Producto> findAllByOrderByPrecioAsc();

    // Ordenar por stock descendente
    // SELECT * FROM productos ORDER BY stock DESC
    List<Producto> findAllByOrderByStockDesc();

    // Buscar por categoría y ordenar por nombre
    // SELECT * FROM productos WHERE categoria = ? ORDER BY nombre ASC
    List<Producto> findByCategoriaOrderByNombreAsc(String categoria);

    // Verificar si existe un producto con ese nombre
    // SELECT EXISTS(SELECT 1 FROM productos WHERE nombre = ?)
    boolean existsByNombre(String nombre);

    // Contar productos con stock mayor a X
    // SELECT COUNT(*) FROM productos WHERE stock > ?
    Long countByStockGreaterThan(Integer stock);
}
