package com.nexuspos.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * CONTROLLER DE HEALTH CHECK
 *
 * Permite verificar el estado de la aplicación y la conexión a la base de datos
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    /**
     * GET /health
     * Verifica el estado de la aplicación y la base de datos
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("aplicacion", "NexusPOS Backend");
        health.put("estado", "Activo");

        // Verificar conexión a base de datos
        try (Connection connection = dataSource.getConnection()) {
            if (connection != null && !connection.isClosed()) {
                health.put("baseDatos", "Conectada ✅");
                health.put("dbUrl", connection.getMetaData().getURL());
            } else {
                health.put("baseDatos", "Error ❌");
            }
        } catch (Exception e) {
            health.put("baseDatos", "Error: " + e.getMessage() + " ❌");
            health.put("solucion", "Verifica que PostgreSQL esté corriendo y que la base de datos 'nexuspos_db' exista");
        }

        return ResponseEntity.ok(health);
    }
}
