package ar.edu.unrn.seminario.modelo;

public enum EstadoPedido {
	
	// Constantes
	PENDIENTE("Pendiente"),
	EN_EJECUCION("En Ejecucion"),
	COMPLETADO("Completado");

	// Atributos
	private String descripcion;
	
	// Constructores
	EstadoPedido(String descripcion) {
		this.descripcion = descripcion;
	}
	
	@Override
	public String toString() {
		return descripcion;
	}
	
}
