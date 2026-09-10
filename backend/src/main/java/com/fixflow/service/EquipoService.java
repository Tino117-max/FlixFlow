package com.fixflow.service;

import com.fixflow.dto.EquipoRequest;
import com.fixflow.exception.ResourceNotFoundException;
import com.fixflow.model.Cliente;
import com.fixflow.model.Equipo;
import com.fixflow.model.RolUsuario;
import com.fixflow.repository.ClienteRepository;
import com.fixflow.repository.EquipoRepository;
import com.fixflow.security.SecurityUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EquipoService {

    private final EquipoRepository equipoRepository;
    private final ClienteRepository clienteRepository;

    public EquipoService(EquipoRepository equipoRepository, ClienteRepository clienteRepository) {
        this.equipoRepository = equipoRepository;
        this.clienteRepository = clienteRepository;
    }

    public List<Equipo> findAll() {
        if (SecurityUtil.getCurrentRole() == RolUsuario.CLIENTE) {
            Cliente cliente = obtenerClienteActual();
            return cliente != null ? equipoRepository.findByCliente_IdCliente(cliente.getIdCliente()) : List.of();
        }
        return equipoRepository.findAll();
    }

    public Equipo findById(Long id) {
        Equipo equipo = equipoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado"));
        verificarAccesoCliente(equipo.getCliente());
        return equipo;
    }

    private Cliente obtenerClienteActual() {
        Long idUsuario = SecurityUtil.getCurrentUserId();
        return idUsuario != null ? clienteRepository.findByUsuario_IdUsuario(idUsuario).orElse(null) : null;
    }

    private void verificarAccesoCliente(Cliente cliente) {
        if (SecurityUtil.getCurrentRole() == RolUsuario.CLIENTE
                && (cliente == null || cliente.getUsuario() == null
                || !cliente.getUsuario().getIdUsuario().equals(SecurityUtil.getCurrentUserId()))) {
            throw new AccessDeniedException("No puedes acceder a equipos de otro cliente");
        }
    }

    public Equipo create(EquipoRequest request) {
        if (equipoRepository.existsBySerial(request.getSerial().trim())) {
            throw new DataIntegrityViolationException("El serial ya existe");
        }

        Cliente cliente;
        if (SecurityUtil.getCurrentRole() == RolUsuario.CLIENTE) {
            cliente = obtenerClienteActual();
            if (cliente == null) {
                throw new AccessDeniedException("No tienes un perfil de cliente asociado");
            }
        } else {
            cliente = clienteRepository.findById(request.getIdCliente())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
        }

        Equipo equipo = new Equipo();
        equipo.setCliente(cliente);
        equipo.setTipo(request.getTipo().trim());
        equipo.setMarca(request.getMarca().trim());
        equipo.setModelo(request.getModelo().trim());
        equipo.setSerial(request.getSerial().trim());
        equipo.setDescripcion(request.getDescripcion());
        return equipoRepository.save(equipo);
    }

    public Equipo update(Long id, EquipoRequest request) {
        Equipo equipo = equipoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado"));
        verificarAccesoCliente(equipo.getCliente());

        if (request.getIdCliente() != null && SecurityUtil.getCurrentRole() != RolUsuario.CLIENTE) {
            Cliente cliente = clienteRepository.findById(request.getIdCliente())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
            equipo.setCliente(cliente);
        }
        if (request.getSerial() != null && !request.getSerial().isBlank() && !request.getSerial().equals(equipo.getSerial())
                && equipoRepository.existsBySerial(request.getSerial().trim())) {
            throw new DataIntegrityViolationException("El serial ya existe");
        }

        equipo.setTipo(request.getTipo().trim());
        equipo.setMarca(request.getMarca().trim());
        equipo.setModelo(request.getModelo().trim());
        equipo.setSerial(request.getSerial().trim());
        equipo.setDescripcion(request.getDescripcion());
        return equipoRepository.save(equipo);
    }

    public void delete(Long id) {
        Equipo equipo = equipoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado"));
        equipoRepository.delete(equipo);
    }
}
