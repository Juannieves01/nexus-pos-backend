package com.nexuspos.backend.controller;

import com.nexuspos.backend.model.Producto;
import com.nexuspos.backend.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * CONTROLLER DE PRODUCTO - API REST
 *
 * ¿Qué es un Controller REST?
 * ---------------------------
 * Clase que expone endpoints HTTP para que el frontend pueda interactuar.
 *
 * EJEMPLO DE FLUJO COMPLETO:
 * 1. Frontend hace: GET http://localhost:8080/api/productos
 * 2. Este controller recibe la petición
 * 3. Llama al ProductoService
 * 4. ProductoService usa ProductoRepository
 * 5. Repository consulta PostgreSQL
 * 6. Datos suben por las capas hasta el controller
 * 7. Controller devuelve JSON al frontend
 *
 * ANOTACIONES:
 *
 * @RestController - Combinación de @Controller + @ResponseBody
 *                   Todas las respuestas se convierten automáticamente a JSON
 *
 * @RequestMapping - Define la ruta base para todos los endpoints de este controller
 *                   Todos los métodos aquí empiezan con /api/productos
 *
 * @CrossOrigin - Permite peticiones desde otros dominios (CORS)
 *               origins = "*": Permite desde cualquier origen (solo desarrollo)
 *               En producción deberías poner: origins = "https://tudominio.com"
 *
 * @RequiredArgsConstructor - Inyección de dependencias por constructor (Lombok)
 *
 * @Slf4j - Logger para debugging
 */
@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
@Slf4j
public class ProductoController {

    /**
     * Inyección del servicio
     * El controller NO habla directamente con el repository
     * Siempre usa el service (separación de capas)
     */
    private final ProductoService productoService;

    // =========================================================================
    // ENDPOINTS CRUD
    // =========================================================================

    /**
     * GET /api/productos
     * Obtiene todos los productos
     *
     * @GetMapping - Maneja peticiones HTTP GET
     *               GET se usa para LEER datos (no modifica nada)
     *
     * ResponseEntity<T> - Wrapper para respuestas HTTP
     *                     Permite controlar:
     *                     - Código de estado (200, 404, 500, etc.)
     *                     - Headers
     *                     - Body
     *
     * CÓDIGOS HTTP:
     * - 200 OK: Petición exitosa
     * - 201 CREATED: Recurso creado
     * - 204 NO CONTENT: Éxito sin contenido
     * - 400 BAD REQUEST: Datos inválidos
     * - 404 NOT FOUND: No encontrado
     * - 500 INTERNAL SERVER ERROR: Error del servidor
     *
     * EJEMPLO DE PETICIÓN:
     * GET http://localhost:8080/api/productos
     *
     * RESPUESTA:
     * [
     *   {
     *     "id": 1,
     *     "nombre": "Coca Cola",
     *     "categoria": "Bebidas",
     *     "precio": 3000.0,
     *     "stock": 50,
     *     "createdAt": "2025-12-09T10:30:00",
     *     "updatedAt": "2025-12-09T10:30:00"
     *   },
     *   ...
     * ]
     *
     * @return lista de productos con código 200
     */
    @GetMapping
    public ResponseEntity<List<Producto>> getAllProductos() {
        log.info("GET /api/productos - Obteniendo todos los productos");

        List<Producto> productos = productoService.findAll();

        log.info("Se encontraron {} productos", productos.size());

        return ResponseEntity.ok(productos); // 200 OK
    }

