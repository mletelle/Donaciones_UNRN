package ar.edu.unrn.seminario.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ar.edu.unrn.seminario.modelo.Bien;

public class VisitaDTO {

    private String fechaDeVisita;
    private String observacion;
    private List<String> bienesRetirados;

    public VisitaDTO(String fechaDeVisita, String observacion, List<String> bienesRetirados) {
        this.fechaDeVisita = fechaDeVisita;
        this.observacion = observacion;
        this.bienesRetirados = bienesRetirados;
    }

    public VisitaDTO(String fechaDeVisita, String observacion, List<Bien> bienes) {
        this.fechaDeVisita = fechaDeVisita;
        this.observacion = observacion;
        this.bienesRetirados = new ArrayList<>();
        for (Bien bien : bienes) {
            this.bienesRetirados.add(bien.toString()); 
        }
    }

    public VisitaDTO(String fecha, String observacion, List<Bien> bienes) {
        this.fechaDeVisita = fecha;
        this.observacion = observacion;
        this.bienesRetirados = new ArrayList<>();
        for (Bien bien : bienes) {
            this.bienesRetirados.add(bien.toString()); 
        }
    }

    public VisitaDTO(String fecha, String observacion, List<Bien> bienes, boolean fromBienList) {
        this.fechaDeVisita = fecha;
        this.observacion = observacion;
        this.bienesRetirados = new ArrayList<>();
        if (fromBienList) {
            for (Bien bien : bienes) {
                this.bienesRetirados.add(bien.toString());
            }
        }
    }

    public String getFechaDeVisita() {
        return fechaDeVisita;
    }

    public void setFechaDeVisita(String fechaDeVisita) {
        this.fechaDeVisita = fechaDeVisita;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public List<String> getBienesRetirados() {
        return bienesRetirados;
    }

    public void setBienesRetirados(List<String> bienesRetirados) {
        this.bienesRetirados = bienesRetirados;
    }

    public VisitaDTO(String fecha, String observacion, List<Bien> bienes) {
        this.fechaDeVisita = fecha;
        this.observacion = observacion;
        this.bienesRetirados = new ArrayList<>();
        for (Bien bien : bienes) {
            this.bienesRetirados.add(bien.toString()); 
        }
    }
}