package com.nexuspos.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CLASE PRINCIPAL DE LA APLICACIÓN NEXUSPOS
 *
 * ¿Qué hace @SpringBootApplication?
 * Es una anotación "mágica" que combina 3 anotaciones:
 *
 * 1. @Configuration: Marca esta clase como fuente de configuración
 * 2. @EnableAutoConfiguration: Activa auto-configuración de Spring Boot
 *    (detecta dependencias en pom.xml y configura automáticamente)
 * 3. @ComponentScan: Escanea paquetes para encontrar componentes
 *    (Controllers, Services, Repositories, etc.)
 *
 * ¿Por qué es importante?
 * Sin esta anotación, Spring Boot no funcionaría. Es el "cerebro" que:
 * - Inicia el servidor Tomcat en puerto 8080
 * - Conecta a la base de datos
 * - Configura Jackson para JSON
 * - Escanea y registra todos tus @RestController, @Service, etc.
 */
@SpringBootApplication
public class NexusPosBackendApplication {

    /**
     * MÉTODO MAIN - Punto de entrada de Java
     *
     * ¿Qué hace SpringApplication.run()?
     * 1. Inicia el contenedor de Spring (IoC Container)
     * 2. Levanta el servidor web embebido (Tomcat)
     * 3. Escanea y configura todos los componentes
     * 4. Conecta a la base de datos
     * 5. Registra todos los endpoints REST
     *
     * @param args argumentos de línea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(NexusPosBackendApplication.class, args);

        // Una vez que veas este mensaje en consola, significa que:
        // - El servidor está corriendo en http://localhost:8080
        // - La base de datos está conectada
        // - Todos los endpoints están listos
        System.out.println("\n" +
                "╔═══════════════════════════════════════════════════════╗\n" +
                "║                                                       ║\n" +
                "║       🚀 NEXUSPOS BACKEND INICIADO CON ÉXITO 🚀      ║\n" +
                "║                                                       ║\n" +
                "║   📡 API corriendo en: http://localhost:8080         ║\n" +
                "║   📚 Documentación: http://localhost:8080/api        ║\n" +
                "║   💾 Base de datos: PostgreSQL conectada             ║\n" +
                "║                                                       ║\n" +
                "╚═══════════════════════════════════════════════════════╝\n");
    }
}
