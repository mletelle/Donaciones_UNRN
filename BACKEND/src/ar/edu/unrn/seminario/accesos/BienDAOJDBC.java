package ar.edu.unrn.seminario.accesos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import ar.edu.unrn.seminario.modelo.Bien;

public class BienDAOJDBC implements BienDao {

	@Override
	public void createBatch(List<Bien> bienes, int idPedido, Connection conn) throws SQLException {
		PreparedStatement statement = null;
		try {
			statement = conn.prepareStatement(
					"INSERT INTO bienes(id_pedido_donacion, categoria, cantidad, tipo, descripcion, fecha_vencimiento) "
					+ "VALUES (?, ?, ?, ?, ?, ?)");
			// inserta los bienes uno por uno, toma los valores de cada bien y los asigna a la sentencia preparada
			for (Bien bien : bienes) {
				statement.setInt(1, idPedido);
				statement.setInt(2, bien.obtenerCategoria());
				statement.setInt(3, bien.obtenerCantidad());
				statement.setInt(4, bien.obtenerTipo());
				statement.setString(5, "");
				
				if (bien.getFecVec() != null) {
					statement.setDate(6, new java.sql.Date(bien.getFecVec().getTime()));
				} else {
					statement.setNull(6, java.sql.Types.DATE);
				}
				// AGREGA LA CONSULTA AL LOTE (NO LA EJECUTA)
				statement.addBatch();
				// statement.executeUpdate();
				// Iba a la base de datos 
			}
			// EJECUTA TODAS LAS CONSULTAS DEL LOTE EN UNA SOLA LLAMADA A LA BD
			statement.executeBatch();
		} finally {
			if (statement != null) statement.close();
		}
	}
}