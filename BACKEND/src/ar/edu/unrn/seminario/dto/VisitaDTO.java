package ar.edu.unrn.seminario.dto;

import java.util.List;
import java.time.LocalDateTime;

public class VisitaDTO {

    private String fechaDeVisita;
    private String observacion;
    private List<String> bienesRetirados;
    private boolean estado;
    private LocalDateTime fechaHora;

    public VisitaDTO(String fechaDeVisita, String observacion, List<String> bienesRetirados) {
        this.fechaDeVisita = fechaDeVisita;
        this.observacion = observacion;
        this.bienesRetirados = bienesRetirados;
    }

    public VisitaDTO(String fechaDeVisita, String observacion, List<String> bienesRetirados, boolean estado) {
        this.fechaDeVisita = fechaDeVisita;
        this.observacion = observacion;
        this.bienesRetirados = bienesRetirados;
        this.estado = estado;
    }

    public VisitaDTO(LocalDateTime fechaHora, String resultado, String observacion) {
        this.fechaHora = fechaHora;
        this.observacion = observacion;
        this.estado = "Recolección Exitosa".equals(resultado);
    }

    // Getters
    public String getFechaDeVisita() {
        return fechaDeVisita;
    }

    public String getObservacion() {
        return observacion;
    }

    public List<String> getBienesRetirados() {
        return bienesRetirados;
    }

    public boolean getEstado() {
        return estado;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }
}