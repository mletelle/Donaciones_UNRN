package ar.edu.unrn.seminario.modelo;

public enum TipoBien {
	
	ALIMENTO("Alimento"),
	ROPA("Ropa"),
	MOBILIARIO("Mobiliario"),
	HIGIENE("Higiene");
	
	private String descripcion;
	
	TipoBien(String descripcion) {
		this.descripcion = descripcion;
	}
	
	@Override
	public String toString() {
		return descripcion;
	}
	
}
