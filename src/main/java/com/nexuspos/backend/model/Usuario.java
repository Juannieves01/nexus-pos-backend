package com.nexuspos.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * ENTIDAD USUARIO
 *
 * Representa un usuario del sistema con su rol y credenciales
 */
@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre completo del usuario
     */
    @Column(nullable = false, length = 100)
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombre;

    /**
     * Username único para login
     */
    @Column(nullable = false, unique = true, length = 50)
    @NotBlank(message = "El username es obligatorio")
    @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
    private String username;

    /**
     * Email del usuario (opcional)
     */
    @Column(length = 100)
    @Email(message = "Email inválido")
    private String email;

    /**
     * Contraseña (en producción debería estar hasheada)
     */
    @Column(nullable = false)
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 4, message = "La contraseña debe tener al menos 4 caracteres")
    private String password;

    /**
     * Rol del usuario
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull(message = "El rol es obligatorio")
    private Rol rol = Rol.CAJERO;

    /**
     * Si el usuario está activo o no
     */
    @Column(nullable = false)
    private Boolean activo = true;

    /**
     * Fecha de creación
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Fecha de última actualización
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Último login (opcional)
     */
    private LocalDateTime ultimoLogin;

    // Helper methods
    public boolean isAdministrador() {
        return this.rol == Rol.ADMINISTRADOR;
    }

    public boolean isCajero() {
        return this.rol == Rol.CAJERO;
    }

    public boolean isMesero() {
        return this.rol == Rol.MESERO;
    }
}
