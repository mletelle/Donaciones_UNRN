package ar.edu.unrn.seminario.modelo;

import java.util.Date;

import ar.edu.unrn.seminario.exception.CampoVacioException;

public class Bien {

    // constantes 
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
    
    //constructores con todos los parametros
    public Bien(int tipo, int cantidad, int categoria) throws CampoVacioException {
        if (cantidad <= 0) {
            throw new CampoVacioException("La cantidad debe ser mayor a cero.");
        }
        if (categoria < CATEGORIA_BAJA || categoria > CATEGORIA_ALTA) {
            throw new CampoVacioException("La categoría es inválida.");
        }
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.categoria = categoria;
    }
    public Bien(String tipo, int cantidad, String cat) throws CampoVacioException {
        this(cantidad, cantidad, cat.equalsIgnoreCase("baja") ? CATEGORIA_BAJA : cat.equalsIgnoreCase("media") ? CATEGORIA_MEDIA : CATEGORIA_ALTA);
        if (tipo == null || tipo.isEmpty()) {
            throw new CampoVacioException("El tipo no puede estar vacío.");
        }
    }
    public Bien(String tipo) throws CampoVacioException {
        if (tipo == null || tipo.trim().isEmpty()) {
            throw new CampoVacioException("El tipo no puede ser nulo o vacío.");
        }
        this.tipo = TIPO_ALIMENTO; 
        this.cantidad = 1; 
        this.categoria = CATEGORIA_MEDIA;
    }
    //getters - setters
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
	public void setFecVec(Date fecVec) {
		this.fecVec = fecVec;
	}
	public Date getFechaIngreso() {
		return fechaIngreso;
	}
	public void setFechaIngreso(Date fechaIngreso) {
		this.fechaIngreso = fechaIngreso;
	}
	public String getEstado() {
		return estado;
	}
	public void setEstado(String estado) {
		this.estado = estado;
	}
    public boolean isPerecedero() {
		return perecedero;
	}
	public void setPerecedero(boolean perecedero) {
		this.perecedero = perecedero;
	}
    
    //calcular volumen: depende del tipo de bien
    //se utilizaria para saber el tipo de vehiculo necesario
    //para transportar los bienes
    // no se usa en el main pero se deja por si se necesita
    public double calcularVolumen() {
        if (tipo == TIPO_MOBILIARIO)// mobiliario ocupa medio metro cubico por unidad
            return cantidad * 0.5; 
        if (tipo == TIPO_ALIMENTO)
            return cantidad * 0.1; // alimentos ocupan 0.1 metro cubico por unidad
        return cantidad * 0.05; // ropa e higiene ocupan 0.05 metro cubico por unidad
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
    // Sin implementacion
    public void aceptarItem() {
     System.out.println("Item aceptado");
    }

    public void rechazarItem() {
     System.out.println("Item rechazado");
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
