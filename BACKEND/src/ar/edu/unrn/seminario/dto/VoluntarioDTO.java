package ar.edu.unrn.seminario.dto;

public class VoluntarioDTO {
    
	// Atributos
	private int id;
    private String nombre;
    private String apellido;

    // Constructores
    public VoluntarioDTO(int id, String nombre, String apellido) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
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

    @Override
    public String toString() {
        return nombre + " " + apellido;
    }
}