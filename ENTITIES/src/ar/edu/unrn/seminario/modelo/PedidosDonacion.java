package ar.edu.unrn.seminario.modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import ar.edu.unrn.seminario.exception.CampoVacioException;
import ar.edu.unrn.seminario.exception.ObjetoNuloException;

public class PedidosDonacion {

    private static int secuencia = 0;
    private int id;
    private LocalDateTime fecha;
    private ArrayList<Bien> bienes;
    private int tipoVehiculo; // 1:Auto, 2:Camioneta, 3:Camion
    
    private Usuario donante; 
    private OrdenRetiro ordenRetiro; 
    private EstadoPedido estadoPedido;

    public PedidosDonacion(LocalDateTime fecha, ArrayList<Bien> bienes, int tipoVehiculo, Usuario donante) throws CampoVacioException, ObjetoNuloException {
        if (fecha == null) throw new ObjetoNuloException("La fecha no puede ser nula.");
        if (bienes == null || bienes.isEmpty()) throw new CampoVacioException("La lista de bienes no puede estar vacía.");
        if (donante == null) throw new ObjetoNuloException("El donante no puede ser nulo.");
        
        // Validación opcional
        // if (!donante.esDonante()) throw new IllegalArgumentException("El usuario asignado no tiene rol de Donante.");

        this.id = ++secuencia;
        this.fecha = fecha;
        this.bienes = bienes;
        this.tipoVehiculo = tipoVehiculo;
        this.donante = donante;
        this.estadoPedido = EstadoPedido.PENDIENTE;
    }

    // Este permite crear el objeto sin validar bienes al inicio y seteando el ID
    public PedidosDonacion(int id, LocalDateTime fecha, int tipoVehiculo, Usuario donante) {
        this.id = id;
        this.fecha = fecha;
        this.tipoVehiculo = tipoVehiculo;
        this.donante = donante;
        this.bienes = new ArrayList<>(); // Lista vacía para evitar NullPointerException
        this.estadoPedido = EstadoPedido.PENDIENTE; // Por defecto
    }

    public Usuario getDonante() { return donante; }
    public Usuario obtenerDonante() { return donante; }
    public int getId() { return id; }
    public int obtenerId() { return id; }
    public LocalDateTime obtenerFecha() { return fecha; }
    public ArrayList<Bien> obtenerBienes() { return bienes; }
    public EstadoPedido obtenerEstadoPedido() { return estadoPedido; }
    public OrdenRetiro obtenerOrden() { return ordenRetiro; } 

    public void setId(int id) { this.id = id; }
    
    public void setEstado(EstadoPedido estado) {
        this.estadoPedido = estado;
    }
    
    public void asignarOrden(OrdenRetiro o) {
        this.ordenRetiro = o;
    }

    public void marcarCompletado() { this.estadoPedido = EstadoPedido.COMPLETADO; }
    public void marcarEnEjecucion() { this.estadoPedido = EstadoPedido.EN_EJECUCION; }
    
    public String obtenerEstado() {
        return estadoPedido != null ? estadoPedido.toString() : "DESCONOCIDO";
    }

    public String describirTipoVehiculo() {
        switch (tipoVehiculo) {
            case 1: return "AUTO";
            case 2: return "CAMIONETA";
            case 3: return "CAMION";
            default: return "DESCONOCIDO";
        }
    }

    public static int convertirVehiculoAInt(String tipo) {
        if (tipo == null) return 0;
        String t = tipo.trim().toUpperCase();
        if ("AUTO".equals(t)) return 1;
        if ("CAMIONETA".equals(t)) return 2;
        if ("CAMION".equals(t)) return 3;
        return 0; 
    }
}