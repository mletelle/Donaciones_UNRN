package ar.edu.unrn.seminario.modelo;

import java.util.Date;

import ar.edu.unrn.seminario.exception.CampoVacioException;
import ar.edu.unrn.seminario.exception.ReglaNegocioException;

public class Bien {

    private int id;
    private TipoBien tipo;
    private int cantidad;
    private CategoriaBien categoria;
    private boolean perecedero;
    private Date fecVec = null;
    private Date fechaIngreso = new Date();
    private String estado;
    private String descripcion;
    private Vehiculo vehiculo;
    
    private EstadoBien estadoInventario; 
    
    
    public Bien(TipoBien tipo, int cantidad, CategoriaBien categoria) throws CampoVacioException {
        if (tipo == null) throw new CampoVacioException("El tipo no puede ser nulo.");
        if (categoria == null) throw new CampoVacioException("La categoria no puede ser nula.");
        if (cantidad <= 0) throw new CampoVacioException("La cantidad debe ser mayor a cero.");
        
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.categoria = categoria;
        this.estadoInventario = EstadoBien.PENDIENTE;
    }

    public Bien(TipoBien tipo, int cantidad, CategoriaBien categoria, Vehiculo vehiculo) throws CampoVacioException {
        this(tipo, cantidad, categoria);
        this.vehiculo = vehiculo;
    }
    
    public EstadoBien getEstadoInventario() {
        return estadoInventario;
    }

    public void setEstadoInventario(EstadoBien estadoInventario) {
        this.estadoInventario = estadoInventario;
    }
    
    public TipoBien obtenerTipo() {
        return tipo;
    }

    public int obtenerCantidad() {
        return cantidad;
    }

    public CategoriaBien obtenerCategoria() {
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
	
	public void actualizarDatos(int cantidad, String descripcion, Date fechaVencimiento) throws ReglaNegocioException {
	    if (cantidad < 0) 
	        throw new ReglaNegocioException("La cantidad no puede ser negativa.");
	    
	    this.cantidad = cantidad;
	    this.descripcion = descripcion;
	    this.fecVec = fechaVencimiento;
	    
	    validarReglasDeInventario();
	}

	private void validarReglasDeInventario() throws ReglaNegocioException {

	    boolean requiereVencimiento = (categoria == CategoriaBien.ALIMENTOS || categoria == CategoriaBien.MEDICAMENTOS);
	    
	    if (requiereVencimiento) {
	        if (fecVec == null) {
	            throw new ReglaNegocioException("La fecha de vencimiento es obligatoria para alimentos y medicamentos.");
	        }
	        if (fecVec.before(new Date())) {
	            throw new ReglaNegocioException("El bien está vencido. Debe ser una fecha posterior a hoy.");
	        }
	    }
	}
    
    @Override
    public String toString() {
        String desc = (descripcion != null && !descripcion.isEmpty()) ? descripcion : "Sin descripcion";
        return desc + " (" + categoria.toString() + ") x " + cantidad;
    }

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		Bien other = (Bien) obj;
		// dos bienes se consideran iguales si tienen el mismo tipo, cantidad y categoria
		return cantidad == other.cantidad && categoria == other.categoria && tipo == other.tipo;
	}

}
