package ar.edu.unrn.seminario.modelo;

import java.util.Date;

import ar.edu.unrn.seminario.exception.CampoVacioException;

public class Bien {

    // constantes tipo
	private static final int TIPO_ALIMENTO = 1;
	private static final int TIPO_ROPA = 2;
	private static final int TIPO_MOBILIARIO = 3;
	private static final int TIPO_HIGIENE = 4;

	// constantes categoria (tipo de bien)
	private static final int CATEGORIA_ROPA = 1;
	private static final int CATEGORIA_MUEBLES = 2;
	private static final int CATEGORIA_ALIMENTOS = 3;
	private static final int CATEGORIA_ELECTRODOMESTICOS = 4;
	private static final int CATEGORIA_HERRAMIENTAS = 5;
	private static final int CATEGORIA_JUGUETES = 6;
	private static final int CATEGORIA_LIBROS = 7;
	private static final int CATEGORIA_MEDICAMENTOS = 8;
	private static final int CATEGORIA_HIGIENE = 9;
	private static final int CATEGORIA_OTROS = 10;
    
	private int id; // nuevo atributo
    private int tipo;
	private int cantidad;
    private int categoria;
    private boolean perecedero;
    private Date fecVec = null;
    private Date fechaIngreso = new Date();
    private String estado;
    private String descripcion;
    private Vehiculo vehiculo;
    

    
    // Estados del inventario
    public static final String ESTADO_PENDIENTE = "PENDIENTE";
    public static final String ESTADO_EN_STOCK = "EN_STOCK";
    public static final String ESTADO_ENTREGADO = "ENTREGADO";

    private String estadoInventario; 
    
    
    public Bien(int tipo, int cantidad, int categoria) throws CampoVacioException {
        if (cantidad <= 0) {
            throw new CampoVacioException("La cantidad debe ser mayor a cero.");
        }
        if (categoria < CATEGORIA_ROPA || categoria > CATEGORIA_OTROS) {
            throw new CampoVacioException("La categoria es invalida.");
        }
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.categoria = categoria;
        this.estadoInventario = ESTADO_PENDIENTE;
    }

    public Bien(int tipo, int cantidad, int categoria, Vehiculo vehiculo) throws CampoVacioException {
        this(tipo, cantidad, categoria);
        this.vehiculo = vehiculo;
    }
    
    public String getEstadoInventario() {
        return estadoInventario;
    }

    public void setEstadoInventario(String estadoInventario) {
        this.estadoInventario = estadoInventario;
    }
    
    
    public int obtenerTipo() {
        return tipo;
    }

    public int obtenerCantidad() {
        return cantidad;
    }

    public int obtenerCategoria() {
        return categoria;
    }
    
    public Date getFecVec() {
		return fecVec;
	}
    
	public Date getFechaIngreso() {
		return fechaIngreso;
	}
	
	public String getEstado() {
		return estado;
	}
	
    public boolean isPerecedero() {
		return perecedero;
	}
    
    public Vehiculo obtenerVehiculo() {
        return vehiculo;
    }
    
	public void setFecVec(Date fecVec) {
		this.fecVec = fecVec;
	}

	public void setFechaIngreso(Date fechaIngreso) {
		this.fechaIngreso = fechaIngreso;
	}
	
	public void setEstado(String estado) {
		this.estado = estado;
	}

	public void setPerecedero(boolean perecedero) {
		this.perecedero = perecedero;
	}
	
	public String getDescripcion() {
		return descripcion;
	}
	
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	
    public void asignarVehiculo(Vehiculo vehiculo) {
        this.vehiculo = vehiculo;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public int getCantidad() {
		return cantidad;
	}

	public void setCantidad(int cantidad) {
		this.cantidad = cantidad;
	}
    
    
    //metodos de ayuda para el toString
    private String describirTipo() {
        switch (tipo) {
            case TIPO_ALIMENTO:
                return "ALIMENTO";
            case TIPO_ROPA:
                return "ROPA";
            case TIPO_MOBILIARIO:
                return "MOBILIARIO";
            case TIPO_HIGIENE:
                return "HIGIENE";
            default:
                return "OTRO";
        }
    }
  
    private String describirCategoria() {
        switch (categoria) {
            case CATEGORIA_ROPA:
                return "Ropa";
            case CATEGORIA_MUEBLES:
                return "Muebles";
            case CATEGORIA_ALIMENTOS:
                return "Alimentos";
            case CATEGORIA_ELECTRODOMESTICOS:
                return "Electrodomesticos";
            case CATEGORIA_HERRAMIENTAS:
                return "Herramientas";
            case CATEGORIA_JUGUETES:
                return "Juguetes";
            case CATEGORIA_LIBROS:
                return "Libros";
            case CATEGORIA_MEDICAMENTOS:
                return "Medicamentos";
            case CATEGORIA_HIGIENE:
                return "Higiene";
            default:
                return "Otros";
        }
    }
    
    @Override
        public String toString() {
            return cantidad + " x " + describirTipo() + describirCategoria();
        }

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		Bien other = (Bien) obj;
		return cantidad == other.cantidad && categoria == other.categoria && tipo == other.tipo;
	}

    
}
