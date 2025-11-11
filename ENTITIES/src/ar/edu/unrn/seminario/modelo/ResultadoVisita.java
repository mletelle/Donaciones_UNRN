package ar.edu.unrn.seminario.modelo;

public enum ResultadoVisita {
	RECOLECCION_EXITOSA("Recoleccion Exitosa"),
	RECOLECCION_PARCIAL("Recoleccion Parcial"),
	DONANTE_AUSENTE("Donante Ausente"),
	CANCELADO("Cancelado");

	private String descripcion;
	
	ResultadoVisita(String descripcion) {
		this.descripcion = descripcion;
	}
	
	@Override
	public String toString() {
		return descripcion;
	}
	
	public static ResultadoVisita fromString(String texto) {
		for (ResultadoVisita r : ResultadoVisita.values()) {
			if (r.descripcion.equalsIgnoreCase(texto)) {
				return r;
			}
		}
		throw new IllegalArgumentException("Resultado de visita desconocido: " + texto);
	}
}
