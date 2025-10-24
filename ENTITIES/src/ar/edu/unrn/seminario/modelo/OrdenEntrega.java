package ar.edu.unrn.seminario.modelo;

import java.util.ArrayList;
import java.util.Date;

public class OrdenEntrega {

    // variables y catalogos
    private static int secuencia = 0;//para el id
    private static final int ESTADO_PENDIENTE = 1;
    private static final int ESTADO_EN_EJECUCION = 2;
    private static final int ESTADO_COMPLETADO = 3;
    private static final int ESTADO_CANCELADO = 4;
    

    // atributos
    private int id;
    private Beneficiario beneficiario;
    private Date fechaGeneracion = new Date();
    private int estado;
    private Ubicacion destino;
    private Colaborador voluntario;
    private ArrayList<Visita> visitas;
    private PedidosDonacion pedidoOrigen;
    private ArrayList<Bien> bienes;
    
    
    private Vehiculo v;
    private Date fechaEjecucion = new Date();
    private Date fechaProgramada = new Date();
    
    

    // constructor con todos los parametros
    public OrdenEntrega(PedidosDonacion pedido, Ubicacion destino) {
        this.id = ++secuencia;
        this.estado = ESTADO_PENDIENTE;
        this.destino = destino;
        this.pedidoOrigen = pedido;
        this.visitas = new ArrayList<Visita>();
    	ArrayList<Bien> bienes = new ArrayList<Bien>();
        /// pedido.asignarOrden(this);
    }
    private void asignarRecursos(Colaborador c, Vehiculo v) {
    	this.v = v;
    	asignarVoluntario(c);
    }

    // metodos
    // asignacion de voluntario
    public void asignarVoluntario(Colaborador c) {
        voluntario = c;
    }
  
    // actualizacion de estado
    public void actualizarEstado(int nuevoEstado) {
        this.estado = nuevoEstado;
    }
  
    // metodos para cambiar el estado
    public void iniciar() {
        estado = ESTADO_EN_EJECUCION;
    }

    public void marcarEntregada() {
        estado = ESTADO_COMPLETADO;
    }
    public void cancelar(String motivo) {
        estado = ESTADO_CANCELADO;
        /// sin implementación concreta
        System.out.println("Motivo de cancelacion: "+motivo);
    }
    public Colaborador obtenerVoluntario() {
    	return this.voluntario;
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
            case ESTADO_CANCELADO:
                return "CANCELADO";
            default:
                return "";
        }
    }

     private double VolumenTotal() {
        double sumaVolumen = 0.0;
        for(Bien bien : bienes) {
            sumaVolumen += bien.calcularVolumen();
        }
        return sumaVolumen;
    }
    public void quitarBien(Bien item) {
    	bienes.remove(item);
     }
     private void agregarItem(Bien item) {
    	 bienes.add(item);
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

	public boolean equals(OrdenEntrega obj) {
        return (this.estado==obj.estado) && (this.destino.equals(obj.destino)) && (this.pedidoOrigen.equals(obj.pedidoOrigen));
    }
}