    /**
     * GET /api/productos/{id}
     * Obtiene un producto por ID
     *
     * @PathVariable - Extrae valores de la URL
     *                 /api/productos/5 → id = 5
     *
     * EJEMPLO DE PETICIÓN:
     * GET http://localhost:8080/api/productos/5
     *
     * RESPUESTA EXITOSA (200):
     * {
     *   "id": 5,
     *   "nombre": "Hamburguesa",
     *   "precio": 15000.0,
     *   ...
     * }
     *
     * RESPUESTA ERROR (404):
     * "Producto no encontrado con id: 5"
     *
     * @param id identificador del producto
     * @return producto encontrado (200) o error (404)
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductoById(@PathVariable Long id) {
        log.info("GET /api/productos/{} - Buscando producto", id);

        // 1. Buscamos el producto y lo guardamos en una variable Optional
        java.util.Optional<Producto> productoOpt = productoService.findById(id);

        // 2. Verificamos si existe
        if (productoOpt.isPresent()) {
            // CASO DE ÉXITO (Devuelve Producto)
            Producto producto = productoOpt.get();
            log.info("Producto encontrado: {}", producto.getNombre());
            return ResponseEntity.ok(producto);

        } else {
            // CASO DE ERROR (Devuelve String)
            log.warn("Producto no encontrado con id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Producto no encontrado con id: " + id);
        }
    }

    /**
     * POST /api/productos
     * Crea un nuevo producto
     *
     * @PostMapping - Maneja peticiones HTTP POST
     *                POST se usa para CREAR recursos
     *
     * @RequestBody - Convierte el JSON del body a un objeto Producto
     *
     * @Valid - Activa las validaciones de la entidad Producto
     *          (@NotBlank, @Positive, etc.)
     *          Si falla, Spring devuelve 400 automáticamente
     *
     * EJEMPLO DE PETICIÓN:
     * POST http://localhost:8080/api/productos
     * Content-Type: application/json
     *
     * Body:
     * {
     *   "nombre": "Pizza Margherita",
     *   "categoria": "Alimentos",
     *   "precio": 25000.0,
     *   "stock": 30
     * }
     *
     * RESPUESTA EXITOSA (201):
     * {
     *   "id": 10,
     *   "nombre": "Pizza Margherita",
     *   "categoria": "Alimentos",
     *   "precio": 25000.0,
     *   "stock": 30,
     *   "createdAt": "2025-12-09T14:00:00",
     *   "updatedAt": "2025-12-09T14:00:00"
     * }
     *
     * @param producto datos del producto a crear
     * @return producto creado con código 201
     */
    @PostMapping
    public ResponseEntity<?> createProducto(@Valid @RequestBody Producto producto) {
        log.info("POST /api/productos - Creando producto: {}", producto.getNombre());

        try {
            Producto created = productoService.create(producto);
            log.info("Producto creado exitosamente con id: {}", created.getId());

            return ResponseEntity
                .status(HttpStatus.CREATED) // 201 CREATED
                .body(created);
        } catch (IllegalArgumentException e) {
            log.error("Error al crear producto: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) // 400 BAD REQUEST
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/productos/{id}
     * Actualiza un producto existente
     *
     * @PutMapping - Maneja peticiones HTTP PUT
     *               PUT se usa para ACTUALIZAR recursos completos
     *
     * DIFERENCIA PUT vs PATCH:
     * - PUT: Actualiza TODO el recurso (envías todos los campos)
     * - PATCH: Actualiza PARTE del recurso (envías solo lo que cambió)
     *
     * EJEMPLO DE PETICIÓN:
     * PUT http://localhost:8080/api/productos/10
     * Content-Type: application/json
     *
     * Body:
     * {
     *   "nombre": "Pizza Napolitana",
     *   "categoria": "Alimentos",
     *   "precio": 28000.0,
     *   "stock": 25
     * }
     *
     * RESPUESTA (200):
     * {
     *   "id": 10,
     *   "nombre": "Pizza Napolitana",
     *   ...
     * }
     *
     * @param id identificador del producto
     * @param producto datos actualizados
     * @return producto actualizado (200) o error (400/404)
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProducto(
        @PathVariable Long id,
        @Valid @RequestBody Producto producto
    ) {
        log.info("PUT /api/productos/{} - Actualizando producto", id);

        try {
            Producto updated = productoService.update(id, producto);
            log.info("Producto actualizado exitosamente");

            return ResponseEntity.ok(updated); // 200 OK
        } catch (IllegalArgumentException e) {
            log.error("Error al actualizar producto: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) // 400 BAD REQUEST
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/productos/{id}
     * Elimina un producto
     *
     * @DeleteMapping - Maneja peticiones HTTP DELETE
     *                  DELETE se usa para ELIMINAR recursos
     *
     * EJEMPLO DE PETICIÓN:
     * DELETE http://localhost:8080/api/productos/10
     *
     * RESPUESTA EXITOSA (200):
     * {
     *   "message": "Producto eliminado exitosamente",
     *   "id": 10
     * }
     *
     * @param id identificador del producto a eliminar
     * @return mensaje de éxito (200) o error (404)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProducto(@PathVariable Long id) {
        log.info("DELETE /api/productos/{} - Eliminando producto", id);

        try {
            productoService.delete(id);
            log.info("Producto eliminado exitosamente");

            return ResponseEntity.ok(Map.of(
                "message", "Producto eliminado exitosamente",
                "id", id
            )); // 200 OK
        } catch (IllegalArgumentException e) {
            log.error("Error al eliminar producto: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND) // 404 NOT FOUND
                .body(Map.of("error", e.getMessage()));
        }
    }

    // =========================================================================
    // ENDPOINTS DE BÚSQUEDA Y FILTROS
    // =========================================================================

    /**
     * GET /api/productos/buscar?nombre=coca
     * Busca productos por nombre (búsqueda parcial)
     *
     * @RequestParam - Extrae parámetros de la query string
     *                 /api/productos/buscar?nombre=coca → nombre = "coca"
     *
     * required = false: Parámetro opcional
     * defaultValue: Valor por defecto si no se envía
     *
     * EJEMPLO:
     * GET http://localhost:8080/api/productos/buscar?nombre=coca
     *
     * Encuentra: "Coca Cola", "coca zero", "COCA LIGHT"
     *
     * @param nombre texto a buscar
     * @return productos que contienen ese texto
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<Producto>> searchProductos(
        @RequestParam(required = false, defaultValue = "") String nombre
    ) {
        log.info("GET /api/productos/buscar?nombre={}", nombre);

        List<Producto> productos = productoService.searchByNombre(nombre);

        log.info("Se encontraron {} productos", productos.size());

        return ResponseEntity.ok(productos);
    }

    /**
     * GET /api/productos/categoria/{categoria}
     * Obtiene productos de una categoría específica
     *
     * EJEMPLO:
     * GET http://localhost:8080/api/productos/categoria/Bebidas
     *
     * @param categoria nombre de la categoría
     * @return productos de esa categoría
     */
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<Producto>> getProductosByCategoria(@PathVariable String categoria) {
        log.info("GET /api/productos/categoria/{}", categoria);

        List<Producto> productos = productoService.findByCategoria(categoria);

        log.info("Se encontraron {} productos en categoría {}", productos.size(), categoria);

        return ResponseEntity.ok(productos);
    }

    /**
     * GET /api/productos/categorias
     * Obtiene lista de todas las categorías únicas
     *
     * EJEMPLO:
     * GET http://localhost:8080/api/productos/categorias
     *
     * RESPUESTA:
     * ["Bebidas", "Alimentos", "Postres", "Entradas"]
     *
     * @return lista de categorías
     */
    @GetMapping("/categorias")
    public ResponseEntity<List<String>> getAllCategorias() {
        log.info("GET /api/productos/categorias");

        List<String> categorias = productoService.findAllCategorias();

        return ResponseEntity.ok(categorias);
    }

    /**
     * GET /api/productos/stock/bajo
     * Obtiene productos con stock bajo (< 20 unidades)
     *
     * @return productos con stock bajo
     */
    @GetMapping("/stock/bajo")
    public ResponseEntity<List<Producto>> getStockBajo() {
        log.info("GET /api/productos/stock/bajo");

        List<Producto> productos = productoService.findStockBajo();

        log.info("Se encontraron {} productos con stock bajo", productos.size());

        return ResponseEntity.ok(productos);
    }

    /**
     * GET /api/productos/stock/critico
     * Obtiene productos con stock crítico (< 10 unidades)
     *
     * @return productos con stock crítico
     */
    @GetMapping("/stock/critico")
    public ResponseEntity<List<Producto>> getStockCritico() {
        log.info("GET /api/productos/stock/critico");

        List<Producto> productos = productoService.findStockCritico();

        log.info("Se encontraron {} productos con stock crítico", productos.size());

        return ResponseEntity.ok(productos);
    }

    // =========================================================================
    // ENDPOINTS DE OPERACIONES SOBRE STOCK
    // =========================================================================

    /**
     * PATCH /api/productos/{id}/stock/agregar
     * Agrega stock a un producto
     *
     * @PatchMapping - Actualización PARCIAL (solo un campo)
     *
     * EJEMPLO:
     * PATCH http://localhost:8080/api/productos/5/stock/agregar
     * Content-Type: application/json
     *
     * Body:
     * {
     *   "cantidad": 50
     * }
     *
     * @param id identificador del producto
     * @param body mapa con la cantidad
     * @return producto actualizado
     */
    @PatchMapping("/{id}/stock/agregar")
    public ResponseEntity<?> agregarStock(
        @PathVariable Long id,
        @RequestBody Map<String, Integer> body
    ) {
        Integer cantidad = body.get("cantidad");
        log.info("PATCH /api/productos/{}/stock/agregar - Cantidad: {}", id, cantidad);

        try {
            Producto updated = productoService.agregarStock(id, cantidad);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            log.error("Error: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PATCH /api/productos/{id}/stock/reducir
     * Reduce stock de un producto (usado en ventas)
     *
     * @param id identificador del producto
     * @param body mapa con la cantidad
     * @return producto actualizado
     */
    @PatchMapping("/{id}/stock/reducir")
    public ResponseEntity<?> reducirStock(
        @PathVariable Long id,
        @RequestBody Map<String, Integer> body
    ) {
        Integer cantidad = body.get("cantidad");
        log.info("PATCH /api/productos/{}/stock/reducir - Cantidad: {}", id, cantidad);

        try {
            Producto updated = productoService.reducirStock(id, cantidad);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            log.error("Error: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/productos/count
     * Obtiene el total de productos
     *
     * EJEMPLO:
     * GET http://localhost:8080/api/productos/count
     *
     * RESPUESTA:
     * {
     *   "total": 45
     * }
     *
     * @return mapa con el total
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> countProductos() {
        log.info("GET /api/productos/count");

        long total = productoService.count();

        return ResponseEntity.ok(Map.of("total", total));
    }
}
