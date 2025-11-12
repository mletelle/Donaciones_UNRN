package ar.edu.unrn.seminario.modelo;

import java.util.Date;

import ar.edu.unrn.seminario.exception.CampoVacioException;

public class Bien {

    // constantes tipo
	private static final int TIPO_ALIMENTO = 1;
	private static final int TIPO_ROPA = 2;
	private static final int TIPO_MOBILIARIO = 3;
	private static final int TIPO_HIGIENE = 4;

	private static final int CATEGORIA_BAJA = 1;
	private static final int CATEGORIA_MEDIA = 2;
	private static final int CATEGORIA_ALTA = 3;
    
    //atributos
    private int tipo;
    private int cantidad;
    private int categoria;
    private boolean perecedero;
    private Date fecVec = new Date();
    private Date fechaIngreso = new Date();
    private String estado;
    private Vehiculo vehiculo;
    
    //constructores 
    public Bien(int tipo, int cantidad, int categoria) throws CampoVacioException {
        if (cantidad <= 0) {
            throw new CampoVacioException("La cantidad debe ser mayor a cero.");
        }
        if (categoria < CATEGORIA_BAJA || categoria > CATEGORIA_ALTA) {
            throw new CampoVacioException("La categoria es invalida.");
        }
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.categoria = categoria;
    }
    
    public Bien(String tipo, int cantidad, String cat) throws CampoVacioException {
        this(cantidad, cantidad, cat.equalsIgnoreCase("baja") ? CATEGORIA_BAJA : cat.equalsIgnoreCase("media") ? CATEGORIA_MEDIA : CATEGORIA_ALTA);
        if (tipo == null || tipo.isEmpty()) {
            throw new CampoVacioException("El tipo no puede estar vacio.");
        }
    }

    public Bien(int tipo, int cantidad, int categoria, Vehiculo vehiculo) throws CampoVacioException {
        this(tipo, cantidad, categoria);
        this.vehiculo = vehiculo;
    }
    
    // Getters 
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
    
	// Setters
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
	
    public void asignarVehiculo(Vehiculo vehiculo) {
        this.vehiculo = vehiculo;
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
            case CATEGORIA_BAJA:
                return "BAJA";
            case CATEGORIA_MEDIA:
                return "MEDIA";
            case CATEGORIA_ALTA:
                return "ALTA";
            default:
                return "";
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
