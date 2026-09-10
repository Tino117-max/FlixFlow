package com.fixflow.repository;

import com.fixflow.model.EstadoSolicitud;
import com.fixflow.model.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {
    List<Solicitud> findByCliente_IdCliente(Long clienteId);
    List<Solicitud> findByEstado(EstadoSolicitud estado);
    List<Solicitud> findByEquipo_Cliente_IdCliente(Long clienteId);
    List<Solicitud> findByAsignacion_Tecnico_IdTecnico(Long tecnicoId);
}
