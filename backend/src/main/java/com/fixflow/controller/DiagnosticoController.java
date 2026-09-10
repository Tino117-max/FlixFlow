package com.fixflow.controller;

import com.fixflow.dto.DiagnosticoRequest;
import com.fixflow.model.Diagnostico;
import com.fixflow.service.DiagnosticoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DiagnosticoController {

    private final DiagnosticoService diagnosticoService;

    public DiagnosticoController(DiagnosticoService diagnosticoService) {
        this.diagnosticoService = diagnosticoService;
    }

    @GetMapping("/diagnosticos")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
    public ResponseEntity<List<Diagnostico>> getAll() {
        return ResponseEntity.ok(diagnosticoService.findAll());
    }

    @PostMapping("/diagnosticos")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
    public ResponseEntity<Diagnostico> create(@Valid @RequestBody DiagnosticoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(diagnosticoService.create(request));
    }

    @GetMapping("/diagnosticos/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
    public ResponseEntity<Diagnostico> getById(@PathVariable Long id) {
        return ResponseEntity.ok(diagnosticoService.findById(id));
    }

    @PutMapping("/diagnosticos/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
    public ResponseEntity<Diagnostico> update(@PathVariable Long id, @Valid @RequestBody DiagnosticoRequest request) {
        return ResponseEntity.ok(diagnosticoService.update(id, request));
    }

    @DeleteMapping("/diagnosticos/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        diagnosticoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
