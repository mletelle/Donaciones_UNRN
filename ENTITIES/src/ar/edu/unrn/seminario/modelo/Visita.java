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

    // constructor principal con todos los parametros
    public Visita(Date fecha, ResultadoVisita resultado, String obs, List<Bien> bienes) throws CampoVacioException, ObjetoNuloException {
        if (fecha == null) {
            throw new ObjetoNuloException("La fecha no puede ser nula");
        }
        if (obs == null || obs.trim().isEmpty())  {
            throw new CampoVacioException("La observacion no puede estar vacia");
        }
        if (bienes == null || bienes.isEmpty()) { // 
            throw new ObjetoNuloException("La lista de bienes no puede ser nula o vacia");
        }
        this.fechaDeVisita = fecha;
        this.resultado = resultado;
        this.observacion = obs;
        this.bienesRetirados = new ArrayList<>(bienes);
    }
    
    // constructor con todos los parametros (compatibilidad con codigo anterior que usaba int)
    public Visita(Date fecha, int estado, String obs, List<Bien> bienes) throws CampoVacioException, ObjetoNuloException {
        if (fecha == null) {
            throw new ObjetoNuloException("La fecha no puede ser nula");
        }
        if (obs == null || obs.trim().isEmpty()) {
            throw new CampoVacioException("La observacion no puede estar vacia");
        }
        if (bienes == null || bienes.isEmpty()) { // 
            throw new ObjetoNuloException("La lista de bienes no puede ser nula o vacia");
        }
        this.fechaDeVisita = fecha;
        //  int a enum para compatibilidad
        this.resultado = (estado == 2) ? ResultadoVisita.RECOLECCION_EXITOSA : ResultadoVisita.CANCELADO;
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
            throw new CampoVacioException("La fecha no puede ser nula o vacia");
        }
        if (observacion == null || observacion.trim().isEmpty()) {
            throw new CampoVacioException("La observacion no puede estar vacia");
        }
        if (bienes == null || bienes.isEmpty()) { // 
            throw new ObjetoNuloException("La lista de bienes no puede ser nula o vacia");
        }
        this.fechaDeVisita = new Date(); // asume que la fecha es actual
        this.resultado = ResultadoVisita.RECOLECCION_EXITOSA;
        this.observacion = observacion;
        this.bienesRetirados = new ArrayList<>(bienes);
    }
    public Visita(Date fecha, ResultadoVisita resultado, String obs) throws CampoVacioException, ObjetoNuloException {
        if (fecha == null) {
            throw new ObjetoNuloException("La fecha no puede ser nula");
        }
        if (obs == null || obs.trim().isEmpty())  {
            throw new CampoVacioException("La observacion no puede estar vacia");
        }
        this.fechaDeVisita = fecha;
        this.resultado = resultado;
        this.observacion = obs;
        this.bienesRetirados = new ArrayList<>(); // lista vacia
    }
    public Visita(Date fecha, String obs) throws CampoVacioException, ObjetoNuloException {
        if (fecha == null) {
            throw new ObjetoNuloException("La fecha no puede ser nula");
        }
        if (obs == null || obs.trim().isEmpty())  {
            throw new CampoVacioException("La observacion no puede estar vacia");
        }
        this.fechaDeVisita = fecha;
        this.resultado = ResultadoVisita.RECOLECCION_EXITOSA;
        this.observacion = obs;
        this.bienesRetirados = new ArrayList<>(); // lista vacia
    }
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
}