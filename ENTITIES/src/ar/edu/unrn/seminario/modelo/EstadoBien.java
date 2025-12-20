package ar.edu.unrn.seminario.modelo;

public enum EstadoBien {
	
	PENDIENTE("Pendiente"),
	EN_STOCK("En Stock"),
	ENTREGADO("Entregado"),
	BAJA("Baja");
	
	private String descripcion;
	
	EstadoBien(String descripcion) {
		this.descripcion = descripcion;
	}
	
	@Override
	public String toString() {
		return descripcion;
	}
	
	public static EstadoBien fromString(String texto) {
		if (texto == null || texto.isEmpty()) {
			return PENDIENTE;
		}
		String normalizado = texto.trim().toUpperCase();
		for (EstadoBien e : EstadoBien.values()) {
			if (e.name().equals(normalizado)) {
				return e;
			}
		}
		throw new IllegalArgumentException("estado de bien desconocido: " + texto);
	}
}
