package ar.edu.unrn.seminario.modelo;

public enum TipoVehiculo {
	
	AUTO("Auto"),
	CAMIONETA("Camioneta"),
	CAMION("Camion");
	
	private String descripcion;
	
	TipoVehiculo(String descripcion) {
		this.descripcion = descripcion;
	}
	
	@Override
	public String toString() {
		return descripcion;
	}
	
}
