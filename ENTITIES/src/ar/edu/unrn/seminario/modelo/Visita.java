package ar.edu.unrn.seminario.modelo;

import java.util.ArrayList; 
import java.util.Date;
import java.util.List;
import java.time.LocalDateTime;

import ar.edu.unrn.seminario.exception.CampoVacioException;
import ar.edu.unrn.seminario.exception.ObjetoNuloException;

public class Visita {
    private Date fechaDeVisita;
    private ResultadoVisita resultado;
    private String observacion;
    private ArrayList<Bien> bienesRetirados;
    private PedidosDonacion pedidoRelacionado; // referencia al pedido de esta visita
    
    public Visita(LocalDateTime fechaHora, ResultadoVisita resultado, String obs) throws CampoVacioException, ObjetoNuloException {
        if (fechaHora == null) {
            throw new ObjetoNuloException("La fecha no puede ser nula");
        }
        if (obs == null || obs.trim().isEmpty()) {
            throw new CampoVacioException("La observacion no puede estar vacia");
        }
        this.fechaDeVisita = java.util.Date.from(fechaHora.atZone(java.time.ZoneId.systemDefault()).toInstant());
        this.resultado = resultado;
        this.observacion = obs;
        this.bienesRetirados = new ArrayList<>();
    }
    public Visita(LocalDateTime fechaHora, String obs) throws CampoVacioException, ObjetoNuloException {
        if (fechaHora == null) {
            throw new ObjetoNuloException("La fecha no puede ser nula");
        }
        if (obs == null || obs.trim().isEmpty()) {
            throw new CampoVacioException("La observacion no puede estar vacia");
        }
        this.fechaDeVisita = java.util.Date.from(fechaHora.atZone(java.time.ZoneId.systemDefault()).toInstant());
        this.resultado = ResultadoVisita.RECOLECCION_EXITOSA;
        this.observacion = obs;
        this.bienesRetirados = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "Visita " + fechaDeVisita + ", " + describirEstado();
    }
    // metodo para describir el estado de la visita, uso interno
    private String describirEstado() {
        return this.resultado.toString();
    }

    // helper formateador de fecha: dia/mes/anioo hora:minuto
    // se evita crear una clase extra 
    // solo tiene sentido dentro de Visita
    private static final java.text.SimpleDateFormat fecha = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");

    // getters
    public String obtenerFechaFormateada() {
        return fecha.format(fechaDeVisita);
    }

    public String obtenerObservacion() {
        return observacion;
    }
    
    public ResultadoVisita obtenerResultado() {
        return resultado;
    }
    
	public void realizar() {
		this.resultado = ResultadoVisita.RECOLECCION_EXITOSA;
	}
	public void cancelar() {
		this.resultado = ResultadoVisita.CANCELADO;
	}
	public boolean equals (Visita obj2) {
		return this.fechaDeVisita==obj2.fechaDeVisita && this.resultado==obj2.resultado;
	}
	public List<Bien> obtenerBienes() {
		return new ArrayList<>(bienesRetirados);
	}
	// obtener el vehiculo 
	public Vehiculo obtenerVehiculo() {
		return bienesRetirados.isEmpty() ? null : bienesRetirados.get(0).obtenerVehiculo();
	}
	
	// establecer el pedido relacionado con esta visita
	public void setPedidoRelacionado(PedidosDonacion pedido) {
		this.pedidoRelacionado = pedido;
	}
	
	// obtener el pedido relacionado con esta visita
	public PedidosDonacion getPedidoRelacionado() {
		return this.pedidoRelacionado;
	}
}