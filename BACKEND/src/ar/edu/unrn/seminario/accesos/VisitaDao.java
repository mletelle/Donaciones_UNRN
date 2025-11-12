package ar.edu.unrn.seminario.accesos;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import ar.edu.unrn.seminario.modelo.Usuario;
import ar.edu.unrn.seminario.modelo.Visita;

public interface VisitaDao {
	
	void create(Visita visita, int idOrden, int idPedido, Connection conn) throws SQLException;
	
	List<Visita> findByVoluntario(Usuario voluntario, Connection conn) throws SQLException;
	
}
