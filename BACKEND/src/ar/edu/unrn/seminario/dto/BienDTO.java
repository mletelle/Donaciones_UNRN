package ar.edu.unrn.seminario.dto;

import java.time.LocalDate;

public class BienDTO {
	// Constantes Categoria
    public static final int CATEGORIA_ROPA = 1;
    public static final int CATEGORIA_MUEBLES = 2;
    public static final int CATEGORIA_ALIMENTOS = 3;
    public static final int CATEGORIA_ELECTRODOMESTICOS = 4;
    public static final int CATEGORIA_HERRAMIENTAS = 5;
    public static final int CATEGORIA_JUGUETES = 6;
    public static final int CATEGORIA_LIBROS = 7;
    public static final int CATEGORIA_MEDICAMENTOS = 8;
    public static final int CATEGORIA_HIGIENE = 9;
    public static final int CATEGORIA_OTROS = 10;

    // Constantes tipo
    public static final int TIPO_NUEVO = 1;
    public static final int TIPO_USADO = 2;

    // Atributos
    private int id;
    private int tipo;
    private int cantidad;
    private int categoria;
    private String descripcion;
    private LocalDate fechaVencimiento;
    private int peso;

    // Constructores
    public BienDTO(int tipo, int cantidad, int categoria, String descripcion, LocalDate fechaVencimiento) {
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.fechaVencimiento = fechaVencimiento;
    }

    // Getters
    public String getDescripcion() {
        return descripcion;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public int getTipo() {
        return tipo;
    }
    
    public int getCantidad() {
        return cantidad;
    }
    
    public int getCategoria() {
        return categoria;
    }
    
    public int getId() {
        return id;
    }
    
    public int getPeso() {
        return peso;
    }
    
    // Setters
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public void setCategoria(int categoria) {
        this.categoria = categoria;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPeso(int peso) {
        this.peso = peso;
    }
}