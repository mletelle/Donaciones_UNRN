package ar.edu.unrn.seminario.exception;
// excepcion para campos vacios en formularios
public class CampoVacioException extends Exception {

    public CampoVacioException(String message) { 
        super(message);
    }
}