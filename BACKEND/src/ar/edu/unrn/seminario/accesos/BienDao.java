package ar.edu.unrn.seminario.accesos;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import ar.edu.unrn.seminario.modelo.Bien;

public interface BienDao {
	
	void createBatch(List<Bien> bienes, int idPedido, Connection conn) throws SQLException;

	void updateEstadoPorPedido(int idPedido, String nuevoEstado, Connection conn) throws SQLException;

	List<Bien> findByEstadoInventario(String estado, Connection conn) throws SQLException;
	
	// Buscar un bien específico para editarlo
    Bien findById(int id, Connection conn) throws SQLException;
    
    // Guardar los cambios
    void update(Bien bien, Connection conn) throws SQLException;
	
}
