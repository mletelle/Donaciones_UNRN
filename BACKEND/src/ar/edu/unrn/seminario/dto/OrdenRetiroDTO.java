package ar.edu.unrn.seminario.dto;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class OrdenRetiroDTO {
    
	// Atributos
	private int id;
    private String estado; // cambiado de int a String
    private Date fechaCreacion;
    private List<VisitaDTO> visitas;
    private String donante;
    private String vehiculo;
    private String voluntario;

    private String fechaFormateada; // Para mostrar en la tabla (JTable)
    
    
    
    // Constructores
    public OrdenRetiroDTO(int id, Date fechaCreacion, String fechaFormateada, String estado, String voluntario, int cantidadPedidos, String donante, String vehiculo) {
        this.id = id;
        this.fechaCreacion = fechaCreacion;
        this.fechaFormateada = fechaFormateada;
        this.estado = estado;
        this.voluntario = voluntario;
        this.donante = donante;
        this.vehiculo = vehiculo;
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
    public String getFechaFormateada() {
        return fechaFormateada;
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