package ar.edu.unrn.seminario.accesos;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import ar.edu.unrn.seminario.modelo.Bien;

public interface BienDao {
	
	void createBatch(List<Bien> bienes, int idPedido, Connection conn) throws SQLException;
	
}
