package ar.edu.unrn.seminario.modelo;

public class Vehiculo {
	
	private String patente;
	private String estado;
	private String tipoVeh;
	private int capacidad;
	public Vehiculo(String patente,String estado,String tipoVeh,int capacidad ) {
		this.setPatente(patente);
		this.setEstado(estado);
		this.setTipoVeh(tipoVeh);
		this.setCapacidad(capacidad);
		
	}
		
	public String getPatente() {
		return patente;
	}

	public String getEstado() {
		return estado;
	}
	
	public String getTipoVeh() {
		return tipoVeh;
	}
	
	public int getCapacidad() {
		return capacidad;
	}
	
	public String getDescripcion() {
        return "Tipo: " + tipoVeh + ", Patente: " + patente + ", Estado: " + estado + ", Capacidad: " + capacidad;
    }
    
	public void cambiarEstado(String nuevoEstado) {
		estado = nuevoEstado;
	}

	public void setPatente(String patente) {
		this.patente = patente;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public void setTipoVeh(String tipoVeh) {
		this.tipoVeh = tipoVeh;
	}
	
	public void setCapacidad(int capacidad) {
		this.capacidad = capacidad;
	}
	
 	public void mantenimiento() {
 		// Sin implementacion de momento
 	}
 	
}
