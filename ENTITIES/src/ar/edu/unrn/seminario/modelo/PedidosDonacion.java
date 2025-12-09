package ar.edu.unrn.seminario.modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import ar.edu.unrn.seminario.exception.CampoVacioException;
import ar.edu.unrn.seminario.exception.ObjetoNuloException;

public class PedidosDonacion {

    private static int secuencia = 0;
    private int id;
    private LocalDateTime fecha;
    private ArrayList<Bien> bienes;
    private int tipoVehiculo; // 1:Auto, 2:Camioneta, 3:Camion
    
    private Usuario donante; // Referencia a la clase Usuario unificada
    private OrdenRetiro ordenRetiro; 
    private EstadoPedido estadoPedido;

    public PedidosDonacion(LocalDateTime fecha, ArrayList<Bien> bienes, int tipoVehiculo, Usuario donante) throws CampoVacioException, ObjetoNuloException {
        if (fecha == null) throw new ObjetoNuloException("La fecha no puede ser nula.");
        if (bienes == null || bienes.isEmpty()) throw new CampoVacioException("La lista de bienes no puede estar vacía.");
        if (donante == null) throw new ObjetoNuloException("El donante no puede ser nulo.");
        if (!donante.esDonante()) throw new IllegalArgumentException("El usuario asignado no tiene rol de Donante.");

        this.id = ++secuencia;
        this.fecha = fecha;
        this.bienes = bienes;
        this.tipoVehiculo = tipoVehiculo;
        this.donante = donante;
        this.estadoPedido = EstadoPedido.PENDIENTE;
        
        // Vinculación bidireccional (opcional, pero recomendada)
        this.donante.agregarPedido(this);
    }

    // Getters
    public Usuario getDonante() { return donante; }
    public Usuario obtenerDonante() { return donante; }
    public int getId() { return id; }
    public LocalDateTime obtenerFecha() { return fecha; }
    public ArrayList<Bien> obtenerBienes() { return bienes; }
    public EstadoPedido obtenerEstadoPedido() { return estadoPedido; }
    
    // Métodos de estado
    public void marcarCompletado() { this.estadoPedido = EstadoPedido.COMPLETADO; }
    public void marcarEnEjecucion() { this.estadoPedido = EstadoPedido.EN_EJECUCION; }
    
    public void asignarOrden(OrdenRetiro o) {
        this.ordenRetiro = o;
    }
    
    // Helper para descripción
    public String describirTipoVehiculo() {
        switch (tipoVehiculo) {
            case 1: return "AUTO";
            case 2: return "CAMIONETA";
            case 3: return "CAMION";
            default: return "DESCONOCIDO";
        }
    }
}