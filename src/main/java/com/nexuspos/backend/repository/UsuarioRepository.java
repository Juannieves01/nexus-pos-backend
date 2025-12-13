package com.nexuspos.backend.repository;

import com.nexuspos.backend.model.Rol;
import com.nexuspos.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Buscar por username (para login)
    Optional<Usuario> findByUsername(String username);

    // Buscar por email
    Optional<Usuario> findByEmail(String email);

    // Verificar si existe username
    boolean existsByUsername(String username);

    // Verificar si existe email
    boolean existsByEmail(String email);

    // Buscar por rol
    List<Usuario> findByRol(Rol rol);

    // Buscar usuarios activos
    List<Usuario> findByActivo(Boolean activo);

    // Buscar por username y activo
    Optional<Usuario> findByUsernameAndActivo(String username, Boolean activo);

    // Contar usuarios por rol
    long countByRol(Rol rol);

    // Contar usuarios activos
    long countByActivo(Boolean activo);
}
