package ar.edu.unrn.seminario.modelo;

import ar.edu.unrn.seminario.exception.CampoVacioException;
import ar.edu.unrn.seminario.exception.ObjetoNuloException;

public class Colaborador extends Persona {
    // mantiene la orden en curso o null si no hay ninguna asignada
    private OrdenRetiro ordenActual;

    // constructores
    public Colaborador(String nom, String ape, int dni, Ubicacion ubi) throws CampoVacioException, ObjetoNuloException {
        super(nom, ape, dni, ubi);
    }
    public Colaborador(String nom, String ape, int dni) throws CampoVacioException {
        super(nom, ape, dni);
    }

    // comprueba si el colaborador esta libre
    public boolean disponible() {
        return ordenActual == null || ordenActual.estaCompletada();
    }

    // intenta aceptar la orden si esta disponible
    public boolean aceptarOrden(OrdenRetiro orden) {
        if (!disponible()) {
            return false;// no se puede aceptar la orden
        }
        // orden recibida como la actual
        this.ordenActual = orden;
        // quien recibe la orden es el voluntario
        orden.asignarVoluntario(this);
        // notificar al voluntario
        notificar("Se le asigno " + orden);
        return true;
    }

    // notificacion
    public void notificar(String mensaje) {
        System.out.println(" Voluntario "
                + obtenerNombre() + " " + obtenerApellido()
                + " (DNI " + obtenerDni() + "): "
                + mensaje);
    }

    // asignar orden actual internamente
    private void asignarOrden(OrdenRetiro o) {
        this.ordenActual = o;
    }
}
