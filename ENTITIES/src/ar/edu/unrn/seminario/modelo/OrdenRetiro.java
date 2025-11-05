package ar.edu.unrn.seminario.modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;

import ar.edu.unrn.seminario.exception.CampoVacioException;
import ar.edu.unrn.seminario.exception.ObjetoNuloException;

public class OrdenRetiro {

    // variables y catalogos
    private static int secuencia = 0;//para el id
    private static final int ESTADO_PENDIENTE = 1;
    private static final int ESTADO_EN_EJECUCION = 2;
    private static final int ESTADO_COMPLETADO = 3;

    // atributos
    private LocalDateTime fechaGeneracion = LocalDateTime.now();
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
        this.destino = new Ubicacion(dest, "", "", 0.0, 0.0); // valores por defecto
        this.pedidoOrigen = pedido;
        this.voluntarios = new ArrayList<>();
        this.visitas = new ArrayList<>();
        pedido.asignarOrden(this);
    }
    public OrdenRetiro(Voluntario voluntario, String tipoVehiculo) {
        this.id = ++secuencia; // Inicializar el ID
        this.estado = ESTADO_PENDIENTE;
        this.voluntarios = new ArrayList<>();
        this.visitas = new ArrayList<>();
        this.voluntarios.add(voluntario);
        this.destino = null; // por defecto
        this.pedidoOrigen = null; // defecto xq se usa en otros metodos
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
    public String obtenerNombreEstado() {
        return describirEstado();
    }

    public void actualizarEstado(String nuevoEstado) {
        switch (nuevoEstado.toUpperCase()) {
            case "PENDIENTE":
                this.estado = ESTADO_PENDIENTE;
                break;
            case "EN_EJECUCION":
                this.estado = ESTADO_EN_EJECUCION;
                break;
            case "COMPLETADO":
                this.estado = ESTADO_COMPLETADO;
                break;
            default:
                throw new IllegalArgumentException("Estado desconocido: " + nuevoEstado);
        }
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

    public LocalDateTime obtenerFechaCreacion() {
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

    public void registrarVisita(LocalDateTime fechaHora, String observacion) throws ObjetoNuloException, CampoVacioException {
        if (fechaHora == null) {
            throw new ObjetoNuloException("La fecha de la visita no puede ser nula.");
        }
        if (observacion == null || observacion.isEmpty()) {
            throw new ObjetoNuloException("La observación no puede ser nula o vacía.");
        }
        Visita nuevaVisita = new Visita(fechaHora, observacion);
        if (this.visitas == null) {
            this.visitas = new ArrayList<>();
        }
        this.visitas.add(nuevaVisita);
    }

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

    public LocalDateTime getFechaCreacion() {
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
