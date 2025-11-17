package ar.edu.unrn.seminario.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.sql.Timestamp;

public class OrdenRetiroDTO {
    
	// Atributos
	private int id;
    private String estado; // cambiado de int a String
    private Date fechaCreacion;
    private List<VisitaDTO> visitas;
    private String donante;
    private String vehiculo;
    private String voluntario;

    // Constructores
    public OrdenRetiroDTO(int id, String estado, LocalDateTime fechaCreacion, List<VisitaDTO> visitas, String donante, String vehiculo, String voluntario) {
        this.id = id;
        this.estado = estado;
        this.fechaCreacion = Timestamp.valueOf(fechaCreacion); 
        this.visitas = visitas;
        this.donante = donante;
        this.vehiculo = vehiculo;
        this.voluntario = voluntario;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getEstado() {
        return estado;
    }
    
    public Date getFechaCreacion() {
        return fechaCreacion;
    }
    
    public List<VisitaDTO> getVisitas() {
        return visitas;
    }
    
    public String getDonante() {
        return donante;
    }
    
    public String getVehiculo() {
        return vehiculo;
    }
    
    public String getVoluntario() {
        return voluntario;
    }
    
    public String getDescripcion() {
        return "Donante: " + donante + ", Vehiculo: " + vehiculo + ", Voluntario: " + voluntario;
    }
    
    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public void setVisitas(List<VisitaDTO> visitas) {
        this.visitas = visitas;
    }

    public void setDonante(String donante) {
        this.donante = donante;
    }

    public void setVehiculo(String vehiculo) {
        this.vehiculo = vehiculo;
    }

    public void setVoluntario(String voluntario) {
        this.voluntario = voluntario;
    }

}