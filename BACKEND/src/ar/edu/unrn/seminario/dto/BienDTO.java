package ar.edu.unrn.seminario.dto;

import java.time.LocalDate;
import java.time.ZoneId; 
import java.time.format.DateTimeFormatter; // <--- AGREGADO PARA EL FORMATO
import java.util.Date;

public class BienDTO {
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

    public static final int TIPO_NUEVO = 1;
    public static final int TIPO_USADO = 2;

    private int id;
    private int tipo;
    private int cantidad;
    private int categoria;
    private String descripcion;
    private LocalDate fechaVencimiento; 
    private int peso;
    
    private String categoriaTexto;
    private String estadoTexto;     
    private String vencimientoTexto;

    public BienDTO(String categoriaTexto, String descripcion, int cantidad, String estadoTexto, String vencimientoTexto) {
        this.categoriaTexto = categoriaTexto;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.estadoTexto = estadoTexto;
        this.vencimientoTexto = vencimientoTexto;
    }

    public BienDTO(int tipo, int cantidad, int categoria, String descripcion, LocalDate fechaVencimiento) {
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.fechaVencimiento = fechaVencimiento; 
    }
    
    public BienDTO() {}

    //GETTERS Y SETTERS

    public String getCategoriaTexto() { return categoriaTexto; }
    public String getEstadoTexto() { return estadoTexto; }
    public String getVencimientoTexto() { return vencimientoTexto; }

    public String getDescripcion() { return descripcion; }
    public int getCantidad() { return cantidad; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public int getTipo() { return tipo; }
    public int getCategoria() { return categoria; }
    public int getId() { return id; }
    public int getPeso() { return peso; }

    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public void setFechaVencimiento(LocalDate fechaVencimiento) { 
        this.fechaVencimiento = fechaVencimiento; 
        if (fechaVencimiento != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            this.vencimientoTexto = fechaVencimiento.format(formatter);
        } else {
            this.vencimientoTexto = "-";
        }
    }
    
    public void setTipo(int tipo) { this.tipo = tipo; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public void setCategoria(int categoria) { this.categoria = categoria; }
    public void setId(int id) { this.id = id; }
    public void setPeso(int peso) { this.peso = peso; }

    public Date getVencimiento() {
        if (this.fechaVencimiento == null) {
            return null;
        }
        return Date.from(this.fechaVencimiento.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public void setVencimiento(Date nuevaFechaVencimiento) {
        if (nuevaFechaVencimiento == null) {
            this.setFechaVencimiento(null);
        } else {
            this.setFechaVencimiento(nuevaFechaVencimiento.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate());
        }
    }
}