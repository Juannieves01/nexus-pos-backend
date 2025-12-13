package com.nexuspos.backend.controller;

import com.nexuspos.backend.model.Rol;
import com.nexuspos.backend.model.Usuario;
import com.nexuspos.backend.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * CONTROLLER DE USUARIOS - API REST
 */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Slf4j
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * GET /api/usuarios
     * Obtiene todos los usuarios
     */
    @GetMapping
    public ResponseEntity<List<Usuario>> getAllUsuarios() {
        log.info("GET /api/usuarios");
        List<Usuario> usuarios = usuarioService.findAll();

        // Ocultar contraseñas
        usuarios.forEach(u -> u.setPassword(null));

        return ResponseEntity.ok(usuarios);
    }

    /**
     * GET /api/usuarios/{id}
     * Obtiene un usuario por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUsuarioById(@PathVariable Long id) {
        log.info("GET /api/usuarios/{}", id);

        return usuarioService.findById(id)
            .map(usuario -> {
                usuario.setPassword(null); // Ocultar contraseña
                return ResponseEntity.ok((Object) usuario);
            })
            .orElseGet(() -> ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body((Object) Map.of("error", "Usuario no encontrado")));
    }

    /**
     * GET /api/usuarios/username/{username}
     * Obtiene un usuario por username
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<?> getUsuarioByUsername(@PathVariable String username) {
        log.info("GET /api/usuarios/username/{}", username);

        return usuarioService.findByUsername(username)
            .map(usuario -> {
                usuario.setPassword(null);
                return ResponseEntity.ok((Object) usuario);
            })
            .orElseGet(() -> ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body((Object) Map.of("error", "Usuario no encontrado")));
    }

    /**
     * GET /api/usuarios/rol/{rol}
     * Obtiene usuarios por rol
     */
    @GetMapping("/rol/{rol}")
    public ResponseEntity<?> getUsuariosByRol(@PathVariable String rol) {
        log.info("GET /api/usuarios/rol/{}", rol);

        try {
            Rol rolEnum = Rol.valueOf(rol.toUpperCase());
            List<Usuario> usuarios = usuarioService.findByRol(rolEnum);
            usuarios.forEach(u -> u.setPassword(null));
            return ResponseEntity.ok(usuarios);
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Rol inválido: " + rol));
        }
    }

    /**
     * GET /api/usuarios/activos
     * Obtiene usuarios activos
     */
    @GetMapping("/activos")
    public ResponseEntity<List<Usuario>> getUsuariosActivos() {
        log.info("GET /api/usuarios/activos");
        List<Usuario> usuarios = usuarioService.findActivos();
        usuarios.forEach(u -> u.setPassword(null));
        return ResponseEntity.ok(usuarios);
    }

    /**
     * POST /api/usuarios
     * Crea un nuevo usuario
     */
    @PostMapping
    public ResponseEntity<?> createUsuario(@RequestBody Usuario usuario) {
        log.info("POST /api/usuarios - Username: {}", usuario.getUsername());

        try {
            Usuario nuevoUsuario = usuarioService.crearUsuario(usuario);
            nuevoUsuario.setPassword(null);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoUsuario);
        } catch (IllegalArgumentException e) {
            log.error("Error al crear usuario: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/usuarios/{id}
     * Actualiza un usuario
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUsuario(@PathVariable Long id, @RequestBody Usuario usuario) {
        log.info("PUT /api/usuarios/{}", id);

        try {
            Usuario usuarioActualizado = usuarioService.actualizarUsuario(id, usuario);
            usuarioActualizado.setPassword(null);
            return ResponseEntity.ok(usuarioActualizado);
        } catch (IllegalArgumentException e) {
            log.error("Error al actualizar usuario: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PATCH /api/usuarios/{id}/toggle-activo
     * Activa/Desactiva un usuario
     */
    @PatchMapping("/{id}/toggle-activo")
    public ResponseEntity<?> toggleActivo(@PathVariable Long id) {
        log.info("PATCH /api/usuarios/{}/toggle-activo", id);

        try {
            Usuario usuario = usuarioService.toggleActivo(id);
            usuario.setPassword(null);
            return ResponseEntity.ok(usuario);
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/usuarios/{id}
     * Elimina un usuario
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUsuario(@PathVariable Long id) {
        log.info("DELETE /api/usuarios/{}", id);

        try {
            usuarioService.delete(id);
            return ResponseEntity.ok(Map.of("message", "Usuario eliminado correctamente"));
        } catch (Exception e) {
            log.error("Error al eliminar usuario: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al eliminar usuario: " + e.getMessage()));
        }
    }

    /**
     * GET /api/usuarios/stats
     * Obtiene estadísticas de usuarios
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        log.info("GET /api/usuarios/stats");

        long totalActivos = usuarioService.countActivos();
        long totalAdmins = usuarioService.countByRol(Rol.ADMINISTRADOR);
        long totalCajeros = usuarioService.countByRol(Rol.CAJERO);
        long totalMeseros = usuarioService.countByRol(Rol.MESERO);

        return ResponseEntity.ok(Map.of(
            "activos", totalActivos,
            "administradores", totalAdmins,
            "cajeros", totalCajeros,
            "meseros", totalMeseros
        ));
    }
}
