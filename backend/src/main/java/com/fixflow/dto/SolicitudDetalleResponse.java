package com.fixflow.dto;

import com.fixflow.model.EstadoSolicitud;
import com.fixflow.model.PrioridadSolicitud;

import java.time.LocalDateTime;

public class SolicitudDetalleResponse {

    private Long idSolicitud;
    private EstadoSolicitud estado;
    private PrioridadSolicitud prioridad;
    private String descripcion;
    private LocalDateTime fecha;

    private String clienteNombre;
    private String clienteEmail;

    private String equipoTipo;
    private String equipoMarca;
    private String equipoModelo;
    private String equipoSerial;
    private String equipoDescripcion;

    private String tecnicoNombre;
    private String tecnicoEspecialidad;
    private String tecnicoTelefono;
    private LocalDateTime fechaAsignacion;

    private String diagnosticoDescripcion;
    private LocalDateTime diagnosticoFecha;

    private String reparacionSolucion;
    private String reparacionObservaciones;
    private LocalDateTime reparacionFechaInicio;
    private LocalDateTime reparacionFechaFinalizacion;

    public Long getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(Long idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public EstadoSolicitud getEstado() {
        return estado;
    }

    public void setEstado(EstadoSolicitud estado) {
        this.estado = estado;
    }

    public PrioridadSolicitud getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(PrioridadSolicitud prioridad) {
        this.prioridad = prioridad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public String getClienteEmail() {
        return clienteEmail;
    }

    public void setClienteEmail(String clienteEmail) {
        this.clienteEmail = clienteEmail;
    }

    public String getEquipoTipo() {
        return equipoTipo;
    }

    public void setEquipoTipo(String equipoTipo) {
        this.equipoTipo = equipoTipo;
    }

    public String getEquipoMarca() {
        return equipoMarca;
    }

    public void setEquipoMarca(String equipoMarca) {
        this.equipoMarca = equipoMarca;
    }

    public String getEquipoModelo() {
        return equipoModelo;
    }

    public void setEquipoModelo(String equipoModelo) {
        this.equipoModelo = equipoModelo;
    }

    public String getEquipoSerial() {
        return equipoSerial;
    }

    public void setEquipoSerial(String equipoSerial) {
        this.equipoSerial = equipoSerial;
    }

    public String getEquipoDescripcion() {
        return equipoDescripcion;
    }

    public void setEquipoDescripcion(String equipoDescripcion) {
        this.equipoDescripcion = equipoDescripcion;
    }

    public String getTecnicoNombre() {
        return tecnicoNombre;
    }

    public void setTecnicoNombre(String tecnicoNombre) {
        this.tecnicoNombre = tecnicoNombre;
    }

    public String getTecnicoEspecialidad() {
        return tecnicoEspecialidad;
    }

    public void setTecnicoEspecialidad(String tecnicoEspecialidad) {
        this.tecnicoEspecialidad = tecnicoEspecialidad;
    }

    public String getTecnicoTelefono() {
        return tecnicoTelefono;
    }

    public void setTecnicoTelefono(String tecnicoTelefono) {
        this.tecnicoTelefono = tecnicoTelefono;
    }

    public LocalDateTime getFechaAsignacion() {
        return fechaAsignacion;
    }

    public void setFechaAsignacion(LocalDateTime fechaAsignacion) {
        this.fechaAsignacion = fechaAsignacion;
    }

    public String getDiagnosticoDescripcion() {
        return diagnosticoDescripcion;
    }

    public void setDiagnosticoDescripcion(String diagnosticoDescripcion) {
        this.diagnosticoDescripcion = diagnosticoDescripcion;
    }

    public LocalDateTime getDiagnosticoFecha() {
        return diagnosticoFecha;
    }

    public void setDiagnosticoFecha(LocalDateTime diagnosticoFecha) {
        this.diagnosticoFecha = diagnosticoFecha;
    }

    public String getReparacionSolucion() {
        return reparacionSolucion;
    }

    public void setReparacionSolucion(String reparacionSolucion) {
        this.reparacionSolucion = reparacionSolucion;
    }

    public String getReparacionObservaciones() {
        return reparacionObservaciones;
    }

    public void setReparacionObservaciones(String reparacionObservaciones) {
        this.reparacionObservaciones = reparacionObservaciones;
    }

    public LocalDateTime getReparacionFechaInicio() {
        return reparacionFechaInicio;
    }

    public void setReparacionFechaInicio(LocalDateTime reparacionFechaInicio) {
        this.reparacionFechaInicio = reparacionFechaInicio;
    }

    public LocalDateTime getReparacionFechaFinalizacion() {
        return reparacionFechaFinalizacion;
    }

    public void setReparacionFechaFinalizacion(LocalDateTime reparacionFechaFinalizacion) {
        this.reparacionFechaFinalizacion = reparacionFechaFinalizacion;
    }
}
