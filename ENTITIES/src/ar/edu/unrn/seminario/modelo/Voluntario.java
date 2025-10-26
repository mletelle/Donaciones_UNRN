package ar.edu.unrn.seminario.modelo;

public class Voluntario {

    private String nombre;
    private String apellido;
    private String zona;

    public Voluntario(String nombre, String apellido, String zona) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.zona = zona;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getZona() {
        return zona;
    }

    @Override
    public String toString() {
        return nombre + " " + apellido + " (" + zona + ")";
    }
}