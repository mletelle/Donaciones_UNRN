package ar.edu.unrn.seminario.dto;

import java.util.List;

public class PedidoDonacionDTO {
    
	// Atributos
	private int id;
    private String fecha;
    private List<BienDTO> bienes;
    private String tipoVehiculo;
    private int donanteId;
    private String donante;
    private String direccion;
    private String estado;

    // Constructores
    public PedidoDonacionDTO(String fecha, List<BienDTO> bienes, String tipoVehiculo, int donanteId) {
        this.fecha = fecha;
        this.bienes = bienes;
        this.tipoVehiculo = tipoVehiculo;
        this.donanteId = donanteId;
    }

    public PedidoDonacionDTO(int id, String donante, String direccion, String estado) {
        this.id = id;
        this.donante = donante;     
        this.direccion = direccion; 
        this.estado = estado;       
    }

    //  Constructor completo para listados con nombre de donante
    public PedidoDonacionDTO(int id, String fecha, String tipoVehiculo, int donanteId, String nombreDonante) {
        this.id = id;
        this.fecha = fecha;
        this.tipoVehiculo = tipoVehiculo;
        this.donanteId = donanteId;
        this.donante = nombreDonante;
    }

    //  Constructor completo con estado para listados
    public PedidoDonacionDTO(int id, String fecha, String tipoVehiculo, int donanteId, String nombreDonante, String estado) {
        this.id = id;
        this.fecha = fecha;
        this.tipoVehiculo = tipoVehiculo;
        this.donanteId = donanteId;
        this.donante = nombreDonante;
        this.estado = estado;
    }

    // Getters
    public int getId() {
        return id;
    }
    
    public String getFecha() {
        return fecha;
    }

    public List<BienDTO> getBienes() {
        return bienes;
    }
    
    public String getTipoVehiculo() {
        return tipoVehiculo;
    }
    
    public int getDonanteId() {
        return donanteId;
    }
    
    public String getDonante() {
        return donante;
    }

    public String getDireccion() {
        return direccion;
    }
    
    // Setters
    public String getEstado() {
        return estado;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public void setBienes(List<BienDTO> bienes) {
        this.bienes = bienes;
    }

    public void setTipoVehiculo(String tipoVehiculo) {
        this.tipoVehiculo = tipoVehiculo;
    }

    public void setDonanteId(int donanteId) {
        this.donanteId = donanteId;
    }

}
