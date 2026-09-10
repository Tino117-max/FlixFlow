package com.fixflow.repository;

import com.fixflow.model.Tecnico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TecnicoRepository extends JpaRepository<Tecnico, Long> {
    Optional<Tecnico> findByUsuario_IdUsuario(Long idUsuario);
}
