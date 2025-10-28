package ar.edu.unrn.seminario.dto;

public class VoluntarioDTO {
    private int id;
    private String nombre;
    private String apellido;

    public VoluntarioDTO(int id, String nombre, String apellido) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
    }

    public VoluntarioDTO(String nombre, String apellido) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.id = 0; 
    }

    public VoluntarioDTO(String nombre, String apellido, String ubicacion) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.id = 0; 
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }
}