package com.fixflow.security;

import com.fixflow.model.EstadoUsuario;
import com.fixflow.model.RolUsuario;
import com.fixflow.model.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserDetailsImpl implements UserDetails {

    private final Long idUsuario;
    private final String email;
    private final String password;
    private final RolUsuario rol;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Long idUsuario, String email, String password, RolUsuario rol,
                           boolean enabled, Collection<? extends GrantedAuthority> authorities) {
        this.idUsuario = idUsuario;
        this.email = email;
        this.password = password;
        this.rol = rol;
        this.enabled = enabled;
        this.authorities = authorities;
    }

    public static UserDetailsImpl build(Usuario usuario) {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));
        return new UserDetailsImpl(
                usuario.getIdUsuario(),
                usuario.getEmail(),
                usuario.getPassword(),
                usuario.getRol(),
                usuario.getEstado() == EstadoUsuario.ACTIVO,
                authorities
        );
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public RolUsuario getRol() {
        return rol;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
