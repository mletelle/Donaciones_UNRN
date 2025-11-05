package ar.edu.unrn.seminario.modelo;

import java.util.ArrayList;
import java.time.LocalDateTime;

import ar.edu.unrn.seminario.exception.CampoVacioException;
import ar.edu.unrn.seminario.exception.ObjetoNuloException;

public class Donante extends Persona {

    private ArrayList<PedidosDonacion> pedidos;

    public Donante(String nom, String ape, int dni, Ubicacion ubi) throws CampoVacioException, ObjetoNuloException {
        super(nom, ape, dni, ubi);
        if (ubi == null) {
            throw new ObjetoNuloException("La ubicación no puede ser nula.");
        }
        this.pedidos = new ArrayList<>();
    }

    public Donante(String nom, String ape, int dni, String dir, String zona, String barrio, double lat, double lon) throws CampoVacioException, ObjetoNuloException {
        this(nom, ape, dni, new Ubicacion(dir, zona, barrio, lat, lon));
    }

    public PedidosDonacion crearPedido(LocalDateTime fecha, ArrayList<Bien> bienes, String tipo, String obs) throws CampoVacioException, ObjetoNuloException {
        if (fecha == null) {
            throw new ObjetoNuloException("La fecha no puede ser nula.");
        }
        if (bienes == null || bienes.isEmpty()) {
            throw new CampoVacioException("La lista de bienes no puede estar vacía.");
        }
        for (Bien bien : bienes) {
            if (bien.obtenerCantidad() <= 0) {
                throw new CampoVacioException("La cantidad de cada bien debe ser mayor a cero.");
            }
        }
        if (tipo == null || tipo.isEmpty()) {
            throw new CampoVacioException("El tipo de vehículo no puede estar vacío.");
        }
        PedidosDonacion p = new PedidosDonacion(fecha, bienes, tipo, obs, this);
        pedidos.add(p);
        return p;
    }

    public PedidosDonacion crearPedido(ArrayList<Bien> bienes, String tipo, String obs) throws CampoVacioException, ObjetoNuloException {
        if (bienes == null || bienes.isEmpty()) {
            throw new CampoVacioException("La lista de bienes no puede estar vacía.");
        }
        if (tipo == null || tipo.isEmpty()) {
            throw new CampoVacioException("El tipo de vehículo no puede estar vacío.");
        }
        PedidosDonacion p = new PedidosDonacion(tipo, bienes, obs, this);
        pedidos.add(p);
        return p;
    }

    public void notificar(String mensaje) {
        System.out.println("Donante " + this + ": " + mensaje);
    }

    public String getNombre() {
        return super.obtenerNombre();
    }

    public String getDireccion() {
        return this.obtenerUbicacion();
    }

}
