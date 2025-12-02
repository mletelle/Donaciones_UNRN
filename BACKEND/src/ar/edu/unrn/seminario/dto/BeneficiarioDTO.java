package ar.edu.unrn.seminario.dto;

public class BeneficiarioDTO {
    private int id; 
    private String nombre;
    private String apellido;
    private int dni;
    private String contacto;
    private String ubicacion; 

    // Constructor completo
    public BeneficiarioDTO(int id, String nombre, String apellido, int dni, String contacto, String ubicacion) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.contacto = contacto;
        this.ubicacion = ubicacion;
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

    public int getDni() {
        return dni;
    }

    public String getContacto() {
        return contacto;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    @Override
    public String toString() {

        return nombre + " " + apellido + " (DNI: " + dni + ")";
    }
}