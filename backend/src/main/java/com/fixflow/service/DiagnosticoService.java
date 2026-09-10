package com.fixflow.service;

import com.fixflow.dto.DiagnosticoRequest;
import com.fixflow.exception.ResourceNotFoundException;
import com.fixflow.model.Diagnostico;
import com.fixflow.model.EstadoSolicitud;
import com.fixflow.model.Solicitud;
import com.fixflow.repository.DiagnosticoRepository;
import com.fixflow.repository.SolicitudRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiagnosticoService {

    private final DiagnosticoRepository diagnosticoRepository;
    private final SolicitudRepository solicitudRepository;

    public DiagnosticoService(DiagnosticoRepository diagnosticoRepository, SolicitudRepository solicitudRepository) {
        this.diagnosticoRepository = diagnosticoRepository;
        this.solicitudRepository = solicitudRepository;
    }

    public Diagnostico findById(Long id) {
        return diagnosticoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Diagnóstico no encontrado"));
    }

    public List<Diagnostico> findAll() {
        return diagnosticoRepository.findAll();
    }

    public Diagnostico create(DiagnosticoRequest request) {
        Solicitud solicitud = solicitudRepository.findById(request.getIdSolicitud())
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));

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
