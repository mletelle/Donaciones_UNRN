package ar.edu.unrn.seminario.exception;

public class PersistenceException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public PersistenceException(String mensaje) {
		super(mensaje);
	}

	public PersistenceException(String mensaje, Throwable causa) {
		super(mensaje, causa);
	}
}
