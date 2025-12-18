package ar.edu.unrn.seminario.dto;

import java.time.LocalDateTime;
import java.util.List;

public class VisitaDTO {

    private String fechaDeVisita;
    private String observacion;
    private List<String> bienesRetirados;
    private String resultado;
    private LocalDateTime fechaHora;
    private String donante;

    public VisitaDTO(String fechaDeVisita, String observacion) {
        this.fechaDeVisita = fechaDeVisita;
        this.observacion = observacion;
    }

    public VisitaDTO(LocalDateTime fechaHora, String resultado, String observacion, String donante) {
        this.fechaHora = fechaHora;
        this.observacion = observacion;
        this.resultado = resultado;
        this.donante = donante;
    }

    // Constructor completo con todos los datos para el historial
    public VisitaDTO(String fechaDeVisita, String observacion, String resultado, String donante) {
        this.fechaDeVisita = fechaDeVisita;
        this.observacion = observacion;
        this.resultado = resultado;
        this.donante = donante;
    }

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

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public LocalDateTime getFecha() {
        return fechaHora;
    }

    public String getDonante() {
        return donante;
    }
    
    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

}