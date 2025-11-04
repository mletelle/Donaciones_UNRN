package ar.edu.unrn.seminario.modelo;

import java.util.ArrayList;
import java.util.Date;

import ar.edu.unrn.seminario.exception.ObjetoNuloException;

public class OrdenRetiro {

    // variables y catalogos
    private static int secuencia = 0;//para el id
    private static final int ESTADO_PENDIENTE = 1;
    private static final int ESTADO_EN_EJECUCION = 2;
    private static final int ESTADO_COMPLETADO = 3;

    // atributos
    private Date fechaGeneracion = new Date();
    private int estado;
    private Ubicacion destino;
    private ArrayList<Voluntario> voluntarios;
    private PedidosDonacion pedidoOrigen;
    private ArrayList<Visita> visitas;
    private int id;
    private Vehiculo vehiculo;

    


    // constructor con todos los parametros
    public OrdenRetiro(PedidosDonacion pedido, Ubicacion dest) throws ObjetoNuloException {
        if (pedido == null) {
            throw new ObjetoNuloException("El pedido de donación no puede ser nulo.");
        }
        this.id = ++secuencia;
        this.estado = ESTADO_PENDIENTE;
        this.destino = dest;
        this.pedidoOrigen = pedido;
        this.voluntarios = new ArrayList<Voluntario>();
        this.visitas = new ArrayList<Visita>();
        pedido.asignarOrden(this);
    }

    public OrdenRetiro(PedidosDonacion pedido, String dest) throws ObjetoNuloException {
        if (pedido == null || dest == null || dest.isEmpty()) {
            throw new ObjetoNuloException("El pedido de donación o el destino no puede ser nulo o vacío.");
        }
        this.estado = ESTADO_PENDIENTE;
        this.destino = new Ubicacion(dest, "", "", 0.0, 0.0); // Assuming default values for Ubicacion
        this.pedidoOrigen = pedido;
        this.voluntarios = new ArrayList<>();
        this.visitas = new ArrayList<>();
        pedido.asignarOrden(this);
    }
    public OrdenRetiro(Voluntario voluntario, String tipoVehiculo) {
        this.estado = ESTADO_PENDIENTE;
        this.voluntarios = new ArrayList<>();
        this.visitas = new ArrayList<>();
        this.voluntarios.add(voluntario);
        this.destino = null; // Default destination, can be updated later
        this.pedidoOrigen = null; // Default, as multiple pedidos can be associated
    }

    // metodos
    // asignacion de voluntario
    public void asignarVoluntario(Voluntario voluntario) {
        if (this.voluntarios == null) {
            this.voluntarios = new ArrayList<>();
        }
        this.voluntarios.add(voluntario);
    }
  
    // actualizacion de estado
    public void actualizarEstado(int nuevoEstado) {
        this.estado = nuevoEstado;
    }
  
    // getters
    public int obtenerEstado() {
        return estado;
    }

	public boolean estaCompletada() {
		return estado == ESTADO_COMPLETADO;
	}
    public Voluntario obtenerPrimerVoluntario() {
      if (voluntarios.isEmpty()) {
          return null; // No hay voluntarios disponibles
      }
      return voluntarios.get(0); // Devuelve el primero de la lista
    }
  
    public int obtenerId() {
        return this.id;
    }

    public Date obtenerFechaCreacion() {
        return this.fechaGeneracion;
    }

    public ArrayList<Visita> obtenerVisitas() {
        return this.visitas;
    }
  
  
    // metodo de ayuda para el toString
    public String describirEstado() {
        switch (estado) {
            case ESTADO_PENDIENTE:
                return "PENDIENTE";
            case ESTADO_EN_EJECUCION:
                return "EN_EJECUCION";
            case ESTADO_COMPLETADO:
                return "COMPLETADO";
            default:
                return "";
        }
    }
  
	public boolean equals(OrdenRetiro obj) {
        return (this.estado==obj.estado) && (this.destino.equals(obj.destino)) && (this.pedidoOrigen.equals(obj.pedidoOrigen));
    }

    public void agregarVisita(Visita visita) {
        this.visitas.add(visita);
    }

    // Added methods to retrieve Donante and Vehiculo
    public Donante obtenerDonante() {
        return this.pedidoOrigen != null ? this.pedidoOrigen.obtenerDonante() : null;
    }

    public Vehiculo obtenerVehiculo() {
        return this.vehiculo;
    }

    public void asignarVehiculo(Vehiculo vehiculo) {
        this.vehiculo = vehiculo;
    }

    public Voluntario obtenerVoluntarioPrincipal() {
        return voluntarios.isEmpty() ? null : voluntarios.get(0);
    }

    public Voluntario getVoluntario() {
        return obtenerVoluntarioPrincipal();
    }

    public int getId() {
        return obtenerId();
    }

    public int getEstado() {
        return obtenerEstado();
    }

    public Date getFechaCreacion() {
        return obtenerFechaCreacion();
    }

    public Donante getDonante() {
        return obtenerDonante();
    }

    public Vehiculo getVehiculo() {
        return obtenerVehiculo();
    }

    public PedidosDonacion obtenerPedidoOrigen() {
        return this.pedidoOrigen;
    }
}
