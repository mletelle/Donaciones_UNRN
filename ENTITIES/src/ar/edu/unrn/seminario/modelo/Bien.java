package ar.edu.unrn.seminario.modelo;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import ar.edu.unrn.seminario.exception.CampoVacioException;
import ar.edu.unrn.seminario.exception.ReglaNegocioException;

public class Bien {

    private int id;
    private int cantidad;
    private CategoriaBien categoria;
    private boolean perecedero;
    private Date fecVec = null;
    private Date fechaIngreso = new Date();
    private String estado;
    private String descripcion;
    private Vehiculo vehiculo;
    
    private EstadoBien estadoInventario; 
    
    
    public Bien(int cantidad, CategoriaBien categoria) throws CampoVacioException {
        if (categoria == null) throw new CampoVacioException("La categoria no puede ser nula.");
        if (cantidad < 0) throw new CampoVacioException("La cantidad debe ser mayor a cero.");
        
        this.cantidad = cantidad;
        this.categoria = categoria;
        this.estadoInventario = EstadoBien.PENDIENTE;
    }

    public Bien(int cantidad, CategoriaBien categoria, Vehiculo vehiculo) throws CampoVacioException {
        this(cantidad, categoria);
        this.vehiculo = vehiculo;
    }
    
    public EstadoBien getEstadoInventario() {
        return estadoInventario;
    }

    public void setEstadoInventario(EstadoBien estadoInventario) {
        this.estadoInventario = estadoInventario;
    }

    public int obtenerCantidad() {
        return cantidad;
    }

    public CategoriaBien obtenerCategoria() {
        return categoria;
    }
    
    public Date getFecVec() {
		return fecVec != null ? new Date(fecVec.getTime()) : null;
	}
    
	public Date getFechaIngreso() {
		return fechaIngreso != null ? new Date(fechaIngreso.getTime()) : null;
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
		this.fecVec = fecVec != null ? new Date(fecVec.getTime()) : null;
	}

	public void setFechaIngreso(Date fechaIngreso) {
		this.fechaIngreso = fechaIngreso != null ? new Date(fechaIngreso.getTime()) : null;
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
    
    public void descontarStock(int cantidadSolicitada) throws ReglaNegocioException {
        if (cantidadSolicitada <= 0) {
            throw new IllegalArgumentException("la cantidad debe ser positiva");
        }
        if (this.estadoInventario != EstadoBien.EN_STOCK) {
            throw new ReglaNegocioException("el bien " + this.descripcion + " no esta disponible");
        }
        if (cantidadSolicitada > this.cantidad) {
            throw new ReglaNegocioException("stock insuficiente para: " + this.descripcion);
        }
        this.cantidad -= cantidadSolicitada;
    }
    
    public void validarFechaVencimiento(LocalDate fechaVencimiento) throws ReglaNegocioException {
        if (fechaVencimiento == null) return;
        
        boolean requiereVencimiento = this.categoria == CategoriaBien.ALIMENTOS 
                                    || this.categoria == CategoriaBien.MEDICAMENTOS;
        
        if (requiereVencimiento && fechaVencimiento.isBefore(LocalDate.now())) {
            throw new ReglaNegocioException("esa fecha esta vencida, debe ser posterior a " + LocalDate.now());
        }
    }
    
    public void actualizarDatos(int cantidad, String descripcion, LocalDate fechaVencimiento) throws ReglaNegocioException {
        if (cantidad < 0) {
            throw new ReglaNegocioException("la cantidad no puede ser negativa");
        }
        
        boolean requiereVencimiento = this.categoria == CategoriaBien.ALIMENTOS 
                                    || this.categoria == CategoriaBien.MEDICAMENTOS;
        
        if (requiereVencimiento && fechaVencimiento == null) {
            throw new ReglaNegocioException("la fecha de vencimiento es obligatoria para alimentos y medicamentos");
        }
        
        validarFechaVencimiento(fechaVencimiento);
        
        this.cantidad = cantidad;
        this.descripcion = descripcion;
        if (fechaVencimiento != null) {
            this.fecVec = Date.from(fechaVencimiento.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } else {
            this.fecVec = null;
        }
    }
    
    public void ingresar() throws ReglaNegocioException {
        if (this.estadoInventario != EstadoBien.PENDIENTE) {
            throw new ReglaNegocioException("solo se pueden ingresar al stock bienes pendientes, estado actual: " + this.estadoInventario);
        }
        this.estadoInventario = EstadoBien.EN_STOCK;
    }
    
    public void entregar() throws ReglaNegocioException {
        if (this.estadoInventario != EstadoBien.EN_STOCK) {
            throw new ReglaNegocioException("solo se pueden entregar bienes que esten en stock, estado actual: " + this.estadoInventario);
        }
        this.estadoInventario = EstadoBien.ENTREGADO;
    }
    
    public void darDeBaja(String motivo) throws ReglaNegocioException {
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new ReglaNegocioException("el motivo de baja no puede estar vacio");
        }
        this.estadoInventario = EstadoBien.BAJA;
        this.descripcion = (this.descripcion != null ? this.descripcion : "") + " [BAJA: " + motivo + "]";
    }
    
    public Bien fraccionarParaEntrega(int cantidadSolicitada) throws ReglaNegocioException, CampoVacioException {
        if (this.estadoInventario != EstadoBien.EN_STOCK) {
            throw new ReglaNegocioException("solo se pueden fraccionar bienes en stock, estado actual: " + this.estadoInventario);
        }
        if (cantidadSolicitada <= 0) {
            throw new ReglaNegocioException("la cantidad solicitada debe ser mayor a cero");
        }
        if (cantidadSolicitada > this.cantidad) {
            throw new ReglaNegocioException("stock insuficiente, disponible: " + this.cantidad + ", solicitado: " + cantidadSolicitada);
        }
        
        this.cantidad -= cantidadSolicitada;
        
        Bien bienFraccionado = new Bien(cantidadSolicitada, this.categoria);
        bienFraccionado.setDescripcion(this.descripcion);
        bienFraccionado.setEstadoInventario(EstadoBien.ENTREGADO);
        
        if (this.fecVec != null) {
            bienFraccionado.setFecVec(new Date(this.fecVec.getTime()));
        }
        if (this.fechaIngreso != null) {
            bienFraccionado.setFechaIngreso(new Date(this.fechaIngreso.getTime()));
        }
        
        return bienFraccionado;
    }
    
    public CategoriaBien getCategoria() {
        return categoria;
    }
    
    public LocalDate getFechaVencimiento() {
        return fecVec != null ? fecVec.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null;
    }
    
    public String obtenerDescripcion() {
        return descripcion;
    }
    
    private int idPedidoDonacion;
    
    public int getIdPedidoDonacion() {
        return idPedidoDonacion;
    }
    
    public void setIdPedidoDonacion(int idPedidoDonacion) {
        this.idPedidoDonacion = idPedidoDonacion;
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
		return cantidad == other.cantidad && categoria == other.categoria;
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(cantidad, categoria);
	}
}
