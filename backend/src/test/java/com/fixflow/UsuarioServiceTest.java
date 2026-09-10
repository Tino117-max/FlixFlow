package com.fixflow;

import com.fixflow.dto.UsuarioRequest;
import com.fixflow.exception.ResourceNotFoundException;
import com.fixflow.model.EstadoUsuario;
import com.fixflow.model.RolUsuario;
import com.fixflow.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UsuarioServiceTest {

    @Autowired
    private UsuarioService usuarioService;

    private UsuarioRequest buildRequest(String email) {
        UsuarioRequest request = new UsuarioRequest();
        request.setNombre("Ana");
        request.setApellido("Pérez");
        request.setEmail(email);
        request.setPassword("secret123");
        request.setRol(RolUsuario.CLIENTE);
        return request;
    }

    @Test
    void shouldCreateUser() {
        var created = usuarioService.create(buildRequest("ana-" + System.nanoTime() + "@test.com"));

        assertNotNull(created);
        assertNotNull(created.getIdUsuario());
        assertNotNull(created.getFechaRegistro());
    }

    @Test
    void shouldCreateAndGetUser() {
        String email = "consulta-" + System.nanoTime() + "@test.com";
        Long id = usuarioService.create(buildRequest(email)).getIdUsuario();

        var found = usuarioService.findById(id);

        assertEquals(email, found.getEmail());
    }

    @Test
    void shouldUpdateUser() {
        Long id = usuarioService.create(buildRequest("update-" + System.nanoTime() + "@test.com")).getIdUsuario();

        var request = buildRequest("cambiado-" + System.nanoTime() + "@test.com");
        request.setNombre("María");

        var updated = usuarioService.update(id, request);

        assertEquals("María", updated.getNombre());
        assertEquals(request.getEmail(), updated.getEmail());
    }

    @Test
    void shouldDeactivateUserInsteadOfDelete() {
        Long id = usuarioService.create(buildRequest("deactivate-" + System.nanoTime() + "@test.com")).getIdUsuario();

        usuarioService.delete(id);

        var found = usuarioService.findById(id);
        assertEquals(EstadoUsuario.INACTIVO, found.getEstado());
    }

    @Test
    void shouldThrowNotFoundWhenUserDoesNotExist() {
        assertThrows(ResourceNotFoundException.class, () -> usuarioService.findById(999999L));
    }

    @Test
    void shouldRejectDuplicateEmail() {
        String email = "duplicado-" + System.nanoTime() + "@test.com";
        usuarioService.create(buildRequest(email));

        assertThrows(DataIntegrityViolationException.class, () -> usuarioService.create(buildRequest(email)));
    }

    @Test
    void shouldRejectUserWithoutPassword() {
        UsuarioRequest request = buildRequest("sinpass-" + System.nanoTime() + "@test.com");
        request.setPassword(null);

        assertThrows(IllegalArgumentException.class, () -> usuarioService.create(request));
    }
}