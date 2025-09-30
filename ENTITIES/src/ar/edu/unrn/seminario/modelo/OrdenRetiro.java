package ar.edu.unrn.seminario.modelo;

import java.util.ArrayList;

public class OrdenRetiro {

    // variables y catalogos
    private static int secuencia = 0;//para el id
    private static final int ESTADO_PENDIENTE = 1;
    private static final int ESTADO_EN_EJECUCION = 2;
    private static final int ESTADO_COMPLETADO = 3;

    // atributos
    private int id;
    private int estado;
    private Ubicacion destino;
    private ArrayList<Colaborador> colaboradores;
    private ArrayList<Visita> visitas;
    private PedidosDonacion pedidoOrigen;

    // constructor con todos los parametros
    public OrdenRetiro(PedidosDonacion pedido, Ubicacion destino) {
        this.id = ++secuencia;
        this.estado = ESTADO_PENDIENTE;
        this.destino = destino;
        this.pedidoOrigen = pedido;
        this.colaboradores = new ArrayList<Colaborador>();
        this.visitas = new ArrayList<Visita>();
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
  
    // metodos para cambiar el estado
    public void comenzar() {
        estado = ESTADO_EN_EJECUCION;
    }

    public void finalizar() {
        estado = ESTADO_COMPLETADO;
    }
  
    // agregar visita
    public void agregarVisita(Visita v) {
        visitas.add(v);
    }
  
    // getters
    public int obtenerEstado() {
        return estado;
    }

    public ArrayList<Visita> obtenerVisitas() {
      return visitas;
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
  
    @Override
    public String toString() {
        return "Orden#" + id + " -> " + destino + ": " + describirEstado();
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
  
    // metodo para imprimir el detalle de la orden
    public String imprimirDetalle(int nroVivienda) {//no es tostring porque recibe nroVivienda
        StringBuilder sb = new StringBuilder();
        sb.append("OrdenDeRetiro").append(id)
                .append(" Vivienda: ").append(nroVivienda)
                .append(". Voluntario: ");
        Colaborador v = obtenerVoluntario();
        sb.append(v != null ? v.obtenerNombre() + " " + v.obtenerApellido() : " ")
                .append(" (Estado: ").append(describirEstado()).append("):\n");

        if (visitas.isEmpty()) {
            sb.append("sin visitas\n");
        } else {
            for (int i = 0; i < visitas.size(); i++) {
                Visita vi = visitas.get(i);
                sb.append("  Visita ").append(i + 1)
                        .append(": Fecha: ").append(vi.obtenerFechaFormateada())
                        .append("\n    Obs.: ").append(vi.obtenerObservacion()).append("\n");
            }
        }
        return sb.toString();
    }

	public boolean equals(OrdenRetiro obj) {
        return (this.estado==obj.estado) && (this.destino.equals(obj.destino)) && (this.pedidoOrigen.equals(obj.pedidoOrigen));
    }
}
