package ar.edu.unrn.seminario.accesos;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import ar.edu.unrn.seminario.modelo.Rol;

public interface RolDao {

	Rol find(Integer codigo, Connection conn) throws SQLException;

	List<Rol> findAll(Connection conn) throws SQLException;

}
