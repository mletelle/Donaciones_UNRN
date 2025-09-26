package ar.edu.unrn.seminario.modelo;

import java.util.ArrayList;
import java.util.Date;

public class Donante extends Persona {

    // lista de pedidos que el donante genera
    private ArrayList<PedidosDonacion> pedidos;

    // constructor principal, recibe los datos y crea la lista vacia
    public Donante(String nom, String ape, int dni, Ubicacion ubi) {
        super(nom, ape, dni, ubi);
        this.pedidos = new ArrayList<PedidosDonacion>();
    }

    // crear donante a partir de datos
    public Donante(String nom, String ape, int dni,
            String dir, String zona, String barrio,
            double lat, double lon) {
        this(nom, ape, dni, new Ubicacion(dir, zona, barrio, lat, lon));
    }

    // fabrica un nuevo pedido usando la fecha indicada y lo agrega a la lista
    // interna
    public PedidosDonacion crearPedido(Date fecha, ArrayList<Bien> bienes, String tipo, String obs) {
        PedidosDonacion p = new PedidosDonacion(fecha, bienes, tipo, obs, this);
        pedidos.add(p);
        return p;
    }

    // crea un pedido usando la fecha actual
    public PedidosDonacion crearPedido(ArrayList<Bien> bienes, String tipo, String obs) {
        return crearPedido(new Date(), bienes, tipo, obs);
    }

    // notificar al donante
    public void notificar(String mensaje) {
        System.out.println("Donante " + this + ": " + mensaje);
    }
}
