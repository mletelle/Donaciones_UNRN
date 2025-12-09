package ar.edu.unrn.seminario.dto;

public class UsuarioDTO {
	
	private String username;
	private String password; // Opcional en DTO de listado
	private String nombre;
	private String apellido;
	private String dni;
	private String email;
	private String rol;
	private boolean activo;
	private String estado;
	private String direccion;

	// Constructor completo
	public UsuarioDTO(String username, String nombre, String apellido, String dni, String email, String rol, boolean activo, String estado) {
		this.username = username;
		this.nombre = nombre;
		this.apellido = apellido;
		this.dni = dni;
		this.email = email;
		this.rol = rol;
		this.activo = activo;
		this.estado = estado;
	}
	
	// Constructor simple para compatibilidad
	public UsuarioDTO(String username, String nombre, String apellido, String dni) {
		this.username = username;
		this.nombre = nombre;
		this.apellido = apellido;
		this.dni = dni;
	}

	public String getUsername() {
		return username;
		}
	public String getNombre() {
		return nombre;
		}
	public String getApellido() {
		return apellido; 
		}
	public String getDni() {
		return dni;
		}
	public String getEmail() {
		return email;
		}
	public String getRol() {
		return rol;
		}
	public boolean isActivo() {
		return activo;
		}
	public String getEstado() {
		return estado;
		}
	public String getDireccion() {
		return direccion;
		}
	
	public void setDireccion(String direccion) { 
		this.direccion = direccion;
		}

	@Override
	public String toString() {
		return nombre + " " + apellido + " (" + username + ")";
	}
}