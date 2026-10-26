package com.zzumos.config;

import com.zzumos.model.Usuario;
import com.zzumos.model.enums.Rol;
import com.zzumos.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Crea las cuentas internas de prueba la primera vez que arranca la aplicación. */
@Component
public class DataInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository usuarioRepo;
    private final PasswordEncoder encoder;
    private final String password;

    public DataInitializer(UsuarioRepository usuarioRepo, PasswordEncoder encoder,
                           @Value("${zzumos.admin.password}") String password) {
        this.usuarioRepo = usuarioRepo;
        this.encoder = encoder;
        this.password = password;
    }

    @Override
    public void run(String... args) {
        crearSiNoExiste("Giovanna Carhuayo", "admin@zzumos.com", Rol.ADMINISTRADOR);
        crearSiNoExiste("Personal de atención", "personal@zzumos.com", Rol.PERSONAL);
    }

    private void crearSiNoExiste(String nombres, String correo, Rol rol) {
        if (usuarioRepo.findByCorreo(correo).isPresent()) return;
        Usuario u = new Usuario();
        u.setNombres(nombres);
        u.setCorreo(correo);
        u.setContrasenaHash(encoder.encode(password));
        u.setRol(rol);
        usuarioRepo.save(u);
        log.warn("Cuenta de prueba creada: {} ({}). Cambia su contraseña antes de desplegar.", correo, rol);
    }
}
