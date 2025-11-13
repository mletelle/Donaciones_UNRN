package ar.edu.unrn.seminario.dto;

public class VoluntarioDTO {
    
	// Atributos
	private int id;
    private String nombre;
    private String apellido;
    private String usuario;

    // Constructores
    public VoluntarioDTO(int id, String nombre, String apellido, String usuario) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.usuario = usuario;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getUsuario() {
        return usuario;
    }

    @Override
    public String toString() {
        return nombre + " " + apellido;
    }
}