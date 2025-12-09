package ar.edu.unrn.seminario.modelo;

import java.util.ArrayList;
import ar.edu.unrn.seminario.exception.ObjetoNuloException;

public class OrdenEntrega {

    private static int secuencia = 0;
    
    public static final int ESTADO_PENDIENTE = 1;
    public static final int ESTADO_EN_EJECUCION = 2;
    public static final int ESTADO_COMPLETADO = 3;
    public static final int ESTADO_CANCELADO = 4;

    private int id;
    private int estado;
    private Usuario voluntario; 
    private Usuario beneficiario; 
    private ArrayList<Visita> visitas;
    private PedidosDonacion pedidoOrigen;

    public OrdenEntrega(PedidosDonacion pedido, Usuario beneficiario) throws ObjetoNuloException {
        if (pedido == null) throw new ObjetoNuloException("Pedido nulo.");
        if (beneficiario == null) throw new ObjetoNuloException("Beneficiario nulo.");
        if (!beneficiario.esBeneficiario()) throw new IllegalArgumentException("Usuario no es Beneficiario.");

        this.id = ++secuencia;
        this.estado = ESTADO_PENDIENTE;
        this.pedidoOrigen = pedido;
        this.beneficiario = beneficiario;
        this.visitas = new ArrayList<>();
        
        this.beneficiario.asignarOrdenEntrega(this);
    }
    
    // Constructor para hidratación desde JDBC
    public OrdenEntrega(int id, int estado, Usuario beneficiario, Usuario voluntario, PedidosDonacion pedido) {
        this.id = id;
        this.estado = estado;
        this.beneficiario = beneficiario;
        this.voluntario = voluntario;
        this.pedidoOrigen = pedido;
        this.visitas = new ArrayList<>();
    }
    
    public void asignarVoluntario(Usuario v) throws ObjetoNuloException {
        if (v == null) throw new ObjetoNuloException("Voluntario nulo.");
        if (!v.esVoluntario()) throw new IllegalArgumentException("Usuario no es voluntario.");
        this.voluntario = v;
    }
    
    public int getId() { 
    	return id;
    	}
    public Usuario obtenerBeneficiario() {
    	return beneficiario;
    	}
    public Usuario obtenerVoluntario() {
    	return voluntario;
    	}
    public int obtenerEstado() { 
    	return estado;
    	}
    public PedidosDonacion getPedidoOrigen() {
    	return pedidoOrigen;
    	}
    public void marcarEntregada() {
    	this.estado = ESTADO_COMPLETADO; 
    	}
}