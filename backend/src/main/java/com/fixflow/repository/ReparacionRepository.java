package com.fixflow.repository;

import com.fixflow.model.Reparacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReparacionRepository extends JpaRepository<Reparacion, Long> {
    Optional<Reparacion> findBySolicitud_IdSolicitud(Long solicitudId);
    boolean existsBySolicitud_IdSolicitud(Long solicitudId);
}
