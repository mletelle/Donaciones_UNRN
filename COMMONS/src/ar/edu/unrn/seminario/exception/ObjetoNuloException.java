package ar.edu.unrn.seminario.exception;
//  indicar que un objeto nulo fue encontrado donde no debe
public class ObjetoNuloException extends Exception {

    public ObjetoNuloException(String message) {
        super(message);
    }
}