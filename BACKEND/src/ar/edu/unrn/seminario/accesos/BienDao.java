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
	
	// Asociar bien a orden de entrega y cambiar su estado
	void asociarAOrdenEntrega(int idBien, int idOrdenEntrega, String nuevoEstado, Connection conn) throws SQLException;

	
	List<Bien> findByOrdenEntrega(int idOrdenEntrega, Connection conn) throws SQLException;
	
	
	int create(Bien bien, int idPedidoOriginal, Connection conn) throws SQLException;
}
