package ar.edu.unrn.seminario.modelo;

import java.util.ArrayList;

import ar.edu.unrn.seminario.exception.CampoVacioException;
import ar.edu.unrn.seminario.exception.ObjetoNuloException;

public class Usuario {
	private String usuario;
	private String contrasena;
	private String nombre;
	private String email;
	private Rol rol;
	private boolean activo;
	
	//  absorbidos de Persona
	private String apellido;
	private int dni;
	private String direccion; // SIMPLIFICADO: solo String en lugar de objeto Ubicacion
	
	//  para roles específicos
	private ArrayList<PedidosDonacion> pedidos; // Para Donante
	private ArrayList<OrdenRetiro> ordenesAsignadas; // Para Voluntario

	public Usuario(String usuario, String contrasena, String nombre, String email, Rol rol, String apellido, int dni, String direccion) throws CampoVacioException, ObjetoNuloException {
		if (usuario == null || usuario.isEmpty()) {
			throw new CampoVacioException("El campo 'usuario' no puede estar vacio.");
		}
		if (contrasena == null || contrasena.isEmpty()) {
			throw new CampoVacioException("El campo 'contraseña' no puede estar vacio.");
		}
		if (nombre == null || nombre.isEmpty()) {
			throw new CampoVacioException("El campo 'nombre' no puede estar vacio.");
		}
		if (email == null || email.isEmpty()) {
			throw new CampoVacioException("El campo 'email' no puede estar vacio.");
		}
		if (rol == null) {
			throw new ObjetoNuloException("El campo 'rol' no puede ser nulo.");
		}
		if (apellido == null || apellido.isEmpty()) {
			throw new CampoVacioException("El campo 'apellido' no puede estar vacio.");
		}
		if (dni <= 0) {
			throw new CampoVacioException("El campo 'dni' debe ser un numero positivo.");
		}
		//  solo requerida para DONANTES
		if ((direccion == null || direccion.isEmpty()) && rol.getCodigo() == 3) {
			throw new CampoVacioException("El campo 'direccion' no puede estar vacio para Donantes.");
		}

		this.usuario = usuario;
		this.contrasena = contrasena;
		this.nombre = nombre;
		this.email = email;
		this.rol = rol;
		this.apellido = apellido;
		this.dni = dni;
		this.direccion = direccion;
		this.activo = true; // ACTIVO POR DEFECTO
		
		//  listas según el rol
		this.pedidos = new ArrayList<>();
		this.ordenesAsignadas = new ArrayList<>();
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public String getContrasena() {
		return contrasena;
	}

	public void setContrasena(String contrasena) {
		this.contrasena = contrasena;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Rol getRol() {
		return rol;
	}

	public void setRol(Rol rol) {
		this.rol = rol;
	}

	public String getApellido() {
		return apellido;
	}

	public void setApellido(String apellido) {
		this.apellido = apellido;
	}

	public int getDni() {
		return dni;
	}

	public void setDni(int dni) {
		this.dni = dni;
	}

	public String getDireccion() {
		return direccion;
	}

	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

	public ArrayList<PedidosDonacion> getPedidos() {
		return pedidos;
	}

	public ArrayList<OrdenRetiro> getOrdenesAsignadas() {
		return ordenesAsignadas;
	}

	// Métodos con nomenclatura obtener (estilo del dominio)
	public String obtenerApellido() {
		return apellido;
	}

	public int obtenerDni() {
		return dni;
	}

	public String obtenerDireccion() {
		return direccion;
	}

	public String obtenerNombre() {
		return nombre;
	}

	public boolean isActivo() {
		return activo;
	}

	public String obtenerEstado() {
		return isActivo() ? "ACTIVO" : "INACTIVO";
	}

	public void activar() {
		if (!isActivo())
			this.activo = true;
	}

	public void desactivar() {
		if (isActivo())
			this.activo = false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + dni; // DNI como clave principal
		result = prime * result + ((usuario == null) ? 0 : usuario.hashCode());
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
		Usuario other = (Usuario) obj;
		// Unicidad por DNI
		return this.dni == other.dni;
	}

	@Override
	public String toString() {
		return nombre + " " + apellido + " (DNI " + dni + ")";
	}

}
