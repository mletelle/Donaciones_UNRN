package ar.edu.unrn.seminario.modelo;

import java.util.ArrayList;
import java.util.Date;

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
    private ArrayList<Colaborador> colaboradores;
    private PedidosDonacion pedidoOrigen;
    
    


    // constructor con todos los parametros
    public OrdenRetiro(PedidosDonacion pedido, Ubicacion destino) {
        this.estado = ESTADO_PENDIENTE;
        this.destino = destino;
        this.pedidoOrigen = pedido;
        this.colaboradores = new ArrayList<Colaborador>();
        pedido.asignarOrden(this);
    }

    // metodos
    // asignacion de voluntario
    public void asignarVoluntario(Colaborador c) {
        colaboradores.add(c);
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
    public Colaborador obtenerVoluntario() {
      if (colaboradores.isEmpty()) {
          return null; // No hay colaboradores disponibles
      }
      return colaboradores.get(0); // Devuelve el primero de la lista
    }
  
  
    // metodo de ayuda para el toString
    private String describirEstado() {
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
}
