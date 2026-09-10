package com.fixflow;

import com.fixflow.dto.EquipoRequest;
import com.fixflow.exception.ResourceNotFoundException;
import com.fixflow.model.Cliente;
import com.fixflow.model.Equipo;
import com.fixflow.model.RolUsuario;
import com.fixflow.model.Usuario;
import com.fixflow.repository.ClienteRepository;
import com.fixflow.repository.UsuarioRepository;
import com.fixflow.service.EquipoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EquipoServiceTest {

    @Autowired
    private EquipoService equipoService;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        Usuario usuario = new Usuario();
        usuario.setNombre("Cliente");
        usuario.setApellido("Equipos");
        usuario.setEmail("cliente-equipos-" + System.nanoTime() + "@test.com");
        usuario.setPassword(passwordEncoder.encode("secret123"));
        usuario.setRol(RolUsuario.CLIENTE);
        usuario = usuarioRepository.save(usuario);

        cliente = new Cliente();
        cliente.setUsuario(usuario);
        cliente.setTelefono("123456789");
        cliente.setDireccion("Calle 1");
        cliente = clienteRepository.save(cliente);
    }

    private EquipoRequest buildRequest(String serial) {
        EquipoRequest request = new EquipoRequest();
        request.setIdCliente(cliente.getIdCliente());
        request.setTipo("Laptop");
        request.setMarca("Dell");
        request.setModelo("Latitude 5420");
        request.setSerial(serial);
        request.setDescripcion("Equipo de prueba");
        return request;
    }

    @Test
    void shouldCreateEquipo() {
        Equipo created = equipoService.create(buildRequest("SER-" + System.nanoTime()));

        assertNotNull(created.getIdEquipo());
        assertEquals(cliente.getIdCliente(), created.getCliente().getIdCliente());
    }

    @Test
    void shouldGetEquipo() {
        Long id = equipoService.create(buildRequest("SER-" + System.nanoTime())).getIdEquipo();

        Equipo found = equipoService.findById(id);

        assertEquals(id, found.getIdEquipo());
    }

    @Test
    void shouldUpdateEquipo() {
        String serial = "SER-" + System.nanoTime();
        Long id = equipoService.create(buildRequest(serial)).getIdEquipo();

        EquipoRequest request = buildRequest("SER-" + System.nanoTime());
        request.setMarca("HP");
        Equipo updated = equipoService.update(id, request);

        assertEquals("HP", updated.getMarca());
    }

    @Test
    void shouldDeleteEquipo() {
        Long id = equipoService.create(buildRequest("SER-" + System.nanoTime())).getIdEquipo();

        equipoService.delete(id);

        assertThrows(ResourceNotFoundException.class, () -> equipoService.findById(id));
    }

    @Test
    void shouldRejectDuplicateSerial() {
        String serial = "SER-" + System.nanoTime();
        equipoService.create(buildRequest(serial));

        assertThrows(DataIntegrityViolationException.class, () -> equipoService.create(buildRequest(serial)));
    }

    @Test
    void shouldThrowNotFoundForUnknownId() {
        assertThrows(ResourceNotFoundException.class, () -> equipoService.findById(999999L));
    }

    @Test
    void shouldThrowNotFoundForUnknownCliente() {
        EquipoRequest request = buildRequest("SER-" + System.nanoTime());
        request.setIdCliente(999999L);

        assertThrows(ResourceNotFoundException.class, () -> equipoService.create(request));
    }
}