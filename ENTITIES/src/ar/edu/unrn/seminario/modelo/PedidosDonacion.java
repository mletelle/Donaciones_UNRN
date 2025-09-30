package ar.edu.unrn.seminario.modelo;

import java.util.ArrayList;
import java.util.Date;

public class PedidosDonacion {

    // variables de clase
    private static int secuencia = 0;//para usarlo de id

    // catalogos
    private static final int VEHICULO_AUTO = 1;
    private static final int VEHICULO_CAMIONETA = 2;
    private static final int VEHICULO_CAMION = 3;

    // atributos
    private int id;
    private Date fecha;
    private ArrayList<Bien> bienes;
    private int tipoVehiculo;
    private String observaciones;
    private Donante donante; // asociacion inversa
    private OrdenRetiro ordenRetiro; // 1 a 1

    // constructor con todos los parametros
    public PedidosDonacion(Date fecha, ArrayList<Bien> bienes, int tipoVehiculo, String observaciones, Donante d) {
        this.id = ++secuencia;//preincremta para arrancar desde el 1
        this.fecha = fecha;
        this.bienes = bienes;
        this.tipoVehiculo = tipoVehiculo;
        this.observaciones = observaciones;
        this.donante = d;
    }
    public PedidosDonacion(Date fecha, ArrayList<Bien> bienes, String tipo, String observaciones, Donante d) {
        this.id = ++secuencia;//preincremta para arrancar desde el 1
        this.fecha = fecha;
        this.bienes = bienes;
        this.observaciones = observaciones;
        this.donante = d;
        if (tipo.equalsIgnoreCase("auto")) {
        	this.tipoVehiculo = 1;
        }else if(tipo.equalsIgnoreCase("camioneta")) {
        	this.tipoVehiculo = 2;
        }else {
        	this.tipoVehiculo = 3;

        };
    }
    // constructor sin fecha (usa la actual)
    public PedidosDonacion(ArrayList<Bien> bienes, int tipoVehiculo, String observaciones, Donante d) {
        this(new Date(), bienes, tipoVehiculo, observaciones, d);
    }

    // getters
    public int obtenerId() {
        return id;
    }

    public Date obtenerFecha() {
        return fecha;
    }

    public ArrayList<Bien> obtenerBienes() {
        return bienes;
    }

    public int obtenerTipoVehiculo() {
        return tipoVehiculo;
    }

    public Donante obtenerDonante() {
        return donante;
    }

    public OrdenRetiro obtenerOrden() {
        return ordenRetiro;
    }
  
    // relacion con la orden
    public void asignarOrden(OrdenRetiro o) {
        this.ordenRetiro = o;
    }

    @Override
    public String toString() {
        return "Pedido#" + id
                + " (" + fecha + ") de " + donante
                + " Vehiculo: " + describirTipoVehiculo();
    }
  
    // metodo de ayuda para el toString
    private String describirTipoVehiculo() {
        switch (tipoVehiculo) {
            case VEHICULO_AUTO:
                return "AUTO";
            case VEHICULO_CAMIONETA:
                return "CAMIONETA";
            case VEHICULO_CAMION:
                return "CAMIoN";
            default:
                return "DESCONOCIDO";
        }
    }
    public boolean equals(PedidosDonacion obj) {
        return (fecha.equals(obj.fecha))&&
        (this.tipoVehiculo==obj.tipoVehiculo)&&
        (this.donante.equals(obj.donante));
    } 
}
