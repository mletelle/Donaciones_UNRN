package ar.edu.unrn.seminario.modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import ar.edu.unrn.seminario.exception.CampoVacioException;
import ar.edu.unrn.seminario.exception.ObjetoNuloException;

public class OrdenRetiro {

    private static int secuencia = 0;

    private LocalDateTime fechaGeneracion = LocalDateTime.now();
    private EstadoOrden estado;
    private Ubicacion destino;
    private ArrayList<Usuario> voluntarios;
    private List<PedidosDonacion> pedidos;
    private ArrayList<Visita> visitas;
    private int id;
    private Vehiculo vehiculo;

    public OrdenRetiro(List<PedidosDonacion> pedidos, Ubicacion dest) throws ObjetoNuloException {
        if (pedidos == null || pedidos.isEmpty()) {
            throw new ObjetoNuloException("La lista de pedidos no puede ser nula o vacia.");
        }
        this.id = ++secuencia;
        this.estado = EstadoOrden.PENDIENTE;
        this.destino = dest;
        this.pedidos = new ArrayList<>(pedidos);
        this.voluntarios = new ArrayList<Usuario>();
        this.visitas = new ArrayList<Visita>();
        // cada pedido debe saber que pertenece a esta orden para mantener la coherencia
        for (PedidosDonacion pedido : this.pedidos) {
            pedido.asignarOrden(this);
        }
    }
    
    public OrdenRetiro(int id) {
        this.id = id;
        this.pedidos = new ArrayList<>();
        this.voluntarios = new ArrayList<>();
        this.visitas = new ArrayList<>();
        this.estado = EstadoOrden.PENDIENTE;
    }

    public OrdenRetiro(int id, LocalDateTime fechaGeneracion, EstadoOrden estado, Ubicacion dest, List<PedidosDonacion> pedidos) throws ObjetoNuloException {
        if (pedidos == null) {
             throw new ObjetoNuloException("La lista de pedidos no puede ser nula.");
        }
        this.id = id;
        this.fechaGeneracion = fechaGeneracion;
        this.estado = estado;
        this.destino = dest;
        this.pedidos = new ArrayList<>(pedidos);
        this.voluntarios = new ArrayList<Usuario>();
        this.visitas = new ArrayList<Visita>();
        
        for (PedidosDonacion pedido : this.pedidos) {
            pedido.asignarOrden(this);
        }
    }

    public String obtenerNombreEstado() {
        return describirEstado();
    }
    
    public EstadoOrden obtenerEstadoOrden() {
        return this.estado;
    }

    public Ubicacion obtenerDestino() {
        return this.destino;
    }
    
    public Usuario obtenerPrimerVoluntario() { 
        if (voluntarios.isEmpty()) {
            return null;
        }
        return voluntarios.get(0);
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
      
      public Usuario obtenerDonante() {
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
      
      // setters
      public void setId(int id) {
          this.id = id;
      }
      
      // eliminado setestado publico - usar transicionara() privado
      
      /**
       * metodo exclusivo para hidratacion desde bd
       * publico porque dao esta en otro proyecto
       */
      public void forzarEstadoDesdeBD(EstadoOrden estado) {
          this.estado = estado;
      }
      
      /**
       * unico punto de entrada para cambios de estado
       * garantiza que nadie viole las reglas del enum
       */
      private void transicionarA(EstadoOrden nuevoEstado) {
          if (!this.estado.esTransicionValida(nuevoEstado)) {
              throw new IllegalStateException(
                  String.format("no se puede pasar la orden %d de %s a %s", 
                  this.id, this.estado, nuevoEstado)
              );
          }
          this.estado = nuevoEstado;
      }
      
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
      

    
    //  para actualizar el estado automaticamente basado en los pedidos hijos
    private void verificarCompletitud() {
        if (this.pedidos == null || this.pedidos.isEmpty()) {
            return;
        }
        
        boolean todosCompletados = this.pedidos.stream()
            .allMatch(p -> p.obtenerEstadoPedido() == EstadoPedido.COMPLETADO);
        
        if (todosCompletados && this.estado != EstadoOrden.COMPLETADO) {
            transicionarA(EstadoOrden.COMPLETADO);
        }
    }

    // metodo para marcar como completado
	public boolean estaCompletada() {
		return estado == EstadoOrden.COMPLETADO;
	}

	// metodo para agregar una visita
    public void agregarVisita(Visita visita) {
        this.visitas.add(visita);
        // cambiar a en_ejecucion si estaba pendiente usando transicion segura
        if (this.estado == EstadoOrden.PENDIENTE) {
            transicionarA(EstadoOrden.EN_EJECUCION);
        }
        verificarCompletitud();
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
        // cambiar a en_ejecucion si estaba pendiente usando transicion segura
        if (this.estado == EstadoOrden.PENDIENTE) {
            transicionarA(EstadoOrden.EN_EJECUCION);
        }
        verificarCompletitud();}
    public String describirEstado() {
        return this.estado.name();
    }
  
	public boolean equals(OrdenRetiro obj) {
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final OrdenRetiro other = (OrdenRetiro) obj;
        return this.id == other.id; // Comparar por ID
    }
    
}