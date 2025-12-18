package ar.edu.unrn.seminario.modelo;

import ar.edu.unrn.seminario.exception.CampoVacioException;

public class Rol {
	
	public static final int CODIGO_ADMINISTRADOR = 1;
    public static final int CODIGO_VOLUNTARIO = 2;
    public static final int CODIGO_DONANTE = 3;
    public static final int CODIGO_BENEFICIARIO = 4;
	
	private Integer codigo;
	private String nombre;
	private boolean activo;

	public Rol() {

	}
	
	public Rol(Integer codigo, String nombre) throws CampoVacioException {
		if (codigo == null) {
			throw new CampoVacioException("El campo 'codigo' no puede ser nulo.");
		}
		if (nombre == null || nombre.isEmpty()) {
			throw new CampoVacioException("El campo 'nombre' no puede estar vacio.");
		}

		this.codigo = codigo;
		this.nombre = nombre;
	}

	public Integer getCodigo() {
		return codigo;
	}
	
	public String getNombre() {
		return nombre;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setCodigo(Integer codigo) {
		this.codigo = codigo;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	// metodo para activar
	public void activar() {
		this.activo = true;
	}

	// metodo para desactivar
	public void desactivar() {
		this.activo = false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((codigo == null) ? 0 : codigo.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Rol other = (Rol) obj;
		if (codigo == null) {
			if (other.codigo != null)
				return false;
		} else if (!codigo.equals(other.codigo))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Rol [codigo=" + codigo + ", nombre=" + nombre + ", activo=" + activo + "]";
	}

}
