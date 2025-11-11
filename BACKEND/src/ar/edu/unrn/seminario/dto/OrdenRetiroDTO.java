package ar.edu.unrn.seminario.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.sql.Timestamp;

public class OrdenRetiroDTO {
    private int id;
    private String estado; // cambiado de int a String
    private Date fechaCreacion;
    private List<VisitaDTO> visitas;
    private String donante;
    private String vehiculo;
    private String voluntario;


    public OrdenRetiroDTO(int id, String estado, LocalDateTime fechaCreacion, List<VisitaDTO> visitas, String donante, String vehiculo, String voluntario) {
        this.id = id;
        this.estado = estado;
        this.fechaCreacion = Timestamp.valueOf(fechaCreacion); //revisar
        this.visitas = visitas;
        this.donante = donante;
        this.vehiculo = vehiculo;
        this.voluntario = voluntario;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
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

    public String getDonante() {
        return donante;
    }

    public void setDonante(String donante) {
        this.donante = donante;
    }

    public String getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(String vehiculo) {
        this.vehiculo = vehiculo;
    }

    public String getVoluntario() {
        return voluntario;
    }

    public void setVoluntario(String voluntario) {
        this.voluntario = voluntario;
    }

    public String getDescripcion() {
        return "Donante: " + donante + ", Vehiculo: " + vehiculo + ", Voluntario: " + voluntario;
    }

    // el metod getEstadoTexto() eliminado, ahora getEstado() devuelve String directamente
}