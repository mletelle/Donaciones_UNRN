package ar.edu.unrn.seminario.dto;

public class UsuarioDTO {

	private String username;
	private String password;
	private String nombre;
	private String apellido;
	private String email;
	private String rol;
	private boolean activo;
	private String estado;
	private int dni;
	private String direccion;

	public UsuarioDTO() {}

	// Constructor completo (General)
	public UsuarioDTO(String username, String password, String nombre, String email, String rol, boolean activo,
			String estado) {
		this.username = username;
		this.password = password;
		this.nombre = nombre;
		this.email = email;
		this.rol = rol;
		this.activo = activo;
		this.estado = estado;
	}

	// Constructor para Listados (Donantes/Voluntarios)
	public UsuarioDTO(String username, String nombre, String apellido, int dni, String direccion, Integer codigoRol) {
		this.username = username;
		this.nombre = nombre;
		this.apellido = apellido;
		this.dni = dni;
		this.direccion = direccion;
	}

	// Constructor para Detalles
	public UsuarioDTO(String username, String nombre, String apellido, String email, String rol, boolean activo, String estado, int dni, String direccion) {
		this.username = username;
		this.nombre = nombre;
		this.apellido = apellido;
		this.email = email;
		this.rol = rol;
		this.activo = activo;
		this.estado = estado;
		this.dni = dni;
		this.direccion = direccion;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getNombre() {
		return nombre;
	}

	public String getApellido() {
		return apellido;
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

	public int getDni() {
		return dni;
	}

	public String getDireccion() {
		return direccion;
	}

	public int getId() {
		return dni;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public void setApellido(String apellido) {
		this.apellido = apellido;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setRol(String rol) {
		this.rol = rol;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public void setDni(int dni) {
		this.dni = dni;
	}

	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

	// Alias para compatibilidad con código existente
	public String getUsuario() {
		return username;
	}

	@Override
	public String toString() {
		return nombre + " " + (apellido != null ? apellido : "") + " (DNI: " + dni + ")";
	}
}