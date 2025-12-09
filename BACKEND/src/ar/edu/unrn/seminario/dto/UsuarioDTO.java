package ar.edu.unrn.seminario.dto;

public class UsuarioDTO {
	
	private String username;
	private String password;
	private String nombre;
	private String email;
	private String rol;
	private boolean activo;
	private String estado;
	
	private int dni;
	private String apellido;
	private String direccion;

	public UsuarioDTO(String username, String password, String nombre, String email, String rol, boolean activo,
			String estado) {
		super();
		this.username = username;
		this.password = password;
		this.nombre = nombre;
		this.email = email;
		this.rol = rol;
		this.activo = activo;
		this.estado = estado;
	}

	public UsuarioDTO(int dni, String nombre, String apellido, String direccion) {
		this.dni = dni;
		this.nombre = nombre;
		this.apellido = apellido;
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

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public void setNombre(String nombre) {
		this.nombre = nombre;
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

	public String getApellido() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getId() {
		// TODO Auto-generated method stub
		return 0;
	}
	
}