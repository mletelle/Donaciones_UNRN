package ar.edu.unrn.seminario.modelo;

public enum EstadoOrden {
	PENDIENTE("Pendiente"),
	EN_EJECUCION("En Ejecucion"),
	COMPLETADO("Completado");

	private String descripcion;
	
	EstadoOrden(String descripcion) {
		this.descripcion = descripcion;
	}
	
	@Override
	public String toString() {
		return descripcion;
	}
} 
