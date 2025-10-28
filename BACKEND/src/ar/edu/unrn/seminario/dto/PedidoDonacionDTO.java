package ar.edu.unrn.seminario.dto;

import java.util.List;

public class PedidoDonacionDTO {
    private int id;
    private String fecha;
    private List<BienDTO> bienes;
    private String tipoVehiculo;
    private String observaciones;
    private int donanteId;
    private String donante;
    private String direccion;
    private String estado;

    public PedidoDonacionDTO(int id, String fecha, String tipoVehiculo, String observaciones, int donanteId) {
        this.id = id;
        this.fecha = fecha;
        this.tipoVehiculo = tipoVehiculo;
        this.observaciones = observaciones;
        this.donanteId = donanteId;
    }

    public PedidoDonacionDTO(String fecha, List<BienDTO> bienes, String tipoVehiculo, String observaciones, int donanteId) {
        this.fecha = fecha;
        this.bienes = bienes;
        this.tipoVehiculo = tipoVehiculo;
        this.observaciones = observaciones;
        this.donanteId = donanteId;
    }

    public PedidoDonacionDTO(int id, String fecha, String tipoVehiculo, String observaciones, int donanteId, List<BienDTO> bienes) {
        this.id = id;
        this.fecha = fecha;
        this.tipoVehiculo = tipoVehiculo;
        this.observaciones = observaciones;
        this.donanteId = donanteId;
        this.bienes = bienes;
    }

    public PedidoDonacionDTO(int id, String donante, String direccion, String estado) {
        this.id = id;
        this.donante = donante;     
        this.direccion = direccion; 
        this.estado = estado;       
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public List<BienDTO> getBienes() {
        return bienes;
    }

    public void setBienes(List<BienDTO> bienes) {
        this.bienes = bienes;
    }

    public String getTipoVehiculo() {
        return tipoVehiculo;
    }

    public void setTipoVehiculo(String tipoVehiculo) {
        this.tipoVehiculo = tipoVehiculo;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public int getDonanteId() {
        return donanteId;
    }

    public void setDonanteId(int donanteId) {
        this.donanteId = donanteId;
    }
    public String getDonante() {
        return donante;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getEstado() {
        return estado;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PedidoDonacionDTO that = (PedidoDonacionDTO) obj;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return "PedidoDonacionDTO{" +
                "id=" + id +
                ", fecha='" + fecha + '\'' +
                '}';
    }
}