package ar.edu.unrn.seminario.dto;

public class RolDTO {
	
	// Atributos
	private Integer codigo;
	private String nombre;
	private boolean activo;

	// Constructores
	public RolDTO(Integer codigo, String nombre) {
		super();
		this.codigo = codigo;
		this.nombre = nombre;
	}

	public RolDTO(Integer codigo, String nombre, boolean activo) {
		super();
		this.codigo = codigo;
		this.nombre = nombre;
		this.activo = activo;
	}

	// Getters
	public Integer getCodigo() {
		return codigo;
	}
	
	public String getNombre() {
		return nombre;
	}

	public boolean isActivo() {
		return activo;
	}
	
	// Setters
	public void setCodigo(Integer codigo) {
		this.codigo = codigo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

}
