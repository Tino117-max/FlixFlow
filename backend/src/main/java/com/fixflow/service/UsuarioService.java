package com.fixflow.service;

import com.fixflow.dto.RegistroRequest;
import com.fixflow.dto.UsuarioRequest;
import com.fixflow.dto.UsuarioResponse;
import com.fixflow.exception.ResourceNotFoundException;
import com.fixflow.model.Cliente;
import com.fixflow.model.EstadoUsuario;
import com.fixflow.model.RolUsuario;
import com.fixflow.model.Tecnico;
import com.fixflow.model.Usuario;
import com.fixflow.repository.ClienteRepository;
import com.fixflow.repository.AsignacionRepository;
import com.fixflow.repository.EquipoRepository;
import com.fixflow.repository.SolicitudRepository;
import com.fixflow.repository.TecnicoRepository;
import com.fixflow.repository.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final TecnicoRepository tecnicoRepository;
    private final SolicitudRepository solicitudRepository;
    private final EquipoRepository equipoRepository;
    private final AsignacionRepository asignacionRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          ClienteRepository clienteRepository,
                          TecnicoRepository tecnicoRepository,
                          SolicitudRepository solicitudRepository,
                          EquipoRepository equipoRepository,
                          AsignacionRepository asignacionRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.tecnicoRepository = tecnicoRepository;
        this.solicitudRepository = solicitudRepository;
        this.equipoRepository = equipoRepository;
        this.asignacionRepository = asignacionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UsuarioResponse> findAll() {
        return usuarioRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public UsuarioResponse findById(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return toResponse(usuario);
    }

    public Usuario findEntityById(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    @Transactional
    public Usuario registrarCliente(RegistroRequest request) {
        String email = request.getEmail().trim();
        if (usuarioRepository.existsByEmail(email)) {
            throw new DataIntegrityViolationException("El email ya existe");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre().trim());
        usuario.setApellido(request.getApellido().trim());
        usuario.setEmail(email);
        usuario.setRol(RolUsuario.CLIENTE);
        usuario.setEstado(EstadoUsuario.ACTIVO);
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));

        Usuario nuevo = usuarioRepository.save(usuario);

        Cliente cliente = new Cliente();
        cliente.setUsuario(nuevo);
        cliente.setTelefono(request.getTelefono().trim());
        cliente.setDireccion(request.getDireccion().trim());
        clienteRepository.save(cliente);

        return nuevo;
    }

    @Transactional
    public UsuarioResponse create(UsuarioRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail().trim())) {
            throw new DataIntegrityViolationException("El email ya existe");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria al crear un usuario");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre().trim());
        usuario.setApellido(request.getApellido().trim());
        usuario.setEmail(request.getEmail().trim());
        usuario.setRol(request.getRol());
        usuario.setEstado(request.getEstado() != null ? request.getEstado() : EstadoUsuario.ACTIVO);
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));

        Usuario nuevo = usuarioRepository.save(usuario);
        sincronizarPerfilTecnico(nuevo, request);
        return toResponse(nuevo);
    }

    @Transactional
    public UsuarioResponse update(Long id, UsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!usuario.getEmail().equalsIgnoreCase(request.getEmail().trim()) && usuarioRepository.existsByEmail(request.getEmail().trim())) {
            throw new DataIntegrityViolationException("El email ya existe");
        }

        usuario.setNombre(request.getNombre().trim());
        usuario.setApellido(request.getApellido().trim());
        usuario.setEmail(request.getEmail().trim());
        usuario.setRol(request.getRol());
        if (request.getEstado() != null) {
            usuario.setEstado(request.getEstado());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        Usuario actualizado = usuarioRepository.save(usuario);
        sincronizarPerfilTecnico(actualizado, request);
        return toResponse(actualizado);
    }

    private void sincronizarPerfilTecnico(Usuario usuario, UsuarioRequest request) {
        if (usuario.getRol() != RolUsuario.TECNICO) {
            return;
        }

        Tecnico tecnico = tecnicoRepository.findByUsuario_IdUsuario(usuario.getIdUsuario()).orElse(null);
        boolean nuevo = tecnico == null;
        if (nuevo) {
            tecnico = new Tecnico();
            tecnico.setUsuario(usuario);
        }

        String especialidad = request.getEspecialidad();
        if (especialidad != null && !especialidad.isBlank()) {
            tecnico.setEspecialidad(especialidad.trim());
        } else if (nuevo) {
            throw new IllegalArgumentException("La especialidad es obligatoria para un técnico");
        }

        String telefono = request.getTelefono();
        if (telefono != null && !telefono.isBlank()) {
            tecnico.setTelefono(telefono.trim());
        } else if (nuevo) {
            throw new IllegalArgumentException("El teléfono es obligatorio para un técnico");
        }

        tecnicoRepository.save(tecnico);
    }

    @Transactional
    public void desactivar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        usuario.setEstado(EstadoUsuario.INACTIVO);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void eliminar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (usuario.getRol() == RolUsuario.CLIENTE) {
            clienteRepository.findByUsuario_IdUsuario(id).ifPresent(cliente -> {
                Long idCliente = cliente.getIdCliente();
                solicitudRepository.deleteByEquipo_Cliente_IdCliente(idCliente);
                equipoRepository.deleteByCliente_IdCliente(idCliente);
                clienteRepository.delete(cliente);
            });
        } else if (usuario.getRol() == RolUsuario.TECNICO) {
            tecnicoRepository.findByUsuario_IdUsuario(id).ifPresent(tecnico -> {
                asignacionRepository.deleteAllInBatch(asignacionRepository.findByTecnico_IdTecnico(tecnico.getIdTecnico()));
                asignacionRepository.flush();
                tecnicoRepository.delete(tecnico);
                tecnicoRepository.flush();
            });
        }

        usuarioRepository.delete(usuario);
    }

    public UsuarioResponse toResponse(Usuario usuario) {
        UsuarioResponse response = new UsuarioResponse();
        response.setIdUsuario(usuario.getIdUsuario());
        response.setNombre(usuario.getNombre());
        response.setApellido(usuario.getApellido());
        response.setEmail(usuario.getEmail());
        response.setRol(usuario.getRol());
        response.setEstado(usuario.getEstado());
        response.setFechaRegistro(usuario.getFechaRegistro());
        return response;
    }
}
