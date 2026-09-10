package com.fixflow;

import com.fixflow.dto.SolicitudRequest;
import com.fixflow.exception.ResourceNotFoundException;
import com.fixflow.model.*;
import com.fixflow.repository.ClienteRepository;
import com.fixflow.repository.EquipoRepository;
import com.fixflow.repository.UsuarioRepository;
import com.fixflow.service.SolicitudService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SolicitudServiceTest {

    @Autowired
    private SolicitudService solicitudService;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private EquipoRepository equipoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Cliente cliente;
    private Equipo equipo;

    @BeforeEach
    void setUp() {
        Usuario usuario = new Usuario();
        usuario.setNombre("Cliente");
        usuario.setApellido("Solicitudes");
        usuario.setEmail("cliente-solicitudes-" + System.nanoTime() + "@test.com");
        usuario.setPassword(passwordEncoder.encode("secret123"));
        usuario.setRol(RolUsuario.CLIENTE);
        usuario = usuarioRepository.save(usuario);

        cliente = new Cliente();
        cliente.setUsuario(usuario);
        cliente.setTelefono("123456789");
        cliente.setDireccion("Calle 1");
        cliente = clienteRepository.save(cliente);

        equipo = new Equipo();
        equipo.setCliente(cliente);
        equipo.setTipo("Desktop");
        equipo.setMarca("HP");
        equipo.setModelo("Pavilion");
        equipo.setSerial("DESK-" + System.nanoTime());
        equipo = equipoRepository.save(equipo);
    }

    private SolicitudRequest buildRequest() {
        SolicitudRequest request = new SolicitudRequest();
        request.setIdCliente(cliente.getIdCliente());
        request.setIdEquipo(equipo.getIdEquipo());
        request.setPrioridad(PrioridadSolicitud.ALTA);
        request.setDescripcion("No enciende");
        return request;
    }

    @Test
    void shouldCreateSolicitud() {
        var created = solicitudService.create(buildRequest());

        assertNotNull(created.getIdSolicitud());
        assertEquals(EstadoSolicitud.SOLICITUD, created.getEstado());
    }

    @Test
    void shouldGetSolicitud() {
        Long id = solicitudService.create(buildRequest()).getIdSolicitud();

        var found = solicitudService.findById(id);

        assertEquals(id, found.getIdSolicitud());
    }

    @Test
    void shouldUpdateSolicitud() {
        Long id = solicitudService.create(buildRequest()).getIdSolicitud();

        SolicitudRequest request = buildRequest();
        request.setPrioridad(PrioridadSolicitud.BAJA);
        request.setDescripcion("Cambió la falla");

        var updated = solicitudService.update(id, request);

        assertEquals(PrioridadSolicitud.BAJA, updated.getPrioridad());
        assertEquals("Cambió la falla", updated.getDescripcion());
    }

    @Test
    void shouldDeleteSolicitud() {
        Long id = solicitudService.create(buildRequest()).getIdSolicitud();

        solicitudService.delete(id);

        assertThrows(ResourceNotFoundException.class, () -> solicitudService.findById(id));
    }

    @Test
    void shouldApplyValidStateTransition() {
        Long id = solicitudService.create(buildRequest()).getIdSolicitud();

        solicitudService.updateState(id, EstadoSolicitud.PENDIENTE);

        assertEquals(EstadoSolicitud.PENDIENTE, solicitudService.findById(id).getEstado());
    }

    @Test
    void shouldRejectInvalidStateTransition() {
        Long id = solicitudService.create(buildRequest()).getIdSolicitud();

        assertThrows(IllegalArgumentException.class, () -> solicitudService.updateState(id, EstadoSolicitud.FINALIZADA));
    }

    @Test
    void shouldThrowNotFoundForUnknownSolicitud() {
        assertThrows(ResourceNotFoundException.class, () -> solicitudService.findById(999999L));
    }

    @Test
    void shouldThrowNotFoundForUnknownEquipo() {
        SolicitudRequest request = buildRequest();
        request.setIdEquipo(999999L);

        assertThrows(ResourceNotFoundException.class, () -> solicitudService.create(request));
    }
}