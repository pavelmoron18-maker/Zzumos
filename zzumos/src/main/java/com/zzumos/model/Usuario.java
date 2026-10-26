package com.zzumos.model;

import com.zzumos.model.enums.Rol;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity @Table(name = "usuario")
@Getter @Setter @NoArgsConstructor
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idUsuario;

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(nullable = false, length = 120, unique = true)
    private String correo;

    @Column(nullable = false)
    private String contrasenaHash;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Rol rol = Rol.CLIENTE;

    private boolean activo = true;

    private LocalDateTime fechaRegistro;

    @PrePersist void antesDeGuardar() {
        if (fechaRegistro == null) fechaRegistro = LocalDateTime.now();
    }
}
