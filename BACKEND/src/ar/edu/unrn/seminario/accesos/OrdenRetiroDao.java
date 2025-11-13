package ar.edu.unrn.seminario.accesos;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import ar.edu.unrn.seminario.modelo.OrdenRetiro;

public interface OrdenRetiroDao {
	
	int create(OrdenRetiro orden, Connection conn) throws SQLException;
	
	void update(OrdenRetiro orden, Connection conn) throws SQLException;
	
	OrdenRetiro findById(int idOrden, Connection conn) throws SQLException;
	
	List<OrdenRetiro> findByEstado(String estado, Connection conn) throws SQLException;
	
	List<OrdenRetiro> findAll(Connection conn) throws SQLException;
	
	List<OrdenRetiro> findByVoluntario(String username, Connection conn) throws SQLException;
	
}
