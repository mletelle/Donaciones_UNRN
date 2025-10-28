package ar.edu.unrn.seminario.modelo;

import ar.edu.unrn.seminario.exception.CampoVacioException;
import ar.edu.unrn.seminario.exception.ObjetoNuloException;

public class Voluntario extends Persona {
    private OrdenRetiro ordenActual;

    public Voluntario(String nombre, String apellido, int dni, Ubicacion ubicacion) throws CampoVacioException, ObjetoNuloException {
        super(nombre, apellido, dni, ubicacion);
    }

    public Voluntario(String nombre, String apellido, int dni) throws CampoVacioException {
        super(nombre, apellido, dni);
    }

    public Voluntario(String nombre, String apellido, String ubicacion) throws CampoVacioException, ObjetoNuloException {
        super(nombre, apellido, 0, new Ubicacion(ubicacion, "", "", 0.0, 0.0));
    }

    // comprueba si el voluntario esta libre
    public boolean disponible() {
        return ordenActual == null || ordenActual.estaCompletada();
    }

    // intenta aceptar la orden si esta disponible
    public boolean aceptarOrden(OrdenRetiro orden) {
        if (!disponible()) {
            return false; // no se puede aceptar la orden
        }
        // orden recibida como la actual
        this.ordenActual = orden;
        // quien recibe la orden es el voluntario
        orden.asignarVoluntario(this);
        // notificar al voluntario
        notificar("Se le asigno " + orden);
        return true;
    }

    @Override
    public void notificar(String mensaje) {
        System.out.println("Voluntario " + obtenerNombre() + " " + obtenerApellido() + " (DNI " + obtenerDni() + "): " + mensaje);
    }

    public int obtenerId() {
        return this.obtenerDni();
    }

    public String getNombre() {
        return obtenerNombre();
    }
}