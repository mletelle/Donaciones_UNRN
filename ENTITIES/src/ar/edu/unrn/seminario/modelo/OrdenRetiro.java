package ar.edu.unrn.seminario.modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import ar.edu.unrn.seminario.exception.CampoVacioException;
import ar.edu.unrn.seminario.exception.ObjetoNuloException;

public class OrdenRetiro {

    // variables
    private static int secuencia = 0;//para el id

    // atributos
    private LocalDateTime fechaGeneracion = LocalDateTime.now();
    private EstadoOrden estado;
    private Ubicacion destino;
    private ArrayList<Usuario> voluntarios; // ahora es Usuario, antes voluntario
    private List<PedidosDonacion> pedidos;
    private ArrayList<Visita> visitas;
    private int id;
    private Vehiculo vehiculo;

    // Constructores
    public OrdenRetiro(List<PedidosDonacion> pedidos, Ubicacion dest) throws ObjetoNuloException {
        if (pedidos == null || pedidos.isEmpty()) {
            throw new ObjetoNuloException("La lista de pedidos no puede ser nula o vacia.");
        }
        this.id = ++secuencia;
        this.estado = EstadoOrden.PENDIENTE;
        this.destino = dest;
        this.pedidos = new ArrayList<>(pedidos);
        this.voluntarios = new ArrayList<Usuario>(); // MODIFICADO ahora anda
        this.visitas = new ArrayList<Visita>();
        // asignar esta orden a cada pedido
        for (PedidosDonacion pedido : this.pedidos) {
            pedido.asignarOrden(this);
        }
    }

    // Getters
    public String obtenerNombreEstado() {
        return describirEstado();
    }
    
    public EstadoOrden obtenerEstadoOrden() {
        return this.estado;
    }
    
    public Usuario obtenerPrimerVoluntario() { 
        if (voluntarios.isEmpty()) {
            return null; // no hay voluntarios disponibles
        }
        return voluntarios.get(0); // devuelve el primero de la lista
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
      
      public Usuario obtenerDonante() { // ahora devuelve Usuario
          return (!this.pedidos.isEmpty() && this.pedidos.get(0) != null) ? this.pedidos.get(0).obtenerDonante() : null;
      }
      
      public List<PedidosDonacion> obtenerPedidos() {
          return new ArrayList<>(this.pedidos);
      }
      
      public PedidosDonacion obtenerPedidoPorId(int idPedido) {
          for (PedidosDonacion pedido : this.pedidos) {
              if (pedido.obtenerId() == idPedido) {
                  return pedido;
              }
          }
          return null;
      }

      public Vehiculo obtenerVehiculo() {
          return this.vehiculo;
      }

      public Usuario obtenerVoluntarioPrincipal() { 
          return voluntarios.isEmpty() ? null : voluntarios.get(0);
      }

      public Usuario getVoluntario() { 
          return obtenerVoluntarioPrincipal();
      }

      public int getId() {
          return obtenerId();
      }

      public LocalDateTime getFechaCreacion() {
          return obtenerFechaCreacion();
      }

      public Usuario getDonante() { 
          return obtenerDonante();
      }

      public Vehiculo getVehiculo() {
          return obtenerVehiculo();
      }
      
      // Setters
      public void asignarVehiculo(Vehiculo vehiculo) {
          this.vehiculo = vehiculo;
      }
      
      // asignacion de voluntario
      public void asignarVoluntario(Usuario voluntario) { // ahora recibe Usuario
          if (this.voluntarios == null) {
              this.voluntarios = new ArrayList<>();
          }
          this.voluntarios.add(voluntario);
      }
    
    // metodos
    //  para actualizar el estado automaticamente basado en los pedidos hijos
    public void actualizarEstadoAutomatico() {
        if (this.pedidos == null || this.pedidos.isEmpty()) {
            return; // si hay pedidos, no se actualiza
        }
        
        boolean todosCompletados = true;
        boolean algunoEnEjecucion = false;
        
        for (PedidosDonacion pedido : this.pedidos) {
            EstadoPedido estadoPedido = pedido.obtenerEstadoPedido();
            if (estadoPedido != EstadoPedido.COMPLETADO) {
                todosCompletados = false;
            }
            if (estadoPedido == EstadoPedido.EN_EJECUCION) {
                algunoEnEjecucion = true;
            }
        }
        
        // si todos los pedidos estan completados, la orden esta completada
        if (todosCompletados) {
            this.estado = EstadoOrden.COMPLETADO;
        }
        // si al menos uno esta en ejecucion, o si hay visitas registradas
        else if (algunoEnEjecucion || !this.visitas.isEmpty()) {
            this.estado = EstadoOrden.EN_EJECUCION;
        }
        // si no, permanece en PENDIENTE
    }

    // metodo para marcar como completado
	public boolean estaCompletada() {
		return estado == EstadoOrden.COMPLETADO;
	}

	// metodo para agregar una visita
    public void agregarVisita(Visita visita) {
        this.visitas.add(visita);
        // cambiar a EN_EJECUCION si estaba PENDIENTE
        if (this.estado == EstadoOrden.PENDIENTE) {
            this.estado = EstadoOrden.EN_EJECUCION;
        }
    }

    // metodo para registrar una visita
    public void registrarVisita(LocalDateTime fechaHora, String observacion) throws ObjetoNuloException, CampoVacioException {
        if (fechaHora == null) {
            throw new ObjetoNuloException("La fecha de la visita no puede ser nula.");
        }
        if (observacion == null || observacion.isEmpty()) {
            throw new ObjetoNuloException("La observacion no puede ser nula o vacia.");
        }
        Visita nuevaVisita = new Visita(fechaHora, observacion);
        if (this.visitas == null) {
            this.visitas = new ArrayList<>();
        }
        this.visitas.add(nuevaVisita);
        // cambiar a EN_EJECUCION si estaba PENDIENTE
        if (this.estado == EstadoOrden.PENDIENTE) {
            this.estado = EstadoOrden.EN_EJECUCION;
        }
    }
    
    // metodo de ayuda para el toString
    public String describirEstado() {
        return this.estado.toString();
    }
  
	public boolean equals(OrdenRetiro obj) {
        return (this.estado==obj.estado) && (this.destino.equals(obj.destino)) && (this.pedidos.equals(obj.pedidos));
    }
    
}
