package com.zzumos.security;

import com.zzumos.model.Usuario;
import com.zzumos.model.enums.Rol;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

/** Datos del usuario autenticado que se guardan en la sesión. */
public class UsuarioPrincipal implements UserDetails {
    private final Integer idUsuario;
    private final String nombres;
    private final String correo;
    private final String contrasenaHash;
    private final Rol rol;
    private final boolean activo;

    public UsuarioPrincipal(Usuario u) {
        this.idUsuario = u.getIdUsuario();
        this.nombres = u.getNombres();
        this.correo = u.getCorreo();
        this.contrasenaHash = u.getContrasenaHash();
        this.rol = u.getRol();
        this.activo = u.isActivo();
    }

    public Integer getIdUsuario() { return idUsuario; }
    public String getNombres() { return nombres; }
    public String getCorreo() { return correo; }
    public Rol getRol() { return rol; }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }
    @Override public String getPassword() { return contrasenaHash; }
    @Override public String getUsername() { return correo; }
    @Override public boolean isEnabled() { return activo; }
}
