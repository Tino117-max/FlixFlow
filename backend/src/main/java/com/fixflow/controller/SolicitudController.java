package com.fixflow.controller;

import com.fixflow.dto.SolicitudRequest;
import com.fixflow.model.EstadoSolicitud;
import com.fixflow.model.Solicitud;
import com.fixflow.service.SolicitudService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SolicitudController {

    private final SolicitudService solicitudService;

    public SolicitudController(SolicitudService solicitudService) {
        this.solicitudService = solicitudService;
    }

    @GetMapping("/solicitudes")
    @PreAuthorize("hasAnyRole('ADMIN','CLIENTE','TECNICO')")
    public ResponseEntity<List<Solicitud>> getAll() {
        return ResponseEntity.ok(solicitudService.findAll());
    }

    @GetMapping("/solicitudes/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CLIENTE','TECNICO')")
    public ResponseEntity<Solicitud> getById(@PathVariable Long id) {
        return ResponseEntity.ok(solicitudService.findById(id));
    }

    @PostMapping("/solicitudes")
    @PreAuthorize("hasAnyRole('ADMIN','CLIENTE')")
    public ResponseEntity<Solicitud> create(@Valid @RequestBody SolicitudRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(solicitudService.create(request));
    }

    @PutMapping("/solicitudes/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CLIENTE','TECNICO')")
    public ResponseEntity<Solicitud> update(@PathVariable Long id, @Valid @RequestBody SolicitudRequest request) {
        return ResponseEntity.ok(solicitudService.update(id, request));
    }

    @DeleteMapping("/solicitudes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        solicitudService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/solicitudes/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
    public ResponseEntity<Solicitud> updateState(@PathVariable Long id, @RequestParam EstadoSolicitud estado) {
        return ResponseEntity.ok(solicitudService.updateState(id, estado));
    }
}
