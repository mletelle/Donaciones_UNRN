package ar.edu.unrn.seminario.exception;

/**
 * Excepcion para indicar que el usuario es invalido.
 * Esto puede incluir: usuario no encontrado, credenciales incorrectas, o usuario inactivo.
 */
public class UsuarioInvalidoException extends Exception {

    public UsuarioInvalidoException(String message) {
        super(message);
    }

    public UsuarioInvalidoException(String message, Throwable cause) {
        super(message, cause);
    }

    public UsuarioInvalidoException(Throwable cause) {
        super(cause);
    }
}