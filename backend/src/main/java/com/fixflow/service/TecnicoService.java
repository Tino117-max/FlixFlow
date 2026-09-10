package com.fixflow.service;

import com.fixflow.dto.TecnicoRequest;
import com.fixflow.exception.ResourceNotFoundException;
import com.fixflow.model.Tecnico;
import com.fixflow.model.Usuario;
import com.fixflow.repository.TecnicoRepository;
import com.fixflow.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TecnicoService {

    private final TecnicoRepository tecnicoRepository;
    private final UsuarioRepository usuarioRepository;

    public TecnicoService(TecnicoRepository tecnicoRepository, UsuarioRepository usuarioRepository) {
        this.tecnicoRepository = tecnicoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Tecnico> findAll() {
        return tecnicoRepository.findAll();
    }

    public Tecnico findById(Long id) {
        return tecnicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Técnico no encontrado"));
    }

    public Tecnico create(TecnicoRequest request) {
        Usuario usuario = usuarioRepository.findById(request.getIdUsuario())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Tecnico tecnico = new Tecnico();
        tecnico.setUsuario(usuario);
        tecnico.setEspecialidad(request.getEspecialidad());
        tecnico.setTelefono(request.getTelefono());
        return tecnicoRepository.save(tecnico);
    }

    public Tecnico update(Long id, TecnicoRequest request) {
        Tecnico tecnico = tecnicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Técnico no encontrado"));

        if (request.getIdUsuario() != null) {
            Usuario usuario = usuarioRepository.findById(request.getIdUsuario())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
            tecnico.setUsuario(usuario);
        }
        tecnico.setEspecialidad(request.getEspecialidad());
        tecnico.setTelefono(request.getTelefono());
        return tecnicoRepository.save(tecnico);
    }

    public void delete(Long id) {
        Tecnico tecnico = tecnicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Técnico no encontrado"));
        tecnicoRepository.delete(tecnico);
    }
}
