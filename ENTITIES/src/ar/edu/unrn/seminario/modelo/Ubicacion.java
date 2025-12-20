package ar.edu.unrn.seminario.modelo;

public class Ubicacion {
    
    private String direccion;
    private String zona;
    private String barrio;
    private Coordenada coordenada;

    public Ubicacion(String direccion, String zona, String barrio, Coordenada coord) {
        this.direccion = direccion;
        this.zona = zona;
        this.barrio = barrio;
        this.coordenada = coord;
    }

    public Ubicacion(String direccion, String zona, String barrio, double latitud, double longitud) {
        this(direccion, zona, barrio, new Coordenada(latitud, longitud));
    }

    public String obtenerDireccion() {
        return direccion;
    }

    public String obtenerZona() {
        return zona;
    }

    public String obtenerBarrio() {
        return barrio;
    }

    public Coordenada obtenerCoordenada() {
        return coordenada;
    }

    @Override
    public String toString() {
        return direccion + " - " + barrio + " (" + zona + ")";
    }

    public boolean equals(Ubicacion obj) {
        return direccion.equals(obj.direccion) &&
                zona.equals(obj.zona) &&
                barrio.equals(obj.barrio) &&
                coordenada.equals(obj.coordenada);
    }
    
}
