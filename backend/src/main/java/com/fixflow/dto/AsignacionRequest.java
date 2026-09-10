package com.fixflow.dto;

import jakarta.validation.constraints.NotNull;

public class AsignacionRequest {

    @NotNull(message = "La solicitud es obligatoria")
    private Long idSolicitud;

    @NotNull(message = "El técnico es obligatorio")
    private Long idTecnico;

    public Long getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(Long idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public Long getIdTecnico() {
        return idTecnico;
    }

    public void setIdTecnico(Long idTecnico) {
        this.idTecnico = idTecnico;
    }
}
