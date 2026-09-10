package com.fixflow.service;

import com.fixflow.dto.ClienteRequest;
import com.fixflow.exception.ResourceNotFoundException;
import com.fixflow.model.Cliente;
import com.fixflow.model.RolUsuario;
import com.fixflow.model.Usuario;
import com.fixflow.repository.ClienteRepository;
import com.fixflow.repository.UsuarioRepository;
import com.fixflow.security.SecurityUtil;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;

    public ClienteService(ClienteRepository clienteRepository, UsuarioRepository usuarioRepository) {
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Cliente> findAll() {
        if (SecurityUtil.getCurrentRole() == RolUsuario.CLIENTE) {
            Long idUsuario = SecurityUtil.getCurrentUserId();
            return clienteRepository.findByUsuario_IdUsuario(idUsuario)
                    .map(List::of)
                    .orElseGet(List::of);
        }
        return clienteRepository.findAll();
    }

    public Cliente findById(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
        verificarAccesoCliente(cliente);
        return cliente;
    }

    public Cliente create(ClienteRequest request) {
        Usuario usuario = usuarioRepository.findById(request.getIdUsuario())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Cliente cliente = new Cliente();
        cliente.setUsuario(usuario);
        cliente.setTelefono(request.getTelefono());
        cliente.setDireccion(request.getDireccion());
        return clienteRepository.save(cliente);
    }

    public Cliente update(Long id, ClienteRequest request) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        if (request.getIdUsuario() != null) {
            Usuario usuario = usuarioRepository.findById(request.getIdUsuario())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
            cliente.setUsuario(usuario);
        }
        cliente.setTelefono(request.getTelefono());
        cliente.setDireccion(request.getDireccion());
        return clienteRepository.save(cliente);
    }

    public void delete(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
        clienteRepository.delete(cliente);
    }

    private void verificarAccesoCliente(Cliente cliente) {
        if (SecurityUtil.getCurrentRole() == RolUsuario.CLIENTE
                && cliente.getUsuario() != null
                && !cliente.getUsuario().getIdUsuario().equals(SecurityUtil.getCurrentUserId())) {
            throw new AccessDeniedException("No puedes acceder a información de otro cliente");
        }
    }
}
