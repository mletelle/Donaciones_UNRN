package ar.edu.unrn.seminario.modelo;

import java.util.ArrayList;
import java.util.List;
import ar.edu.unrn.seminario.exception.CampoVacioException;
import ar.edu.unrn.seminario.exception.ObjetoNuloException;

public class Usuario {
    
    private String usuario;
    private String contrasena;
    private String nombre;
    private String apellido;
    private String dni; // String para permitir validaciones flexibles
    private String email;
    private String direccion; 
    private Rol rol;
    private boolean activo;

    // Atributos específicos encapsulados
    private String contacto; // Beneficiario
    private OrdenEntrega ordenEntregaActual; // Beneficiario
    private ArrayList<PedidosDonacion> pedidos; // Donante
    private ArrayList<OrdenRetiro> ordenesAsignadas; // Voluntario

    public static final int ROL_ADMIN = 1;
    public static final int ROL_VOLUNTARIO = 2;
    public static final int ROL_DONANTE = 3;
    public static final int ROL_BENEFICIARIO = 4;

    public Usuario(String usuario, String contrasena, String nombre, String email, Rol rol, String apellido, String dni, String direccion, String contacto) throws CampoVacioException, ObjetoNuloException {
        if (usuario == null || usuario.isEmpty()) throw new CampoVacioException("Usuario obligatorio.");
        if (contrasena == null || contrasena.isEmpty()) throw new CampoVacioException("Contraseña obligatoria.");
        if (dni == null || dni.isEmpty()) throw new CampoVacioException("DNI obligatorio.");
        if (rol == null) throw new ObjetoNuloException("Rol no puede ser nulo.");

        this.usuario = usuario;
        this.contrasena = contrasena;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.rol = rol;
        this.dni = dni;
        this.direccion = direccion;
        this.contacto = contacto;
        this.activo = true;

        if (esDonante()) this.pedidos = new ArrayList<>();
        if (esVoluntario()) this.ordenesAsignadas = new ArrayList<>();
    }
    
    // Constructor simplificado para compatibilidad con DAOs antiguos si es necesario
    public Usuario(String usuario, String contrasena, String nombre, String email, Rol rol, String apellido, int dni, String direccion) throws Exception {
       this(usuario, contrasena, nombre, email, rol, apellido, String.valueOf(dni), direccion, null);
    }

    public boolean esAdministrador() {
    	return this.rol.getCodigo() == ROL_ADMIN;
    	}
    public boolean esVoluntario() {
    	return this.rol.getCodigo() == ROL_VOLUNTARIO;
    	}
    public boolean esDonante() {
    	return this.rol.getCodigo() == ROL_DONANTE;
    	}
    public boolean esBeneficiario() { 
    	return this.rol.getCodigo() == ROL_BENEFICIARIO;
    	}

    public void agregarPedido(PedidosDonacion pedido) {
        if (esDonante() && this.pedidos != null) this.pedidos.add(pedido);
    }

    public void asignarOrdenRetiro(OrdenRetiro orden) {
        if (esVoluntario() && this.ordenesAsignadas != null) this.ordenesAsignadas.add(orden);
    }
    
    public void asignarOrdenEntrega(OrdenEntrega orden) {
        if (esBeneficiario()) this.ordenEntregaActual = orden;
    }

    public String getUsuario() {
    	return usuario;
    	}
    public String getContrasena() {
    	return contrasena; 
    	}
    public String getNombre() {
    	return nombre; }
    public String getApellido() {
    	return apellido; 
    	}
    public String getDni() {
    	return dni;
    	}
    public String getEmail() {
    	return email; 
    	}
    public String getDireccion() {
    	return direccion;
    	}
    public Rol getRol() {
    	return rol; 
    	}
    public String getContacto() {
    	return contacto;
    	}
    public boolean isActivo() {
    	return activo; 
    	}
    public void activar() {
    	this.activo = true;
    	}
    public void desactivar() {
    	this.activo = false; 
    }
    public String obtenerEstado() { 
    	return activo ? "ACTIVO" : "INACTIVO";
    }
    
    // Métodos alias para compatibilidad
    public String obtenerDireccion() {
    	return direccion; 
    	}
    public String obtenerNombre() {
    	return nombre; 
    	}
    public String obtenerApellido() {
    	return apellido;
}

    @Override
    public String toString() {
        return nombre + " " + apellido + " (" + rol.getNombre() + ")";
    }
}