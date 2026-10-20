package com.fixflow;

import com.fixflow.dto.RegistroRequest;
import com.fixflow.dto.UsuarioRequest;
import com.fixflow.exception.ResourceNotFoundException;
import com.fixflow.model.EstadoUsuario;
import com.fixflow.model.RolUsuario;
import com.fixflow.repository.ClienteRepository;
import com.fixflow.repository.TecnicoRepository;
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

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private TecnicoRepository tecnicoRepository;

    private UsuarioRequest buildTecnicoRequest(String email, String especialidad, String telefono) {
        UsuarioRequest request = new UsuarioRequest();
        request.setNombre("Tec");
        request.setApellido("Nico");
        request.setEmail(email);
        request.setPassword("secret123");
        request.setRol(RolUsuario.TECNICO);
        request.setEspecialidad(especialidad);
        request.setTelefono(telefono);
        return request;
    }

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
    void shouldDeactivateUserButKeepIt() {
        Long id = usuarioService.create(buildRequest("deactivate-" + System.nanoTime() + "@test.com")).getIdUsuario();

        usuarioService.desactivar(id);

        var found = usuarioService.findById(id);
        assertEquals(EstadoUsuario.INACTIVO, found.getEstado());
    }

    @Test
    void shouldDeleteUserFromDatabase() {
        Long id = usuarioService.create(buildRequest("delete-" + System.nanoTime() + "@test.com")).getIdUsuario();

        usuarioService.eliminar(id);

        assertThrows(ResourceNotFoundException.class, () -> usuarioService.findById(id));
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

    @Test
    void shouldRegisterClientWithProfile() {        RegistroRequest registro = new RegistroRequest();
        registro.setNombre("Juan");
        registro.setApellido("García");
        registro.setEmail("juan-" + System.nanoTime() + "@test.com");
        registro.setPassword("clave123");
        registro.setTelefono("3001234567");
        registro.setDireccion("Calle 10 #5-20");

        var created = usuarioService.registrarCliente(registro);

        assertNotNull(created);
        assertNotNull(created.getIdUsuario());
        assertEquals("Juan", created.getNombre());
        assertEquals(RolUsuario.CLIENTE, created.getRol());
        assertEquals(EstadoUsuario.ACTIVO, created.getEstado());

        var cliente = clienteRepository.findByUsuario_IdUsuario(created.getIdUsuario());
        assertTrue(cliente.isPresent());
        assertEquals("3001234567", cliente.get().getTelefono());
        assertEquals("Calle 10 #5-20", cliente.get().getDireccion());
    }

    @Test
    void shouldRejectDuplicateEmailOnRegistration() {
        String email = "dupreg-" + System.nanoTime() + "@test.com";

        RegistroRequest registro = new RegistroRequest();
        registro.setNombre("María");
        registro.setApellido("López");
        registro.setEmail(email);
        registro.setPassword("clave123");
        registro.setTelefono("3009999999");
        registro.setDireccion("Carrera 5 #1-10");

        usuarioService.registrarCliente(registro);

        assertThrows(DataIntegrityViolationException.class, () -> usuarioService.registrarCliente(registro));
    }

    @Test
    void shouldCreateTecnicoWithProfile() {
        String email = "tec-" + System.nanoTime() + "@test.com";
        var created = usuarioService.create(buildTecnicoRequest(email, "Redes y CCTV", "955123456"));

        assertNotNull(created);
        var tecnico = tecnicoRepository.findByUsuario_IdUsuario(created.getIdUsuario());
        assertTrue(tecnico.isPresent());
        assertEquals("Redes y CCTV", tecnico.get().getEspecialidad());
        assertEquals("955123456", tecnico.get().getTelefono());
    }

    @Test
    void shouldRejectTecnicoWithoutEspecialidad() {
        UsuarioRequest request = buildTecnicoRequest("tecniesp-" + System.nanoTime() + "@test.com", "  ", "955123456");
        assertThrows(IllegalArgumentException.class, () -> usuarioService.create(request));
    }

    @Test
    void shouldRejectTecnicoWithoutTelefono() {
        UsuarioRequest request = buildTecnicoRequest("tecnitel-" + System.nanoTime() + "@test.com", "Redes", "");
        assertThrows(IllegalArgumentException.class, () -> usuarioService.create(request));
    }
}