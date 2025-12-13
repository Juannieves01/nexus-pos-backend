package com.nexuspos.backend.config;

import com.nexuspos.backend.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * INICIALIZADOR DE DATOS
 *
 * Crea datos iniciales necesarios para la aplicación
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UsuarioService usuarioService;

    @Bean
    public ApplicationRunner initData() {
        return args -> {
            log.info("=== Iniciando datos de la aplicación ===");

            // Crear usuario administrador por defecto si no existe ningún usuario
            usuarioService.inicializarAdminPorDefecto();

            log.info("=== Datos inicializados correctamente ===");
        };
    }
}
