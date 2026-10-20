package com.fixflow.service;

import com.fixflow.dto.DiagnosticoRequest;
import com.fixflow.exception.ResourceNotFoundException;
import com.fixflow.model.Diagnostico;
import com.fixflow.model.EstadoSolicitud;
import com.fixflow.model.RolUsuario;
import com.fixflow.model.Solicitud;
import com.fixflow.repository.AsignacionRepository;
import com.fixflow.repository.DiagnosticoRepository;
import com.fixflow.repository.SolicitudRepository;
import com.fixflow.security.SecurityUtil;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiagnosticoService {

    private final DiagnosticoRepository diagnosticoRepository;
    private final SolicitudRepository solicitudRepository;
    private final AsignacionRepository asignacionRepository;

    public DiagnosticoService(DiagnosticoRepository diagnosticoRepository,
                              SolicitudRepository solicitudRepository,
                              AsignacionRepository asignacionRepository) {
        this.diagnosticoRepository = diagnosticoRepository;
        this.solicitudRepository = solicitudRepository;
        this.asignacionRepository = asignacionRepository;
    }

    public Diagnostico findById(Long id) {
        return diagnosticoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Diagnóstico no encontrado"));
    }

    public List<Diagnostico> findAll() {
        List<Diagnostico> diagnosticos = diagnosticoRepository.findAll();
        if (SecurityUtil.getCurrentRole() == RolUsuario.TECNICO) {
            return diagnosticos.stream()
                    .filter(d -> d.getSolicitud() != null && estaAsignadaAlTecnicoActual(d.getSolicitud()))
                    .toList();
        }
        return diagnosticos;
    }

    private boolean estaAsignadaAlTecnicoActual(Solicitud solicitud) {
        return asignacionRepository.findBySolicitud_IdSolicitud(solicitud.getIdSolicitud())
                .map(a -> a.getTecnico() != null && a.getTecnico().getUsuario() != null
                        && a.getTecnico().getUsuario().getIdUsuario().equals(SecurityUtil.getCurrentUserId()))
                .orElse(false);
    }

    private void verificarTecnicoAsignado(Solicitud solicitud) {
        if (SecurityUtil.getCurrentRole() == RolUsuario.TECNICO && !estaAsignadaAlTecnicoActual(solicitud)) {
            throw new AccessDeniedException("La solicitud no está asignada a ti");
        }
    }

    public Diagnostico create(DiagnosticoRequest request) {
        Solicitud solicitud = solicitudRepository.findById(request.getIdSolicitud())
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));

        verificarTecnicoAsignado(solicitud);
        if (solicitud.getEstado() != EstadoSolicitud.ASIGNADA) {
            throw new IllegalArgumentException("La solicitud debe estar ASIGNADA para registrar un diagnóstico");
        }
        if (diagnosticoRepository.existsBySolicitud_IdSolicitud(request.getIdSolicitud())) {
            throw new IllegalArgumentException("La solicitud ya tiene un diagnóstico");
        }

        Diagnostico diagnostico = new Diagnostico();
        diagnostico.setSolicitud(solicitud);
        diagnostico.setDescripcion(request.getDescripcion());
        solicitud.setEstado(EstadoSolicitud.EN_DIAGNOSTICO);
        solicitudRepository.save(solicitud);
        return diagnosticoRepository.save(diagnostico);
    }

    public Diagnostico update(Long id, DiagnosticoRequest request) {
        Diagnostico diagnostico = diagnosticoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Diagnóstico no encontrado"));
        diagnostico.setDescripcion(request.getDescripcion());
        return diagnosticoRepository.save(diagnostico);
    }

    public void delete(Long id) {
        Diagnostico diagnostico = diagnosticoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Diagnóstico no encontrado"));
        diagnosticoRepository.delete(diagnostico);
    }
}
