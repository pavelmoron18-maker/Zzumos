package com.zzumos.service;

import com.zzumos.dto.Dtos.RegistroRequest;
import com.zzumos.dto.Dtos.UsuarioResponse;
import com.zzumos.model.Cliente;
import com.zzumos.model.Usuario;
import com.zzumos.model.enums.Rol;
import com.zzumos.repository.ClienteRepository;
import com.zzumos.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UsuarioRepository usuarioRepo;
    private final ClienteRepository clienteRepo;
    private final PasswordEncoder encoder;

    public AuthService(UsuarioRepository usuarioRepo, ClienteRepository clienteRepo, PasswordEncoder encoder) {
        this.usuarioRepo = usuarioRepo;
        this.clienteRepo = clienteRepo;
        this.encoder = encoder;
    }

    /** BPM "Registro": valida, crea la cuenta (USUARIO) y el perfil (CLIENTE) en una sola transacción. */
    @Transactional
    public UsuarioResponse registrar(RegistroRequest req) {
        String correo = req.correo().trim().toLowerCase();
        if (usuarioRepo.existsByCorreo(correo)) {
            throw new IllegalStateException("Ya existe una cuenta con ese correo");
        }

        Usuario usuario = new Usuario();
        usuario.setNombres(req.nombres().trim());
        usuario.setCorreo(correo);
        usuario.setContrasenaHash(encoder.encode(req.contrasena()));
        usuario.setRol(Rol.CLIENTE);
        usuarioRepo.save(usuario);

        Cliente cliente = new Cliente();
        cliente.setUsuario(usuario);
        cliente.setNombres(req.nombres().trim());
        cliente.setApellidos(req.apellidos() == null || req.apellidos().isBlank() ? null : req.apellidos().trim());
        cliente.setTelefono(req.telefono());
        clienteRepo.save(cliente);

        return new UsuarioResponse(usuario.getIdUsuario(), usuario.getNombres(), usuario.getCorreo(), usuario.getRol());
    }
}
