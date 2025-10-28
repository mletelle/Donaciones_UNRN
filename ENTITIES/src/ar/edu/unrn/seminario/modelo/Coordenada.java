package ar.edu.unrn.seminario.modelo;

public class Coordenada {

    private double latitud;
    private double longitud;
    //crea una coordenada con latitud y longitud
    public Coordenada(double latitud, double longitud) {
        this.latitud = latitud;
        this.longitud = longitud;
    }
  
    //getters
    public double obtenerLatitud() {
        return latitud;
    }

    public double obtenerLongitud() {
        return longitud;
    }

    @Override
    public String toString() {
        return "(" + latitud + ", " + longitud + ")";
    }
    public boolean equals(Coordenada obj) {
        return (latitud == obj.latitud && longitud == obj.longitud);//si son iguales todos los atributos, son iguales
    }
    @Override 
    public int hashCode() {
        return java.util.Objects.hash(latitud, longitud); 
    }
}
