package com.fixflow.service;

import com.fixflow.dto.SolicitudDetalleResponse;
import com.fixflow.dto.SolicitudRequest;
import com.fixflow.exception.ResourceNotFoundException;
import com.fixflow.model.Asignacion;
import com.fixflow.model.Cliente;
import com.fixflow.model.Diagnostico;
import com.fixflow.model.Equipo;
import com.fixflow.model.EstadoSolicitud;
import com.fixflow.model.Reparacion;
import com.fixflow.model.RolUsuario;
import com.fixflow.model.Solicitud;
import com.fixflow.model.Tecnico;
import com.fixflow.repository.ClienteRepository;
import com.fixflow.repository.EquipoRepository;
import com.fixflow.repository.SolicitudRepository;
import com.fixflow.repository.TecnicoRepository;
import com.fixflow.security.SecurityUtil;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final ClienteRepository clienteRepository;
    private final EquipoRepository equipoRepository;
    private final TecnicoRepository tecnicoRepository;

    public SolicitudService(SolicitudRepository solicitudRepository,
                           ClienteRepository clienteRepository,
                           EquipoRepository equipoRepository,
                           TecnicoRepository tecnicoRepository) {
        this.solicitudRepository = solicitudRepository;
        this.clienteRepository = clienteRepository;
        this.equipoRepository = equipoRepository;
        this.tecnicoRepository = tecnicoRepository;
    }

    public List<Solicitud> findAll() {
        RolUsuario rol = SecurityUtil.getCurrentRole();
        if (rol == RolUsuario.CLIENTE) {
            Cliente cliente = obtenerClienteActual();
            return cliente != null ? solicitudRepository.findByCliente_IdCliente(cliente.getIdCliente()) : List.of();
        }
        if (rol == RolUsuario.TECNICO) {
            Tecnico tecnico = obtenerTecnicoActual();
            return tecnico != null ? solicitudRepository.findByAsignacion_Tecnico_IdTecnico(tecnico.getIdTecnico()) : List.of();
        }
        return solicitudRepository.findAll();
    }

    public Solicitud findById(Long id) {
        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));
        verificarAccesoSolicitud(solicitud);
        return solicitud;
    }

    @Transactional(readOnly = true)
    public SolicitudDetalleResponse findDetalle(Long id) {
        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));
        verificarAccesoSolicitud(solicitud);

        SolicitudDetalleResponse detalle = new SolicitudDetalleResponse();
        detalle.setIdSolicitud(solicitud.getIdSolicitud());
        detalle.setEstado(solicitud.getEstado());
        detalle.setPrioridad(solicitud.getPrioridad());
        detalle.setDescripcion(solicitud.getDescripcion());
        detalle.setFecha(solicitud.getFecha());

        Cliente cliente = solicitud.getCliente();
        if (cliente != null) {
            detalle.setClienteEmail(cliente.getUsuario() != null ? cliente.getUsuario().getEmail() : null);
            if (cliente.getUsuario() != null) {
                detalle.setClienteNombre((cliente.getUsuario().getNombre() + " " + cliente.getUsuario().getApellido()).trim());
            }
        }

        Equipo equipo = solicitud.getEquipo();
        if (equipo != null) {
            detalle.setEquipoTipo(equipo.getTipo());
            detalle.setEquipoMarca(equipo.getMarca());
            detalle.setEquipoModelo(equipo.getModelo());
            detalle.setEquipoSerial(equipo.getSerial());
            detalle.setEquipoDescripcion(equipo.getDescripcion());
        }

        Asignacion asignacion = solicitud.getAsignacion();
        if (asignacion != null) {
            detalle.setFechaAsignacion(asignacion.getFechaAsignacion());
            Tecnico tecnico = asignacion.getTecnico();
            if (tecnico != null) {
                detalle.setTecnicoEspecialidad(tecnico.getEspecialidad());
                detalle.setTecnicoTelefono(tecnico.getTelefono());
                if (tecnico.getUsuario() != null) {
                    detalle.setTecnicoNombre((tecnico.getUsuario().getNombre() + " " + tecnico.getUsuario().getApellido()).trim());
                }
            }
        }

        Diagnostico diagnostico = solicitud.getDiagnostico();
        if (diagnostico != null) {
            detalle.setDiagnosticoDescripcion(diagnostico.getDescripcion());
            detalle.setDiagnosticoFecha(diagnostico.getFecha());
        }

        Reparacion reparacion = solicitud.getReparacion();
        if (reparacion != null) {
            detalle.setReparacionSolucion(reparacion.getSolucion());
            detalle.setReparacionObservaciones(reparacion.getObservaciones());
            detalle.setReparacionFechaInicio(reparacion.getFechaInicio());
            detalle.setReparacionFechaFinalizacion(reparacion.getFechaFinalizacion());
        }

        return detalle;
    }

    private Cliente obtenerClienteActual() {
        Long idUsuario = SecurityUtil.getCurrentUserId();
        return idUsuario != null ? clienteRepository.findByUsuario_IdUsuario(idUsuario).orElse(null) : null;
    }

    private Tecnico obtenerTecnicoActual() {
        Long idUsuario = SecurityUtil.getCurrentUserId();
        return idUsuario != null ? tecnicoRepository.findByUsuario_IdUsuario(idUsuario).orElse(null) : null;
    }

    private void verificarAccesoSolicitud(Solicitud solicitud) {
        RolUsuario rol = SecurityUtil.getCurrentRole();
        if (rol == RolUsuario.CLIENTE
                && (solicitud.getCliente() == null || solicitud.getCliente().getUsuario() == null
                || !solicitud.getCliente().getUsuario().getIdUsuario().equals(SecurityUtil.getCurrentUserId()))) {
            throw new AccessDeniedException("No puedes acceder a solicitudes de otro cliente");
        }
        if (rol == RolUsuario.TECNICO
                && (solicitud.getAsignacion() == null || solicitud.getAsignacion().getTecnico() == null
                || solicitud.getAsignacion().getTecnico().getUsuario() == null
                || !solicitud.getAsignacion().getTecnico().getUsuario().getIdUsuario().equals(SecurityUtil.getCurrentUserId()))) {
            throw new AccessDeniedException("No puedes acceder a solicitudes que no te han sido asignadas");
        }
    }

    public Solicitud create(SolicitudRequest request) {
        Cliente cliente;
        if (SecurityUtil.getCurrentRole() == RolUsuario.CLIENTE) {
            cliente = obtenerClienteActual();
            if (cliente == null) {
                throw new AccessDeniedException("No tienes un perfil de cliente asociado");
            }
        } else {
            if (request.getIdCliente() == null) {
                throw new IllegalArgumentException("El cliente es obligatorio");
            }
            cliente = clienteRepository.findById(request.getIdCliente())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
        }
        Equipo equipo = equipoRepository.findById(request.getIdEquipo())
                .orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado"));

        if (SecurityUtil.getCurrentRole() == RolUsuario.CLIENTE && !equipo.getCliente().getIdCliente().equals(cliente.getIdCliente())) {
            throw new AccessDeniedException("Solo puedes registrar solicitudes sobre tus propios equipos");
        }

        Solicitud solicitud = new Solicitud();
        solicitud.setCliente(cliente);
        solicitud.setEquipo(equipo);
        solicitud.setPrioridad(request.getPrioridad());
        solicitud.setDescripcion(request.getDescripcion().trim());
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        return solicitudRepository.save(solicitud);
    }

    public Solicitud update(Long id, SolicitudRequest request) {
        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));
        verificarAccesoSolicitud(solicitud);

        if (request.getIdCliente() != null && SecurityUtil.getCurrentRole() != RolUsuario.CLIENTE) {
            Cliente cliente = clienteRepository.findById(request.getIdCliente())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
            solicitud.setCliente(cliente);
        }
        if (request.getIdEquipo() != null) {
            Equipo equipo = equipoRepository.findById(request.getIdEquipo())
                    .orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado"));
            solicitud.setEquipo(equipo);
        }
        if (request.getPrioridad() != null) {
            solicitud.setPrioridad(request.getPrioridad());
        }
        if (request.getDescripcion() != null && !request.getDescripcion().isBlank()) {
            solicitud.setDescripcion(request.getDescripcion().trim());
        }
        return solicitudRepository.save(solicitud);
    }

    public void delete(Long id) {
        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));
        solicitudRepository.delete(solicitud);
    }

    public Solicitud updateState(Long id, EstadoSolicitud nuevoEstado) {
        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));
        verificarAccesoSolicitud(solicitud);
        validarTransicionEstado(solicitud.getEstado(), nuevoEstado);
        solicitud.setEstado(nuevoEstado);

        if (nuevoEstado == EstadoSolicitud.FINALIZADA && solicitud.getReparacion() != null
                && solicitud.getReparacion().getFechaFinalizacion() == null) {
            solicitud.getReparacion().setFechaFinalizacion(LocalDateTime.now());
        }

        return solicitudRepository.save(solicitud);
    }

    private void validarTransicionEstado(EstadoSolicitud actual, EstadoSolicitud nuevo) {
        if (actual == EstadoSolicitud.SOLICITUD && nuevo == EstadoSolicitud.PENDIENTE) return;
        if (actual == EstadoSolicitud.ASIGNADA && nuevo == EstadoSolicitud.EN_DIAGNOSTICO) return;
        if (actual == EstadoSolicitud.EN_DIAGNOSTICO && nuevo == EstadoSolicitud.EN_REPARACION) return;
        if (actual == EstadoSolicitud.EN_REPARACION && nuevo == EstadoSolicitud.FINALIZADA) return;
        if (actual != EstadoSolicitud.FINALIZADA && actual != EstadoSolicitud.CANCELADA
                && nuevo == EstadoSolicitud.CANCELADA) return;
        throw new IllegalArgumentException("Transición de estado inválida. ASIGNADA solo se obtiene al asignar un técnico.");
    }
}
