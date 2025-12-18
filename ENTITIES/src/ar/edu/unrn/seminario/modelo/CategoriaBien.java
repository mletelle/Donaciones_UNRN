package ar.edu.unrn.seminario.modelo;

public enum CategoriaBien {
	
	ROPA("Ropa"),
	MUEBLES("Muebles"),
	ALIMENTOS("Alimentos"),
	ELECTRODOMESTICOS("Electrodomesticos"),
	HERRAMIENTAS("Herramientas"),
	JUGUETES("Juguetes"),
	LIBROS("Libros"),
	MEDICAMENTOS("Medicamentos"),
	HIGIENE("Higiene"),
	OTROS("Otros");
	
	private String descripcion;
	
	CategoriaBien(String descripcion) {
		this.descripcion = descripcion;
	}
	
	@Override
	public String toString() {
		return descripcion;
	}
	
}
