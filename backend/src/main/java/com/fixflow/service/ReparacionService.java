package com.fixflow.service;

import com.fixflow.dto.ReparacionRequest;
import com.fixflow.exception.ResourceNotFoundException;
import com.fixflow.model.EstadoSolicitud;
import com.fixflow.model.Reparacion;
import com.fixflow.model.Solicitud;
import com.fixflow.repository.ReparacionRepository;
import com.fixflow.repository.SolicitudRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReparacionService {

    private final ReparacionRepository reparacionRepository;
    private final SolicitudRepository solicitudRepository;

    public ReparacionService(ReparacionRepository reparacionRepository, SolicitudRepository solicitudRepository) {
        this.reparacionRepository = reparacionRepository;
        this.solicitudRepository = solicitudRepository;
    }

    public Reparacion findById(Long id) {
        return reparacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reparación no encontrada"));
    }

    public List<Reparacion> findAll() {
        return reparacionRepository.findAll();
    }

    public Reparacion create(ReparacionRequest request) {
        Solicitud solicitud = solicitudRepository.findById(request.getIdSolicitud())
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));

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
