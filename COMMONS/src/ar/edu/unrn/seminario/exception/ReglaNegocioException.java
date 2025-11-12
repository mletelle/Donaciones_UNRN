package ar.edu.unrn.seminario.exception;
// para reglas de negocio
// permite diferenciar errores de negocio de errores comunes
// por ejemplo, se puede usar para indicar que no hay vehículos disponibles
public class ReglaNegocioException extends Exception {

    public ReglaNegocioException(String message) {
        super(message);
    }
}