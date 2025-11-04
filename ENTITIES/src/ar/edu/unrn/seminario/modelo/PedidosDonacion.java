package ar.edu.unrn.seminario.modelo;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ar.edu.unrn.seminario.exception.CampoVacioException;
import ar.edu.unrn.seminario.exception.ObjetoNuloException;

public class PedidosDonacion {

	// variables de clase
	private static int secuencia = 0;//para usarlo de id

	// ESTADOS DE PEDIDO 
	private static final int ESTADO_PENDIENTE = 1;
	private static final int ESTADO_EN_EJECUCION = 2;
	private static final int ESTADO_COMPLETADO = 3;

	// catalogos
	private static final int VEHICULO_AUTO = 1;
	private static final int VEHICULO_CAMIONETA = 2;
	private static final int VEHICULO_CAMION = 3;

	// atributos
	private int id;
	private Date fecha;
	private ArrayList<Bien> bienes;
	private int tipoVehiculo;
	private String observaciones;
	private Donante donante; // asociacion inversa
	private OrdenRetiro ordenRetiro; // 1 a 1
	private int estadoPedido; // Nuevo campo para guardar el estado del pedido

	// constructor con todos los parametros
	public PedidosDonacion(Date fecha, ArrayList<Bien> bienes, int tipoVehiculo, String observaciones, Donante d) throws CampoVacioException, ObjetoNuloException {
		if (fecha == null) {
			throw new ObjetoNuloException("La fecha no puede ser nula.");
		}
		if (bienes == null || bienes.isEmpty()) {
			throw new CampoVacioException("La lista de bienes no puede estar vacía.");
		}
		if (d == null) {
			throw new ObjetoNuloException("El donante no puede ser nulo.");
		}
		this.id = ++secuencia;//preincremta para arrancar desde el 1
		this.fecha = fecha;
		this.bienes = bienes;
		this.tipoVehiculo = tipoVehiculo;
		this.observaciones = observaciones;
		this.donante = d;
		this.estadoPedido = ESTADO_PENDIENTE; // Inicializar en PENDIENTE
	}
	public PedidosDonacion(Date fecha, ArrayList<Bien> bienes, String tipo, String observaciones, Donante d) throws CampoVacioException, ObjetoNuloException {
		this(fecha, bienes, tipo.equalsIgnoreCase("auto") ? VEHICULO_AUTO : tipo.equalsIgnoreCase("camioneta") ? VEHICULO_CAMIONETA : VEHICULO_CAMION, observaciones, d);
	}
	public PedidosDonacion(String tipo, ArrayList<Bien> bienes, String observaciones, Donante donante) throws CampoVacioException, ObjetoNuloException {
		this(new Date(), bienes, tipo.equalsIgnoreCase("auto") ? VEHICULO_AUTO : tipo.equalsIgnoreCase("camioneta") ? VEHICULO_CAMIONETA : VEHICULO_CAMION, observaciones, donante);
	}
	// constructor sin fecha (usa la actual)
	public PedidosDonacion(ArrayList<Bien> bienes, int tipoVehiculo, String observaciones, Donante d) throws CampoVacioException, ObjetoNuloException {
		this(new Date(), bienes, tipoVehiculo, observaciones, d);
	}

	// constructor to accept LocalDateTime
	public PedidosDonacion(LocalDateTime fecha, List<Bien> bienes, String tipoVehiculo, String observaciones, Donante donante) throws CampoVacioException, ObjetoNuloException {
		this(Date.from(fecha.atZone(ZoneId.systemDefault()).toInstant()), new ArrayList<>(bienes),
			 tipoVehiculo.equalsIgnoreCase("auto") ? VEHICULO_AUTO : tipoVehiculo.equalsIgnoreCase("camioneta") ? VEHICULO_CAMIONETA : VEHICULO_CAMION,
			 observaciones, donante);
	}
	// getters
	public int obtenerId() {
		return id;
	}

	public Date obtenerFecha() {
		return fecha;
	}

	public ArrayList<Bien> obtenerBienes() {
		return bienes;
	}

	public int obtenerTipoVehiculo() {
		return tipoVehiculo;
	}

	public Donante obtenerDonante() {
		return donante;
	}

	public OrdenRetiro obtenerOrden() {
		return ordenRetiro;
	}

	public String obtenerObservaciones() {
		return this.observaciones;
	}

	public String obtenerUbicacion() {
		// Assuming ubicacion is derived from donante or another attribute
		return this.donante != null ? this.donante.obtenerUbicacion() : "Ubicación no disponible";
	}

	public int getId() {
		return this.id;
	}

	public Donante getDonante() {
		return this.donante;
	}

	/**
	 * Devuelve el estado del pedido como String (para la GUI)
	 */
	public String obtenerEstado() {
		switch (this.estadoPedido) {
			case ESTADO_PENDIENTE:
				return "PENDIENTE";
			case ESTADO_EN_EJECUCION:
				return "EN_EJECUCION";
			case ESTADO_COMPLETADO:
				return "COMPLETADO";
			default:
				return "DESCONOCIDO";
		}
	}

	/**
	 * Devuelve el estado del pedido como int
	 */
	public int obtenerEstadoInt() {
		return this.estadoPedido;
	}

	public String obtenerDireccion() {
		return donante != null ? donante.obtenerUbicacion() : "Dirección no disponible";
	}

	// relacion con la orden
	public void asignarOrden(OrdenRetiro o) {
		this.ordenRetiro = o;
	}

	public void actualizarEstado(int nuevoEstado) {
		// En un caso real, aquí irían validaciones de transición de estado.
		if (nuevoEstado >= ESTADO_PENDIENTE && nuevoEstado <= ESTADO_COMPLETADO) {
			this.estadoPedido = nuevoEstado;
		} else {
			// Manejo de error simple para el modelo
			System.err.println("Advertencia: Intento de establecer un estado de pedido inválido: " + nuevoEstado);
		}
	}

	@Override
	public String toString() {
		return "Pedido#" + id
				+ " (" + fecha + ") de " + donante
				+ " Vehiculo: " + describirTipoVehiculo();
	}
	
	// metodo de ayuda para el toString
	public String describirTipoVehiculo() {
		switch (tipoVehiculo) {
			case VEHICULO_AUTO:
				return "AUTO";
			case VEHICULO_CAMIONETA:
				return "CAMIONETA";
			case VEHICULO_CAMION:
				return "CAMION";
			default:
				return "DESCONOCIDO";
		}
	}
	public boolean equals(PedidosDonacion obj) {
		return (fecha.equals(obj.fecha))&&
		(this.tipoVehiculo==obj.tipoVehiculo)&&
		(this.donante.equals(obj.donante));
	}	

	public static int getVehiculoAuto() {
		return VEHICULO_AUTO;
	}

	public static int getVehiculoCamioneta() {
		return VEHICULO_CAMIONETA;
	}

	public static int getVehiculoCamion() {
		return VEHICULO_CAMION;
	}
}