package ar.edu.unrn.seminario.modelo;

public class Beneficiario {
	private String nombre;
	private String apellido;
	private int DNI;
	private Ubicacion ubicacion;
	private String contacto;
	private OrdenEntrega ordenActual;
	
	public Beneficiario(String nmbre, String ape, int DNI, Ubicacion ubicacion) {
		this.nombre = nmbre;
		this.apellido = ape;
		this.DNI = DNI;
		this.ubicacion = ubicacion;
	}
	public void cambiarUbicacion(Ubicacion u) {
		setUbicacion(u);
	}
	public void notificar(String mensaje) {
		System.out.println("Mensaje: " +mensaje);
	}
	
	public void asignar(OrdenEntrega orden) {
		setOrdenActual(orden);
	}
	
	
	// Setters-Getters
	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getApellido() {
		return apellido;
	}

	public void setApellido(String apellido) {
		this.apellido = apellido;
	}

	public int getDNI() {
		return DNI;
	}

	public void setDNI(int dNI) {
		DNI = dNI;
	}

	public Ubicacion getUbicacion() {
		return ubicacion;
	}

	public void setUbicacion(Ubicacion ubicacion) {
		this.ubicacion = ubicacion;
	}

	public String getContacto() {
		return contacto;
	}

	public void setContacto(String contacto) {
		this.contacto = contacto;
	}

	public OrdenEntrega getOrdenActual() {
		return ordenActual;
	}

	public void setOrdenActual(OrdenEntrega ordenActual) {
		this.ordenActual = ordenActual;
	}
	
	
	
	
	
	
	
	
}
