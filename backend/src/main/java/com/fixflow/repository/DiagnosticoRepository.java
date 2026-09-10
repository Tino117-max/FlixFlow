package com.fixflow.repository;

import com.fixflow.model.Diagnostico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiagnosticoRepository extends JpaRepository<Diagnostico, Long> {
    Optional<Diagnostico> findBySolicitud_IdSolicitud(Long solicitudId);
    boolean existsBySolicitud_IdSolicitud(Long solicitudId);
}
