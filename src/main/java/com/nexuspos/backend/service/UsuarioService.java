package com.nexuspos.backend.service;

import com.nexuspos.backend.model.Rol;
import com.nexuspos.backend.model.Usuario;
import com.nexuspos.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Autenticar usuario (login)
     */
    public Optional<Usuario> login(String username, String password) {
        log.info("Intento de login para usuario: {}", username);

        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsernameAndActivo(username, true);

        if (usuarioOpt.isEmpty()) {
            log.warn("Usuario no encontrado o inactivo: {}", username);
            return Optional.empty();
        }

        Usuario usuario = usuarioOpt.get();

        // Verificar contraseña (en producción debería ser hash comparison)
        if (!usuario.getPassword().equals(password)) {
            log.warn("Contraseña incorrecta para usuario: {}", username);
            return Optional.empty();
        }

        // Actualizar último login
        usuario.setUltimoLogin(LocalDateTime.now());
        usuarioRepository.save(usuario);

        log.info("Login exitoso para usuario: {} (Rol: {})", username, usuario.getRol());
        return Optional.of(usuario);
    }

    /**
     * Crear nuevo usuario
     */
    @Transactional
    public Usuario crearUsuario(Usuario usuario) {
        log.info("Creando nuevo usuario: {}", usuario.getUsername());

        // Validar username único
        if (usuarioRepository.existsByUsername(usuario.getUsername())) {
            throw new IllegalArgumentException("El username ya existe: " + usuario.getUsername());
        }

        // Validar email único si se proporciona
        if (usuario.getEmail() != null && !usuario.getEmail().isEmpty()) {
            if (usuarioRepository.existsByEmail(usuario.getEmail())) {
                throw new IllegalArgumentException("El email ya existe: " + usuario.getEmail());
            }
        }

        Usuario saved = usuarioRepository.save(usuario);
        log.info("Usuario creado con ID: {}", saved.getId());
        return saved;
    }

    /**
     * Actualizar usuario
     */
    @Transactional
    public Usuario actualizarUsuario(Long id, Usuario usuarioActualizado) {
        log.info("Actualizando usuario ID: {}", id);

        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));

        // Validar username único (si cambió)
        if (!usuario.getUsername().equals(usuarioActualizado.getUsername())) {
            if (usuarioRepository.existsByUsername(usuarioActualizado.getUsername())) {
                throw new IllegalArgumentException("El username ya existe: " + usuarioActualizado.getUsername());
            }
            usuario.setUsername(usuarioActualizado.getUsername());
        }

        // Validar email único (si cambió)
        if (usuarioActualizado.getEmail() != null && !usuarioActualizado.getEmail().isEmpty()) {
            if (!usuarioActualizado.getEmail().equals(usuario.getEmail())) {
                if (usuarioRepository.existsByEmail(usuarioActualizado.getEmail())) {
                    throw new IllegalArgumentException("El email ya existe: " + usuarioActualizado.getEmail());
                }
            }
        }

        usuario.setNombre(usuarioActualizado.getNombre());
        usuario.setEmail(usuarioActualizado.getEmail());
        usuario.setRol(usuarioActualizado.getRol());
        usuario.setActivo(usuarioActualizado.getActivo());

        // Solo actualizar contraseña si se proporciona una nueva
        if (usuarioActualizado.getPassword() != null && !usuarioActualizado.getPassword().isEmpty()) {
            usuario.setPassword(usuarioActualizado.getPassword());
        }

        return usuarioRepository.save(usuario);
    }

    /**
     * Cambiar contraseña
     */
    @Transactional
    public void cambiarPassword(Long id, String passwordActual, String passwordNueva) {
        log.info("Cambiando contraseña para usuario ID: {}", id);

        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (!usuario.getPassword().equals(passwordActual)) {
            throw new IllegalArgumentException("Contraseña actual incorrecta");
        }

        usuario.setPassword(passwordNueva);
        usuarioRepository.save(usuario);
        log.info("Contraseña actualizada para usuario: {}", usuario.getUsername());
    }

    /**
     * Activar/Desactivar usuario
     */
    @Transactional
    public Usuario toggleActivo(Long id) {
        log.info("Cambiando estado de usuario ID: {}", id);

        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.setActivo(!usuario.getActivo());
        return usuarioRepository.save(usuario);
    }

    /**
     * Obtener todos los usuarios
     */
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    /**
     * Obtener usuario por ID
     */
    public Optional<Usuario> findById(Long id) {
        return usuarioRepository.findById(id);
    }

    /**
     * Obtener por username
     */
    public Optional<Usuario> findByUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    /**
     * Obtener usuarios por rol
     */
    public List<Usuario> findByRol(Rol rol) {
        return usuarioRepository.findByRol(rol);
    }

    /**
     * Obtener usuarios activos
     */
    public List<Usuario> findActivos() {
        return usuarioRepository.findByActivo(true);
    }

    /**
     * Eliminar usuario
     */
    @Transactional
    public void delete(Long id) {
        log.warn("Eliminando usuario ID: {}", id);
        usuarioRepository.deleteById(id);
    }

    /**
     * Contar usuarios por rol
     */
    public long countByRol(Rol rol) {
        return usuarioRepository.countByRol(rol);
    }

    /**
     * Contar usuarios activos
     */
    public long countActivos() {
        return usuarioRepository.countByActivo(true);
    }

    /**
     * Inicializar usuario administrador por defecto
     */
    @Transactional
    public void inicializarAdminPorDefecto() {
        if (usuarioRepository.count() == 0) {
            log.info("Creando usuario administrador por defecto");

            Usuario admin = new Usuario();
            admin.setNombre("Administrador");
            admin.setUsername("admin");
            admin.setPassword("admin123");
            admin.setRol(Rol.ADMINISTRADOR);
            admin.setActivo(true);

            usuarioRepository.save(admin);
            log.info("Usuario administrador creado: username=admin, password=admin123");
        }
    }
}
