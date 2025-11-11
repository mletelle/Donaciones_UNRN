package ar.edu.unrn.seminario.modelo;

import ar.edu.unrn.seminario.exception.CampoVacioException;
import ar.edu.unrn.seminario.exception.ObjetoNuloException;

public class Voluntario extends Persona {
    private OrdenRetiro ordenActual;

    public Voluntario(String nombre, String apellido, int dni, Ubicacion ubicacion) throws CampoVacioException, ObjetoNuloException {
        super(nombre, apellido, dni, ubicacion);
    }

    // comprueba si el voluntario esta libre
    public boolean disponible() {
        return ordenActual == null || ordenActual.estaCompletada();
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