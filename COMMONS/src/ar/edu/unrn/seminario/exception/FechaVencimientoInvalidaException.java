package ar.edu.unrn.seminario.exception;
// excepcion para fecha de vencimiento invalida para alimentos
public class FechaVencimientoInvalidaException extends Exception {

    public FechaVencimientoInvalidaException(String message) {
        super(message);
    }
}