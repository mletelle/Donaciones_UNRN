package ar.edu.unrn.seminario.modelo;

public enum EstadoOrden {
	
	// Constantes
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
	
	public static EstadoOrden fromString(String texto) {
	    if (texto == null || texto.isEmpty()) {
	        throw new IllegalArgumentException("El estado no puede ser nulo o vacio");
	    }
	    
	    String textoNormalizado = texto.trim().toUpperCase();


	    for (EstadoOrden e : EstadoOrden.values()) {
	        if (e.name().equals(textoNormalizado)) {
	            return e;
	        }
	    }
	    throw new IllegalArgumentException("Estado de orden desconocido: " + texto);
	}
	
} 
