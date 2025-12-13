package com.nexuspos.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * CONTROLLER DE HOME - Página de bienvenida
 *
 * Este controller maneja la ruta raíz para evitar el error "Whitelabel Error Page"
 */
@RestController
@RequestMapping("/")
public class HomeController {

    /**
     * GET /
     * Endpoint raíz que muestra información de la API
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> home() {
        Map<String, Object> response = new HashMap<>();

        response.put("mensaje", "🚀 NexusPOS Backend API");
        response.put("version", "1.0.0");
        response.put("estado", "Activo");

        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("Obtener todos los productos", "GET /api/productos");
        endpoints.put("Obtener producto por ID", "GET /api/productos/{id}");
        endpoints.put("Crear producto", "POST /api/productos");
        endpoints.put("Actualizar producto", "PUT /api/productos/{id}");
        endpoints.put("Eliminar producto", "DELETE /api/productos/{id}");
        endpoints.put("Buscar por nombre", "GET /api/productos/buscar?nombre=coca");
        endpoints.put("Filtrar por categoría", "GET /api/productos/categoria/Bebidas");
        endpoints.put("Obtener categorías", "GET /api/productos/categorias");
        endpoints.put("Stock bajo", "GET /api/productos/stock/bajo");
        endpoints.put("Stock crítico", "GET /api/productos/stock/critico");
        endpoints.put("Agregar stock", "PATCH /api/productos/{id}/stock/agregar");
        endpoints.put("Reducir stock", "PATCH /api/productos/{id}/stock/reducir");
        endpoints.put("Contar productos", "GET /api/productos/count");

        response.put("endpoints", endpoints);

        Map<String, String> ejemplos = new HashMap<>();
        ejemplos.put("Ver todos los productos", "http://localhost:8080/api/productos");
        ejemplos.put("Ver categorías", "http://localhost:8080/api/productos/categorias");
        ejemplos.put("Contar productos", "http://localhost:8080/api/productos/count");

        response.put("ejemplos", ejemplos);

        return ResponseEntity.ok(response);
    }
}
