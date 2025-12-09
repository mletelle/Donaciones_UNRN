package ar.edu.unrn.seminario.modelo;

import java.util.Date;
import java.util.List;

public class OrdenEntrega {

    private int id;
    private Date fechaGeneracion;
    private int estado;
    private Usuario beneficiario;
    private Usuario voluntario;
    private Vehiculo vehiculo;
    private List<Bien> bienes;

    // Constantes de estado
    public static final int ESTADO_PENDIENTE = 1;
    public static final int ESTADO_COMPLETADO = 3;
    public static final int ESTADO_CANCELADO = 4;

    // Constructor para NUEVAS ordenes
    public OrdenEntrega(Usuario beneficiario, List<Bien> bienes) {
        this.fechaGeneracion = new Date();
        this.estado = ESTADO_PENDIENTE;
        this.beneficiario = beneficiario;
        this.bienes = bienes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(Date fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public Usuario getBeneficiario() {
        return beneficiario;
    }

    public void setBeneficiario(Usuario beneficiario) {
        this.beneficiario = beneficiario;
    }

    public Usuario getVoluntario() {
        return voluntario;
    }

    public void setVoluntario(Usuario voluntario) {
        this.voluntario = voluntario;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(Vehiculo vehiculo) {
        this.vehiculo = vehiculo;
    }

    public List<Bien> getBienes() {
        return bienes;
    }

    public void setBienes(List<Bien> bienes) {
        this.bienes = bienes;
    }

    public String obtenerEstadoString() {
        if (estado == ESTADO_PENDIENTE) return "PENDIENTE";
        if (estado == ESTADO_COMPLETADO) return "COMPLETADO";
        if (estado == ESTADO_CANCELADO) return "CANCELADO";
        return "DESCONOCIDO";
    }

    public void asignarRecursos(Usuario voluntario, Vehiculo vehiculo) {
        this.voluntario = voluntario;
        this.vehiculo = vehiculo;
    }
}