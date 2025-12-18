package ar.edu.unrn.seminario.modelo;

import java.util.ArrayList;
import java.util.List;
import ar.edu.unrn.seminario.exception.CampoVacioException;
import ar.edu.unrn.seminario.exception.ObjetoNuloException;

public class Usuario {
    
    private String usuario;
    private String contrasena;
    private String nombre;
    private String email;
    private Rol rol;
    private boolean activo;
    private String apellido;
    private int dni;
    private String direccion;
    
    // Atributos especificos para el beneficiario
    private String necesidad;
    private Integer personasACargo;
    private String prioridad; 

    
    private ArrayList<PedidosDonacion> pedidos;
    private ArrayList<OrdenRetiro> ordenesAsignadas;

    
    public Usuario(String usuario, String contrasena, String nombre, String email, Rol rol, 
                   String apellido, int dni, String direccion, String necesidad, 
                   Integer personasACargo, String prioridad) throws CampoVacioException, ObjetoNuloException {
        
        if (esVacio(usuario)) throw new CampoVacioException("El usuario es obligatorio.");
        if (esVacio(contrasena)) throw new CampoVacioException("La contraseña es obligatoria.");
        if (esVacio(nombre)) throw new CampoVacioException("El nombre es obligatorio.");
        if (rol == null) throw new ObjetoNuloException("El rol no puede ser nulo.");
        if (dni <= 0) throw new CampoVacioException("DNI inválido.");

        if (rol.getCodigo() == Rol.ROL_DONANTE && esVacio(direccion)) {
            throw new CampoVacioException("La dirección es obligatoria para los Donantes.");
        }

        if (rol.getCodigo() != Rol.ROL_BENEFICIARIO && esVacio(apellido)) {
            throw new CampoVacioException("El apellido es obligatorio.");
        }

        this.usuario = usuario;
        this.contrasena = contrasena;
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
        this.apellido = apellido;
        this.dni = dni;
        this.direccion = direccion;
        this.necesidad = necesidad;
        this.personasACargo = personasACargo;
        this.prioridad = prioridad;
        this.activo = true;
        
        this.pedidos = new ArrayList<>();
        this.ordenesAsignadas = new ArrayList<>();
    }

    
    public Usuario(String usuario, String contrasena, String nombre, String email, Rol rol, String apellido, int dni, String direccion) throws CampoVacioException, ObjetoNuloException {
        this(usuario, contrasena, nombre, email, rol, apellido, dni, direccion, null, 0, null);
    }

    private boolean esVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }

    // Getters y Setters
    public String getUsuario() {
    	return usuario;
}
    public void setUsuario(String usuario) {
    	this.usuario = usuario; 
    	}

    public String getContrasena() { 
    	return contrasena;
    	}
    public void setContrasena(String contrasena) {
    	this.contrasena = contrasena;
    	}

    public String getNombre() {
    	return nombre; 
    	}
    public void setNombre(String nombre) {
    	this.nombre = nombre;
}

    public String getApellido() { 
    	return apellido; 
    	}
    public void setApellido(String apellido) { 
    	this.apellido = apellido; 
    	}

    public int getDni() {
    	return dni; 
    	}
    public void setDni(int dni) { 
    	this.dni = dni;
    	}

    public String obtenerDireccion() { 
    	return direccion; 
    	}
    public void setDireccion(String direccion) {
    	this.direccion = direccion;
    	}

    public String getEmail() { 
    	return email;
    	}
    public void setEmail(String email) {
    	this.email = email; 
    	}

    public Rol getRol() {
    	return rol; 
    	}
    public void setRol(Rol rol) {
    	this.rol = rol; 
    	}

    public boolean isActivo() { 
    	return activo;
    	}
    public String obtenerEstado() { 
    	return isActivo() ? "ACTIVO" : "INACTIVO";
    	}
    
    public void activar() {
    	this.activo = true; 
    	}
    public void desactivar() {
    	this.activo = false; 
    	}

    // Atributos Beneficiario
    public String getNecesidad() {
    	return necesidad;
    	}
    public void setNecesidad(String necesidad) { 
    	this.necesidad = necesidad; 
    	}

    public Integer getPersonasACargo() {
    	return personasACargo;
    	}
    public void setPersonasACargo(Integer personasACargo) { 
    	this.personasACargo = personasACargo; 
    	}

    public String getPrioridad() { 
    	return prioridad; 
    	}
    public void setPrioridad(String prioridad) { 
    	this.prioridad = prioridad; 
    	}

    // Listas
    public List<PedidosDonacion> getPedidos() {
    	return pedidos != null ? java.util.Collections.unmodifiableList(pedidos) : java.util.Collections.emptyList();
    	}
    public List<OrdenRetiro> getOrdenesAsignadas() {
    	return ordenesAsignadas != null ? java.util.Collections.unmodifiableList(ordenesAsignadas) : java.util.Collections.emptyList();
    	}
    public void agregarPedido(PedidosDonacion pedido) {
    	this.pedidos.add(pedido);
    	}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        // se usa solo DNI para identificar unicidad de usuarios en el dominio
        result = prime * result + dni; 
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Usuario other = (Usuario) obj;
        // usuarios con mismo DNI se consideran iguales (unicidad por DNI)
        return this.dni == other.dni;
    }

    @Override
    public String toString() {
        return nombre + " " + (apellido != null ? apellido : "") + " (DNI " + dni + ")";
    }
    
    public Ubicacion getUbicacionEntidad() {
        if (this.direccion == null || this.direccion.isEmpty()) return null; 
        return new Ubicacion(this.direccion, "N/A", "N/A", 0.0, 0.0);
    }
    
    public boolean esDonante() {
        return this.rol != null && this.rol.getCodigo() == Rol.ROL_DONANTE;
    }
}