package com.nexuspos.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CONFIGURACIÓN DE CORS (Cross-Origin Resource Sharing)
 *
 * ¿Qué es CORS?
 * --------------
 * Mecanismo de seguridad de los navegadores que BLOQUEA peticiones HTTP
 * entre diferentes dominios por defecto.
 *
 * PROBLEMA:
 * - Frontend (React): http://localhost:3000
 * - Backend (Spring Boot): http://localhost:8080
 * - Son DIFERENTES dominios → navegador bloquea la petición
 *
 * ERROR sin CORS:
 * "Access to fetch at 'http://localhost:8080/api/productos' from origin
 *  'http://localhost:3000' has been blocked by CORS policy"
 *
 * SOLUCIÓN:
 * Configurar el backend para que PERMITA peticiones desde el frontend.
 *
 * ¿Por qué existe CORS?
 * ----------------------
 * SEGURIDAD. Imagina:
 * - Abres tu banco en: https://mibanco.com
 * - En otra pestaña abres: https://sitiomalicioso.com
 * - Sin CORS, sitiomalicioso.com podría hacer peticiones a mibanco.com
 *   usando tus cookies de sesión y robar tu dinero
 *
 * Con CORS, mibanco.com dice: "Solo acepto peticiones de mi propio dominio"
 *
 * @Configuration - Marca esta clase como fuente de configuración de Spring
 */
@Configuration
public class CorsConfig {

    /**
     * Configura las reglas de CORS globalmente
     *
     * @Bean - Registra este método como un bean de Spring
     *         Spring lo ejecuta automáticamente al iniciar
     *
     * @return configurador de CORS
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**") // Aplica a todas las rutas /api/*
                    // ===== ORÍGENES PERMITIDOS =====
                    // DESARROLLO: Permite cualquier origen (más flexible)
                    // allowedOriginPatterns("*") permite cualquier dominio cuando usas allowCredentials(true)
                    .allowedOriginPatterns("*")

                    // PRODUCCIÓN: Reemplazar por lista específica
                    // .allowedOriginPatterns(
                    //     "https://nexuspos.com",
                    //     "https://www.nexuspos.com",
                    //     "capacitor://localhost",
                    //     "ionic://localhost"
                    // )

                    // ===== MÉTODOS HTTP PERMITIDOS =====
                    .allowedMethods(
                        "GET",      // Leer datos
                        "POST",     // Crear recursos
                        "PUT",      // Actualizar completo
                        "PATCH",    // Actualización parcial
                        "DELETE",   // Eliminar
                        "OPTIONS"   // Pre-flight request (navegador lo hace automáticamente)
                    )

                    // ===== HEADERS PERMITIDOS =====
                    // Permite todos los headers (útil para desarrollo)
                    .allowedHeaders("*")

                    // ===== EXPONER HEADERS =====
                    // Headers que el frontend puede leer de la respuesta
                    .exposedHeaders("Authorization", "Content-Type")

                    // ===== PERMITIR CREDENCIALES =====
                    // Permite enviar cookies/headers de autenticación
                    .allowCredentials(true)

                    // ===== TIEMPO DE CACHE =====
                    // El navegador cachea la configuración CORS por 1 hora
                    // Evita hacer preflight OPTIONS en cada petición
                    .maxAge(3600);
            }
        };
    }
}
