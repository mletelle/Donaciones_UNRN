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
		
		// 1. Sentencia SQL. Se utiliza java.sql.Types.VARCHAR para la descripción.
		String sql = "INSERT INTO bienes(id_pedido_donacion, categoria, cantidad, tipo, descripcion, fecha_vencimiento) "
				   + "VALUES (?, ?, ?, ?, ?, ?)";
		
		try {
			statement = conn.prepareStatement(sql);
			
			for (Bien bien : bienes) {
				// Columna 1: Clave Foránea
				statement.setInt(1, idPedido);
				
				// Columna 2: Categoria (INT)
				statement.setInt(2, bien.obtenerCategoria());
				
				// Columna 3: Cantidad (INT)
				statement.setInt(3, bien.obtenerCantidad());
				
				// ----------------------------------------------------------------------
				// Columna 4: TIPO (Punto de Falla Común)
				// Asumo que 'tipo' en la DB es VARCHAR y Bien.obtenerTipo() devuelve un INT
				// Si en la DB 'tipo' es VARCHAR, DEBES convertirlo a String.
				// Si en la DB 'tipo' es INT, usa setInt.
				
				// **OPCIÓN MÁS SEGURA (Convierte a String)**
				statement.setString(4, String.valueOf(bien.obtenerTipo())); 
				
				// Si sabes que en la DB es un INT:
				// statement.setInt(4, bien.obtenerTipo()); 
				// ----------------------------------------------------------------------

				// Columna 5: DESCRIPCION (Punto de Falla Común: NOT NULL)
				// Si la columna 'descripcion' es NOT NULL en la DB, no puedes pasarle null.
				// Lo robustecemos para usar un valor del modelo si existe, sino un String vacío.
				String descripcion = bien.obtenerDescripcion(); // <-- Asumo que tienes un getter
				if (descripcion == null) {
					descripcion = "";
				}
				statement.setString(5, descripcion);

				// Columna 6: Fecha de Vencimiento (DATE)
				if (bien.getFecVec() != null) {
					statement.setDate(6, new java.sql.Date(bien.getFecVec().getTime()));
				} else {
					// Uso Types.DATE que es el tipo correcto para la columna DATE/DATETIME
					statement.setNull(6, java.sql.Types.DATE); 
				}
				
				// Acumular la sentencia
				statement.addBatch();
			}
			
			// Ejecutar todas las sentencias de la cola
			int[] results = statement.executeBatch();
			
			// Opcional: Verificar si alguna sentencia en el lote falló
			for (int result : results) {
				if (result == PreparedStatement.EXECUTE_FAILED) {
					// Esto debería lanzar una BatchUpdateException, pero verificamos por si acaso
					throw new SQLException("Una o más inserciones en el lote fallaron.");
				}
			}

		} catch (Exception e) {
            // Re-lanzar o envolver la excepción para que el PersistenceApi haga Rollback
			throw new SQLException("Error al ejecutar el lote de inserción de bienes. Causa: " + e.getMessage(), e);
		} finally {
			if (statement != null) statement.close();
		}
	}
}