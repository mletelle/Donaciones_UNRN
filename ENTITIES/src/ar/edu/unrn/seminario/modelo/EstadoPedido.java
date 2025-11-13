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
	
	public static EstadoPedido fromString(String texto) {
		if (texto == null || texto.isEmpty()) {
			throw new IllegalArgumentException("El estado no puede ser nulo o vacio");
		}
		String textoNormalizado = texto.trim().toUpperCase();
		for (EstadoPedido e : EstadoPedido.values()) {
			if (e.name().equals(textoNormalizado)) {
				return e;
			}
		}
		throw new IllegalArgumentException("Estado de pedido desconocido: " + texto);
	}
	
}
