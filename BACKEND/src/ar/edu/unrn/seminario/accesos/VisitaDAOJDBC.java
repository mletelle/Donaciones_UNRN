package ar.edu.unrn.seminario.accesos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import ar.edu.unrn.seminario.modelo.ResultadoVisita;
import ar.edu.unrn.seminario.modelo.Usuario;
import ar.edu.unrn.seminario.modelo.Visita;

public class VisitaDAOJDBC implements VisitaDao {

	@Override
	public void create(Visita visita, int idOrden, int idPedido, Connection conn) throws SQLException {
		PreparedStatement statement = null;
		try {
			statement = conn.prepareStatement(
					"INSERT INTO visitas(id_orden_retiro, id_pedido_donacion, fecha_visita, resultado, observacion) "
					+ "VALUES (?, ?, ?, ?, ?)");
			
			statement.setInt(1, idOrden);
			statement.setInt(2, idPedido);
			statement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
			statement.setString(4, visita.obtenerResultado().toString());
			statement.setString(5, visita.obtenerObservacion());
			
			statement.executeUpdate();
		} finally {
			if (statement != null) statement.close();
		}
	}

	@Override
	public List<Visita> findByVoluntario(Usuario voluntario, Connection conn) throws SQLException {
		List<Visita> visitas = new ArrayList<Visita>();
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			// join para obtener las visitas asociadas a las ordenes de retiro del voluntario
			statement = conn.prepareStatement(
					"SELECT v.id, v.fecha_visita, v.resultado, v.observacion, v.id_pedido_donacion "
					+ "FROM visitas v "
					+ "JOIN ordenes_retiro o ON v.id_orden_retiro = o.id "
					+ "WHERE o.usuario_voluntario = ?");
			
			statement.setString(1, voluntario.getUsuario());
			rs = statement.executeQuery();
			
			while (rs.next()) {
				try {
					Timestamp timestamp = rs.getTimestamp("fecha_visita");
					LocalDateTime fechaHora = timestamp.toLocalDateTime();
					String observacion = rs.getString("observacion");
					String resultadoStr = rs.getString("resultado");
					
					// convertir el string a enum
					ResultadoVisita resultado = ResultadoVisita.fromString(resultadoStr);
					// usar el metodo fromString que coincide con los valores de la BD
					Visita visita = new Visita(fechaHora, resultado, observacion);
					visitas.add(visita);
				} catch (Exception e) {
					System.err.println("Error creando Visita: " + e.getMessage());
					e.printStackTrace();
				}
			}
		} finally {
			if (rs != null) rs.close();
			if (statement != null) statement.close();
		}
		return visitas;
	}

}
