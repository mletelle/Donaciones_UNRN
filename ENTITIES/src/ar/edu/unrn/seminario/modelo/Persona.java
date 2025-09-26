package ar.edu.unrn.seminario.modelo;

public abstract class Persona {

    // atributos
    private String nombre;
    private String apellido;
    private int dni;
    private Ubicacion ubicacion;

    // crea una nueva persona con los datos basicos
    public Persona(String nombre, String apellido, int dni, Ubicacion ubicacion) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.ubicacion = ubicacion;
    }
        public Persona(String nombre, String apellido, int dni) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
    }

    // getters
    public String obtenerNombre() {
        return nombre;
    }

    public String obtenerApellido() {
        return apellido;
    }

    public int obtenerDni() {
        return dni;
    }

    public Ubicacion obtenerUbicacion() {
        return ubicacion;
    }

    public void cambiarUbicacion(Ubicacion nueva) {
        this.ubicacion = nueva;
    }

    // abstracta, cada subclase debe implementarla
    public abstract void notificar(String mensaje);

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        Persona other = (Persona) obj;
        return this.dni == other.dni; // unicidad por DNI
    }

    @Override
    public String toString() {
        return nombre + " " + apellido + " (DNI " + dni + ")";
    }
}
