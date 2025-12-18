package ar.edu.unrn.seminario.accesos;

import java.util.List;

import ar.edu.unrn.seminario.exception.PersistenceException;
import ar.edu.unrn.seminario.modelo.Usuario;
import ar.edu.unrn.seminario.modelo.Visita;

public interface VisitaDao {
	
	void create(Visita visita, int idOrden, int idPedido) throws PersistenceException;
	
	void registrarVisitaCompleta(int idOrdenRetiro, int idPedido, String resultado, String observacion) throws PersistenceException;
	
	List<Visita> findByVoluntario(Usuario voluntario) throws PersistenceException;
	
}
