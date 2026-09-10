package com.fixflow.controller;

import com.fixflow.dto.TecnicoRequest;
import com.fixflow.model.Tecnico;
import com.fixflow.service.TecnicoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TecnicoController {

    private final TecnicoService tecnicoService;

    public TecnicoController(TecnicoService tecnicoService) {
        this.tecnicoService = tecnicoService;
    }

    @GetMapping("/tecnicos")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
    public ResponseEntity<List<Tecnico>> getAll() {
        return ResponseEntity.ok(tecnicoService.findAll());
    }

    @GetMapping("/tecnicos/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
    public ResponseEntity<Tecnico> getById(@PathVariable Long id) {
        return ResponseEntity.ok(tecnicoService.findById(id));
    }

    @PostMapping("/tecnicos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Tecnico> create(@Valid @RequestBody TecnicoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tecnicoService.create(request));
    }

    @PutMapping("/tecnicos/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Tecnico> update(@PathVariable Long id, @Valid @RequestBody TecnicoRequest request) {
        return ResponseEntity.ok(tecnicoService.update(id, request));
    }

    @DeleteMapping("/tecnicos/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tecnicoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
