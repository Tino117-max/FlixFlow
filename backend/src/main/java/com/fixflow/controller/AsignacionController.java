package com.fixflow.controller;

import com.fixflow.dto.AsignacionRequest;
import com.fixflow.model.Asignacion;
import com.fixflow.service.AsignacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AsignacionController {

    private final AsignacionService asignacionService;

    public AsignacionController(AsignacionService asignacionService) {
        this.asignacionService = asignacionService;
    }

    @PostMapping("/asignaciones")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Asignacion> create(@Valid @RequestBody AsignacionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(asignacionService.create(request));
    }

    @GetMapping("/asignaciones")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Asignacion>> getAll() {
        return ResponseEntity.ok(asignacionService.findAll());
    }

    @GetMapping("/asignaciones/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Asignacion> getById(@PathVariable Long id) {
        return ResponseEntity.ok(asignacionService.findById(id));
    }

    @PutMapping("/asignaciones/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Asignacion> update(@PathVariable Long id, @Valid @RequestBody AsignacionRequest request) {
        return ResponseEntity.ok(asignacionService.update(id, request));
    }

    @DeleteMapping("/asignaciones/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        asignacionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
