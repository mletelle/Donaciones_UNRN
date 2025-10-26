package ar.edu.unrn.seminario.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ar.edu.unrn.seminario.modelo.Visita;
import ar.edu.unrn.seminario.dto.VisitaDTO;

public class OrdenRetiroDTO {
    private int id;
    private int estado;
    private Date fechaCreacion;
    private List<VisitaDTO> visitas;

    public OrdenRetiroDTO(int id, int estado, Date fechaCreacion) {
        this.id = id;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
        this.visitas = new ArrayList<>(); 
    }

    public OrdenRetiroDTO(int id, int estado, Date fechaCreacion, List<VisitaDTO> visitas) {
        this.id = id;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
        this.visitas = visitas;
    }

    public OrdenRetiroDTO(int id, int estado, Date fechaCreacion, List<Visita> visitas) {
        this.id = id;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
        this.visitas = new ArrayList<>();
        for (Visita visita : visitas) {
            this.visitas.add(new VisitaDTO(visita.obtenerFechaFormateada(), visita.obtenerObservacion(), visita.obtenerBienes()));
        }
    }

    public OrdenRetiroDTO(int id, int estado, Date fechaCreacion, List<VisitaDTO> visitas, boolean convertToDTO) {
        this.id = id;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
        this.visitas = visitas;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public List<VisitaDTO> getVisitas() {
        return visitas;
    }

    public void setVisitas(List<VisitaDTO> visitas) {
        this.visitas = visitas;
    }
}