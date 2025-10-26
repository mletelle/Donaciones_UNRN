package ar.edu.unrn.seminario.modelo;

import ar.edu.unrn.seminario.exception.CampoVacioException;
import ar.edu.unrn.seminario.exception.ObjetoNuloException;

public abstract class Persona {

    // atributos
    private String nombre;
    private String apellido;
    private int dni;
    private Ubicacion ubicacion;

    // crea una nueva persona con los datos basicos
    public Persona(String nombre, String apellido, int dni, Ubicacion ubicacion) throws CampoVacioException, ObjetoNuloException {
        if (nombre == null || nombre.isEmpty()) {
            throw new CampoVacioException("El campo 'nombre' no puede estar vacío.");
        }
        if (apellido == null || apellido.isEmpty()) {
            throw new CampoVacioException("El campo 'apellido' no puede estar vacío.");
        }
        if (dni <= 0) {
            throw new CampoVacioException("El campo 'dni' debe ser un número positivo.");
        }
        if (ubicacion == null) {
            throw new ObjetoNuloException("El campo 'ubicacion' no puede ser nulo.");
        }

        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.ubicacion = ubicacion;
    }

    public Persona(String nombre, String apellido, int dni) throws CampoVacioException {
        if (nombre == null || nombre.isEmpty()) {
            throw new CampoVacioException("El campo 'nombre' no puede estar vacío.");
        }
        if (apellido == null || apellido.isEmpty()) {
            throw new CampoVacioException("El campo 'apellido' no puede estar vacío.");
        }
        if (dni <= 0) {
            throw new CampoVacioException("El campo 'dni' debe ser un número positivo.");
        }

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

    public String obtenerUbicacion() {
        return ubicacion.toString();
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
