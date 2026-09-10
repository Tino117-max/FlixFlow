package com.fixflow.repository;

import com.fixflow.model.Asignacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AsignacionRepository extends JpaRepository<Asignacion, Long> {
    Optional<Asignacion> findBySolicitud_IdSolicitud(Long solicitudId);
    boolean existsBySolicitud_IdSolicitud(Long solicitudId);
    List<Asignacion> findByTecnico_Usuario_IdUsuario(Long idUsuario);
}
