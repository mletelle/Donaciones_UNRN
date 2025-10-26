package ar.edu.unrn.seminario.modelo;

import java.util.ArrayList; 
import java.util.Date;
import java.util.List;


import ar.edu.unrn.seminario.exception.CampoVacioException;
import ar.edu.unrn.seminario.exception.ObjetoNuloException;

public class Visita {
    // estados de la visita 
    private static final int ESTADO_PENDIENTE = 1;
    private static final int ESTADO_REALIZADA = 2;
    private static final int ESTADO_CANCELADA = 3;

    private Date fechaDeVisita;
    private int estado;
    private String observacion;
    private ArrayList<Bien> bienesRetirados;

    // constructor principal con todos los parametros
    public Visita(Date fecha, String obs, List<Bien> bienes) throws CampoVacioException, ObjetoNuloException {
        if (fecha == null) {
            throw new ObjetoNuloException("La fecha no puede ser nula");
        }
        if (obs == null || obs.trim().isEmpty())  {
            throw new CampoVacioException("La observación no puede estar vacía");
        }
        if (bienes == null || bienes.isEmpty()) {
            throw new ObjetoNuloException("La lista de bienes no puede ser nula o vacía");
        }
        this.fechaDeVisita = fecha;
        this.estado = ESTADO_PENDIENTE;
        this.observacion = obs;
        this.bienesRetirados = new ArrayList<>(bienes);
    }
    public Visita(Date fecha, int estado, String obs, List<Bien> bienes) throws CampoVacioException, ObjetoNuloException {
        if (fecha == null) {
            throw new ObjetoNuloException("La fecha no puede ser nula");
        }
        if (obs == null || obs.trim().isEmpty()) {
            throw new CampoVacioException("La observación no puede estar vacía");
        }
        if (bienes == null || bienes.isEmpty()) {
            throw new ObjetoNuloException("La lista de bienes no puede ser nula o vacía");
        }
        this.fechaDeVisita = fecha;
        this.estado = estado;
        this.observacion = obs;
        this.bienesRetirados = new ArrayList<>(bienes);
    }

    // constructor con estado y bienes, sin observacion
    public Visita(Date fecha, int estado, List<Bien> bienes) throws CampoVacioException, ObjetoNuloException {
        this(fecha, estado, "", bienes);
    }
    // constructor con fecha, observacion y bienes
    public Visita(String fecha, String observacion, List<Bien> bienes) throws CampoVacioException, ObjetoNuloException {
        if (fecha == null || fecha.trim().isEmpty()) {
            throw new CampoVacioException("La fecha no puede ser nula o vacía");
        }
        if (observacion == null || observacion.trim().isEmpty()) {
            throw new CampoVacioException("La observación no puede estar vacía");
        }
        if (bienes == null || bienes.isEmpty()) {
            throw new ObjetoNuloException("La lista de bienes no puede ser nula o vacía");
        }
        this.fechaDeVisita = new Date(); // Asume que la fecha es actual
        this.estado = ESTADO_PENDIENTE;
        this.observacion = observacion;
        this.bienesRetirados = new ArrayList<>(bienes);
    }

    // 
    @Override
    public String toString() {
        return "Visita " + fechaDeVisita + ", " + describirEstado();
    }
    // metodo para describir el estado de la visita, uso interno
    private String describirEstado() {
        switch (estado) {
            case ESTADO_PENDIENTE:
                return "PENDIENTE";
            case ESTADO_REALIZADA:
                return "REALIZADA";
            case ESTADO_CANCELADA:
                return "CANCELADA";
            default:
                return "";
        }
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
	public void realizar() {
		this.estado=2;
	}
	public void cancelar() {
		this.estado=3;
	}
	public boolean equals (Visita obj2) {
		return this.fechaDeVisita==obj2.fechaDeVisita && this.estado==obj2.estado;
	}
	public List<Bien> obtenerBienes() {
		return new ArrayList<>(bienesRetirados);
	}
}
