package ar.edu.unrn.seminario.modelo;

public class Beneficiario {
    
    //atributos privados
    private String nombre;
    private String apellido;
    private int dni; 
    private Ubicacion ubicacion;
    private String contacto;
    private OrdenEntrega ordenActual;

    //CONSTRUCTOR
      public Beneficiario(String nombre, String apellido, int dni, Ubicacion ubicacion) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (ubicacion == null) {
            throw new IllegalArgumentException("El beneficiario necesita una ubicación inicial.");
        }
        
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.ubicacion = ubicacion;
        //contacto y ordenActual pueden nacer nulos 
    }

    //METODOS DE NEGOCIO
    public void cambiarUbicacion(Ubicacion nuevaUbicacion) {
        if (nuevaUbicacion == null) {
            throw new IllegalArgumentException("La nueva ubicación no puede ser nula.");
        }
        this.ubicacion = nuevaUbicacion;
    }

    public void asignar(OrdenEntrega orden) {
        if (orden == null) {
             throw new IllegalArgumentException("No se puede asignar una orden nula.");
        }
        if (this.ordenActual != null) {
        }
        this.ordenActual = orden;
    }

    public void notificar(String mensaje) {
        System.out.println("Notificando a " + this.nombre + ": " + mensaje);
    }
    
    //GETTERS
    
    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public int getDni() {
        return dni;
    }

    public Ubicacion getUbicacion() {
        return ubicacion;
    }

    public String getContacto() {
        return contacto;
    }

    public OrdenEntrega getOrdenActual() {
        return ordenActual;
    }
    public void setContacto(String contacto) {
        this.contacto = contacto;
    }
}