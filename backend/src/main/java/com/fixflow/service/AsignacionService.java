package com.fixflow.service;

import com.fixflow.dto.AsignacionRequest;
import com.fixflow.exception.ResourceNotFoundException;
import com.fixflow.model.Asignacion;
import com.fixflow.model.EstadoSolicitud;
import com.fixflow.model.Solicitud;
import com.fixflow.model.Tecnico;
import com.fixflow.repository.AsignacionRepository;
import com.fixflow.repository.SolicitudRepository;
import com.fixflow.repository.TecnicoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AsignacionService {

    private final AsignacionRepository asignacionRepository;
    private final SolicitudRepository solicitudRepository;
    private final TecnicoRepository tecnicoRepository;

    public AsignacionService(AsignacionRepository asignacionRepository,
                            SolicitudRepository solicitudRepository,
                            TecnicoRepository tecnicoRepository) {
        this.asignacionRepository = asignacionRepository;
        this.solicitudRepository = solicitudRepository;
        this.tecnicoRepository = tecnicoRepository;
    }

    public List<Asignacion> findAll() {
        return asignacionRepository.findAll();
    }

    public Asignacion findById(Long id) {
        return asignacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación no encontrada"));
    }

    public Asignacion create(AsignacionRequest request) {
        Solicitud solicitud = solicitudRepository.findById(request.getIdSolicitud())
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));
        Tecnico tecnico = tecnicoRepository.findById(request.getIdTecnico())
                .orElseThrow(() -> new ResourceNotFoundException("Técnico no encontrado"));

        if (asignacionRepository.existsBySolicitud_IdSolicitud(request.getIdSolicitud())) {
            throw new IllegalArgumentException("La solicitud ya tiene una asignación");
        }

        Asignacion asignacion = new Asignacion();
        asignacion.setSolicitud(solicitud);
        asignacion.setTecnico(tecnico);
        solicitud.setEstado(EstadoSolicitud.ASIGNADA);
        solicitudRepository.save(solicitud);
        return asignacionRepository.save(asignacion);
    }

    public Asignacion update(Long id, AsignacionRequest request) {
        Asignacion asignacion = asignacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación no encontrada"));

        Tecnico tecnico = tecnicoRepository.findById(request.getIdTecnico())
                .orElseThrow(() -> new ResourceNotFoundException("Técnico no encontrado"));
        asignacion.setTecnico(tecnico);
        return asignacionRepository.save(asignacion);
    }

    public void delete(Long id) {
        Asignacion asignacion = asignacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación no encontrada"));
        asignacionRepository.delete(asignacion);
    }
}
