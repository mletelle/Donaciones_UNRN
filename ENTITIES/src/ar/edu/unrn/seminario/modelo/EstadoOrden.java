package ar.edu.unrn.seminario.modelo;

public enum EstadoOrden {
	
	// Constantes
	PENDIENTE("Pendiente"),
	EN_EJECUCION("En Ejecucion"),
	COMPLETADO("Completado");

	// Atributos
	private String descripcion;
	
	// Constructores
	EstadoOrden(String descripcion) {
		this.descripcion = descripcion;
	}
	
	@Override
	public String toString() {
		return descripcion;
	}
	
} 
