package ar.edu.unrn.seminario.dto;

import java.time.LocalDate;

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

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTipo() { return tipo; }
    public void setTipo(int tipo) { this.tipo = tipo; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public int getCategoria() { return categoria; }
    public void setCategoria(int categoria) { this.categoria = categoria; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public int getPeso() { return peso; }
    public void setPeso(int peso) { this.peso = peso; }

    public String getCategoriaTexto() { return categoriaTexto; }
    public void setCategoriaTexto(String categoriaTexto) { this.categoriaTexto = categoriaTexto; }

    public String getEstadoTexto() { return estadoTexto; }
    public void setEstadoTexto(String estadoTexto) { this.estadoTexto = estadoTexto; }

    public String getVencimientoTexto() { return vencimientoTexto; }
    public void setVencimientoTexto(String vencimientoTexto) { this.vencimientoTexto = vencimientoTexto; }
}