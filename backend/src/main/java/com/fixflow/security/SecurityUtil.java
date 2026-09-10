package com.fixflow.security;

import com.fixflow.model.RolUsuario;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtil {

    private SecurityUtil() {
    }

    public static UserDetailsImpl getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl user) {
            return user;
        }
        return null;
    }

    public static Long getCurrentUserId() {
        UserDetailsImpl user = getCurrentUser();
        return user != null ? user.getIdUsuario() : null;
    }

    public static RolUsuario getCurrentRole() {
        UserDetailsImpl user = getCurrentUser();
        return user != null ? user.getRol() : null;
    }
}