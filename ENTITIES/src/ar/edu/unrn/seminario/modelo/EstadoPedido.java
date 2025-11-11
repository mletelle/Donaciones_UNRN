package ar.edu.unrn.seminario.modelo;

public enum EstadoPedido {
	PENDIENTE("Pendiente"),
	EN_EJECUCION("En Ejecucion"),
	COMPLETADO("Completado");

	private String descripcion;
	
	EstadoPedido(String descripcion) {
		this.descripcion = descripcion;
	}
	
	@Override
	public String toString() {
		return descripcion;
	}
}
