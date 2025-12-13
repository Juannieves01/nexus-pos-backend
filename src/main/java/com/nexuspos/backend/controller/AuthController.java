package com.nexuspos.backend.controller;

import com.nexuspos.backend.model.Usuario;
import com.nexuspos.backend.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * CONTROLLER DE AUTENTICACIÓN
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UsuarioService usuarioService;

    /**
     * POST /api/auth/login
     * Autenticar usuario
     *
     * Body:
     * {
     *   "username": "admin",
     *   "password": "admin123"
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        log.info("POST /api/auth/login - Username: {}", username);

        if (username == null || password == null) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Username y password son requeridos"));
        }

        Optional<Usuario> usuarioOpt = usuarioService.login(username, password);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Credenciales inválidas"));
        }

        Usuario usuario = usuarioOpt.get();

        // Crear respuesta sin enviar la contraseña
        Map<String, Object> response = Map.of(
            "id", usuario.getId(),
            "nombre", usuario.getNombre(),
            "username", usuario.getUsername(),
            "email", usuario.getEmail() != null ? usuario.getEmail() : "",
            "rol", usuario.getRol().toString(),
            "activo", usuario.getActivo(),
            "ultimoLogin", usuario.getUltimoLogin() != null ? usuario.getUltimoLogin().toString() : ""
        );

        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/register
     * Registrar nuevo usuario (solo administradores deberían poder)
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Usuario usuario) {
        log.info("POST /api/auth/register - Username: {}", usuario.getUsername());

        try {
            Usuario nuevoUsuario = usuarioService.crearUsuario(usuario);

            Map<String, Object> response = Map.of(
                "id", nuevoUsuario.getId(),
                "nombre", nuevoUsuario.getNombre(),
                "username", nuevoUsuario.getUsername(),
                "rol", nuevoUsuario.getRol().toString(),
                "message", "Usuario creado exitosamente"
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("Error al registrar usuario: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/auth/cambiar-password
     * Cambiar contraseña del usuario actual
     */
    @PostMapping("/cambiar-password")
    public ResponseEntity<?> cambiarPassword(@RequestBody Map<String, String> datos) {
        Long usuarioId = Long.valueOf(datos.get("usuarioId"));
        String passwordActual = datos.get("passwordActual");
        String passwordNueva = datos.get("passwordNueva");

        log.info("POST /api/auth/cambiar-password - Usuario ID: {}", usuarioId);

        try {
            usuarioService.cambiarPassword(usuarioId, passwordActual, passwordNueva);
            return ResponseEntity.ok(Map.of("message", "Contraseña actualizada correctamente"));
        } catch (IllegalArgumentException e) {
            log.error("Error al cambiar contraseña: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/auth/init-admin
     * Inicializa el usuario administrador por defecto
     * (útil para debugging)
     */
    @PostMapping("/init-admin")
    public ResponseEntity<?> initAdmin() {
        log.info("POST /api/auth/init-admin");

        try {
            usuarioService.inicializarAdminPorDefecto();
            return ResponseEntity.ok(Map.of(
                "message", "Usuario administrador inicializado",
                "username", "admin",
                "password", "admin123"
            ));
        } catch (Exception e) {
            log.error("Error al inicializar admin: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
}
