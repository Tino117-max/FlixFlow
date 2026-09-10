package com.fixflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DiagnosticoRequest {

    @NotNull(message = "La solicitud es obligatoria")
    private Long idSolicitud;

    @NotBlank(message = "La descripción del diagnóstico es obligatoria")
    private String descripcion;

    public Long getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(Long idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
