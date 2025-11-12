package ar.edu.unrn.seminario.dto;

public class DonanteDTO {
    
	// Atributos
	private int id;
    private String nombre;
    private String apellido;

    // Constructores
    public DonanteDTO(int id, String nombre, String apellido) {
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

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    @Override
    public String toString() {
        return nombre + " " + apellido;
    }
}