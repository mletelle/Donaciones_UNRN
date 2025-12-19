package ar.edu.unrn.seminario.modelo;

import java.util.Date;
import java.util.List;

public class OrdenEntrega {

    private int id;
    private Date fechaGeneracion;
    private EstadoEntrega estado;
    private Usuario beneficiario;
    private Usuario voluntario;
    private Vehiculo vehiculo;
    private List<Bien> bienes;

    public OrdenEntrega(Usuario beneficiario, List<Bien> bienes) {
        this.fechaGeneracion = new Date();
        this.estado = EstadoEntrega.PENDIENTE;
        this.beneficiario = beneficiario;
        this.bienes = bienes;
    }

    public OrdenEntrega() {
		// TODO Auto-generated constructor stub
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

    public EstadoEntrega getEstado() {
        return estado;
    }

    public void forzarEstadoDesdeBD(EstadoEntrega estado) {
        this.estado = estado;
    }
    
    private void transicionarA(EstadoEntrega nuevoEstado) {
        if (!this.estado.esTransicionValida(nuevoEstado)) {
            throw new IllegalStateException(
                String.format("no se puede pasar la orden %d de %s a %s", 
                this.id, this.estado, nuevoEstado)
            );
        }
        this.estado = nuevoEstado;
    }
    
    public void marcarComoCompletada() {
        transicionarA(EstadoEntrega.COMPLETADO);
    }
    
    public boolean estaPendiente() {
        return this.estado == EstadoEntrega.PENDIENTE;
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

    public void asignarRecursos(Usuario voluntario, Vehiculo vehiculo) {
        this.voluntario = voluntario;
        this.vehiculo = vehiculo;
    }

	public void setEstado(EstadoEntrega valueOf) {
		this.estado = valueOf;
	}
}