package com.fixflow.controller;

import com.fixflow.dto.ReparacionRequest;
import com.fixflow.model.Reparacion;
import com.fixflow.service.ReparacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ReparacionController {

    private final ReparacionService reparacionService;

    public ReparacionController(ReparacionService reparacionService) {
        this.reparacionService = reparacionService;
    }

    @GetMapping("/reparaciones")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
    public ResponseEntity<List<Reparacion>> getAll() {
        return ResponseEntity.ok(reparacionService.findAll());
    }

    @PostMapping("/reparaciones")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
    public ResponseEntity<Reparacion> create(@Valid @RequestBody ReparacionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reparacionService.create(request));
    }

    @GetMapping("/reparaciones/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
    public ResponseEntity<Reparacion> getById(@PathVariable Long id) {
        return ResponseEntity.ok(reparacionService.findById(id));
    }

    @PutMapping("/reparaciones/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
    public ResponseEntity<Reparacion> update(@PathVariable Long id, @Valid @RequestBody ReparacionRequest request) {
        return ResponseEntity.ok(reparacionService.update(id, request));
    }

    @DeleteMapping("/reparaciones/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reparacionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
