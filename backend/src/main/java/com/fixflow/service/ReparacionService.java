package com.fixflow.service;

import com.fixflow.dto.ReparacionRequest;
import com.fixflow.exception.ResourceNotFoundException;
import com.fixflow.model.EstadoSolicitud;
import com.fixflow.model.Reparacion;
import com.fixflow.model.RolUsuario;
import com.fixflow.model.Solicitud;
import com.fixflow.repository.AsignacionRepository;
import com.fixflow.repository.ReparacionRepository;
import com.fixflow.repository.SolicitudRepository;
import com.fixflow.security.SecurityUtil;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReparacionService {

    private final ReparacionRepository reparacionRepository;
    private final SolicitudRepository solicitudRepository;
    private final AsignacionRepository asignacionRepository;

    public ReparacionService(ReparacionRepository reparacionRepository,
                             SolicitudRepository solicitudRepository,
                             AsignacionRepository asignacionRepository) {
        this.reparacionRepository = reparacionRepository;
        this.solicitudRepository = solicitudRepository;
        this.asignacionRepository = asignacionRepository;
    }

    public Reparacion findById(Long id) {
        return reparacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reparación no encontrada"));
    }

    public List<Reparacion> findAll() {
        List<Reparacion> reparaciones = reparacionRepository.findAll();
        if (SecurityUtil.getCurrentRole() == RolUsuario.TECNICO) {
            return reparaciones.stream()
                    .filter(r -> r.getSolicitud() != null && estaAsignadaAlTecnicoActual(r.getSolicitud()))
                    .toList();
        }
        return reparaciones;
    }

    private boolean estaAsignadaAlTecnicoActual(Solicitud solicitud) {
        return asignacionRepository.findBySolicitud_IdSolicitud(solicitud.getIdSolicitud())
                .map(a -> a.getTecnico() != null && a.getTecnico().getUsuario() != null
                        && a.getTecnico().getUsuario().getIdUsuario().equals(SecurityUtil.getCurrentUserId()))
                .orElse(false);
    }

    public Reparacion create(ReparacionRequest request) {
        Solicitud solicitud = solicitudRepository.findById(request.getIdSolicitud())
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));

        if (SecurityUtil.getCurrentRole() == RolUsuario.TECNICO && !estaAsignadaAlTecnicoActual(solicitud)) {
            throw new AccessDeniedException("La solicitud no está asignada a ti");
        }
        if (solicitud.getEstado() != EstadoSolicitud.EN_DIAGNOSTICO
                && solicitud.getEstado() != EstadoSolicitud.EN_REPARACION) {
            throw new IllegalArgumentException("La solicitud debe tener un diagnóstico registrado para registrar la reparación");
        }
        if (reparacionRepository.existsBySolicitud_IdSolicitud(request.getIdSolicitud())) {
            throw new IllegalArgumentException("La solicitud ya tiene una reparación");
        }

        Reparacion reparacion = new Reparacion();
        reparacion.setSolicitud(solicitud);
        reparacion.setSolucion(request.getSolucion());
        reparacion.setObservaciones(request.getObservaciones());
        solicitud.setEstado(EstadoSolicitud.EN_REPARACION);
        solicitudRepository.save(solicitud);
        return reparacionRepository.save(reparacion);
    }

    public Reparacion update(Long id, ReparacionRequest request) {
        Reparacion reparacion = reparacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reparación no encontrada"));
        reparacion.setSolucion(request.getSolucion());
        reparacion.setObservaciones(request.getObservaciones());
        return reparacionRepository.save(reparacion);
    }

    public void delete(Long id) {
        Reparacion reparacion = reparacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reparación no encontrada"));
        reparacionRepository.delete(reparacion);
    }
}
