package ar.edu.unrn.seminario.dto;

import java.util.List;
import java.time.LocalDateTime;

public class VisitaDTO {

    private String fechaDeVisita;
    private String observacion;
    private List<String> bienesRetirados;
    private String resultado;
    private LocalDateTime fechaHora;
    private String donante;

    public VisitaDTO(String fechaDeVisita, String observacion, List<String> bienesRetirados) {
        this.fechaDeVisita = fechaDeVisita;
        this.observacion = observacion;
        this.bienesRetirados = bienesRetirados;
    }

    public VisitaDTO(String fechaDeVisita, String observacion, List<String> bienesRetirados, String resultado) {
        this.fechaDeVisita = fechaDeVisita;
        this.observacion = observacion;
        this.bienesRetirados = bienesRetirados;
        this.resultado = resultado;
    }

    public VisitaDTO(LocalDateTime fechaHora, String resultado, String observacion, String donante) {
        this.fechaHora = fechaHora;
        this.observacion = observacion;
        this.resultado = resultado;
        this.donante = donante;
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

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public LocalDateTime getFecha() {
        return fechaHora;
    }

    public String getDonante() {
        return donante;
    }
}