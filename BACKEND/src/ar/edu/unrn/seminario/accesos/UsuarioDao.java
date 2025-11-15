package ar.edu.unrn.seminario.accesos;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import ar.edu.unrn.seminario.modelo.Usuario;

public interface UsuarioDao {
	
	void create(Usuario usuario, Connection conn) throws SQLException;

	void update(Usuario usuario, Connection conn) throws SQLException;

	Usuario find(String username, Connection conn) throws SQLException;

	List<Usuario> findAll(Connection conn) throws SQLException;
	
	List<Usuario> findByRol(int codigoRol, Connection conn) throws SQLException;

	// agregado
	Usuario findByDni(int dni, Connection conn) throws SQLException;
}
