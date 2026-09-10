package com.fixflow.repository;

import com.fixflow.model.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EquipoRepository extends JpaRepository<Equipo, Long> {
    boolean existsBySerial(String serial);
    List<Equipo> findByCliente_IdCliente(Long clienteId);
}
