package ar.edu.unrn.seminario.accesos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import ar.edu.unrn.seminario.exception.ObjetoNuloException;
import ar.edu.unrn.seminario.modelo.EstadoPedido;
import ar.edu.unrn.seminario.modelo.PedidosDonacion;
import ar.edu.unrn.seminario.modelo.Usuario;

public class PedidosDonacionDAOJDBC implements PedidosDonacionDao {
	
	private UsuarioDao usuarioDao = new UsuarioDAOJDBC();

	@Override
	public int create(PedidosDonacion pedido, Connection conn) throws SQLException {
		PreparedStatement statement = null;
		ResultSet generatedKeys = null;
		try {
			statement = conn.prepareStatement(
					"INSERT INTO pedidos_donacion(fecha, tipo_vehiculo, observaciones, usuario_donante, estado, id_orden_retiro) "
					+ "VALUES (?, ?, ?, ?, ?, ?)",
					Statement.RETURN_GENERATED_KEYS); // el generated keys para obtener el id autogenerado, si no fallaba
			
			statement.setTimestamp(1, Timestamp.valueOf(pedido.obtenerFecha()));
			statement.setString(2, pedido.describirTipoVehiculo());
			statement.setString(3, pedido.obtenerObservaciones());
			statement.setString(4, pedido.getDonante().getUsuario());
			statement.setString(5, pedido.obtenerEstado());
			
			// id_orden_retiro puede ser null
			if (pedido.obtenerOrden() != null) {
				statement.setInt(6, pedido.obtenerOrden().getId());
			} else {
				statement.setNull(6, java.sql.Types.INTEGER);// sin esto tira error
			}
			
			int affectedRows = statement.executeUpdate();
			if (affectedRows == 0) {
				throw new SQLException("No se pudo crear. Error.");
			}

			generatedKeys = statement.getGeneratedKeys(); // sirve para obtener las claves generadas automaticamente por la base de datos
			if (generatedKeys.next()) {
				int generatedId = generatedKeys.getInt(1);
				pedido.setId(generatedId); // *** CORRECCIÓN: Actualiza el ID del objeto en memoria ***
				return generatedId;
			} else {
				throw new SQLException("No se pudo crear. Error.");
			}
		} finally {
			if (generatedKeys != null) generatedKeys.close();
			if (statement != null) statement.close();
		}
	}

	@Override
	public void update(PedidosDonacion pedido, Connection conn) throws SQLException {
		PreparedStatement statement = null;
		try {
			statement = conn.prepareStatement(
					"UPDATE pedidos_donacion SET estado = ?, id_orden_retiro = ? WHERE id = ?");
			
			statement.setString(1, pedido.obtenerEstado());
			
			if (pedido.obtenerOrden() != null) {
				statement.setInt(2, pedido.obtenerOrden().getId());
			} else {
				statement.setNull(2, java.sql.Types.INTEGER);
			}
			
			statement.setInt(3, pedido.getId());
			statement.executeUpdate();
		} finally {
			if (statement != null) statement.close();
		}
	}

	@Override
	public PedidosDonacion findById(int idPedido, Connection conn) throws SQLException {
		PedidosDonacion pedido = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			statement = conn.prepareStatement(
					"SELECT id, fecha, tipo_vehiculo, observaciones, usuario_donante, estado, id_orden_retiro "
					+ "FROM pedidos_donacion WHERE id = ?");
			statement.setInt(1, idPedido);
			rs = statement.executeQuery();
			
			if (rs.next()) {
				pedido = mapearResultadoPedido(rs, conn);
			}
		} finally {
			if (rs != null) rs.close();
			if (statement != null) statement.close();
		}
		return pedido;
	}

	@Override
	public List<PedidosDonacion> findAllPendientes(Connection conn) throws SQLException {
		List<PedidosDonacion> pedidos = new ArrayList<PedidosDonacion>();
		Statement statement = null;
		ResultSet rs = null;
		try {
			statement = conn.createStatement();
			// *** CORRECCIÓN: Busca por el String "Pendiente" del Enum ***
			rs = statement.executeQuery(
					"SELECT id, fecha, tipo_vehiculo, observaciones, usuario_donante, estado, id_orden_retiro "
					+ "FROM pedidos_donacion WHERE estado = 'Pendiente'"); 
			
			while (rs.next()) {
				try {
					PedidosDonacion pedido = mapearResultadoPedido(rs, conn);
					pedidos.add(pedido);
				} catch (Exception e) {
					System.err.println("Error al crear registro: " + e.getMessage());
				}
			}
		} finally {
			if (rs != null) rs.close();
			if (statement != null) statement.close();
		}
		return pedidos;
	}

	@Override
	public List<PedidosDonacion> findByOrden(int idOrden, Connection conn) throws SQLException {
		List<PedidosDonacion> pedidos = new ArrayList<PedidosDonacion>();
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			statement = conn.prepareStatement(
					"SELECT id, fecha, tipo_vehiculo, observaciones, usuario_donante, estado, id_orden_retiro "
					+ "FROM pedidos_donacion WHERE id_orden_retiro = ?");
			statement.setInt(1, idOrden);
			rs = statement.executeQuery();
			
			while (rs.next()) {
				try {
					PedidosDonacion pedido = mapearResultadoPedido(rs, conn);
					pedidos.add(pedido);
				} catch (Exception e) {
					System.err.println("Error al crear PedidosDonacion: " + e.getMessage());
				}
			}
		} finally {
			if (rs != null) rs.close();
			if (statement != null) statement.close();
		}
		return pedidos;
	}
	
	private PedidosDonacion mapearResultadoPedido(ResultSet rs, Connection conn) throws SQLException {
		try {
			String usuarioDonante = rs.getString("usuario_donante");
			Usuario donante = usuarioDao.find(usuarioDonante, conn);
			
			if (donante == null) {
				throw new SQLException("Donante no encontrado: " + usuarioDonante);
			}

			int id = rs.getInt("id"); // LEER EL ID
			LocalDateTime fecha = rs.getTimestamp("fecha").toLocalDateTime();
			String tipoVehiculo = rs.getString("tipo_vehiculo");
			String observaciones = rs.getString("observaciones");
			String estadoStr = rs.getString("estado");
			
			// Convertir String del estado a Enum
			EstadoPedido estado;
			try {
				// Busca el Enum por su descripción (toString)
				if (estadoStr.equals(EstadoPedido.EN_EJECUCION.toString())) {
					estado = EstadoPedido.EN_EJECUCION;
				} else if (estadoStr.equals(EstadoPedido.COMPLETADO.toString())) {
					estado = EstadoPedido.COMPLETADO;
				} else {
					estado = EstadoPedido.PENDIENTE;
				}
			} catch (Exception e) {
				estado = EstadoPedido.PENDIENTE; // Default
			}

			// Usar el nuevo constructor que incluye el ID
			PedidosDonacion pedido = new PedidosDonacion(id, fecha, tipoVehiculo, observaciones, donante, estado);
			
			return pedido;
			
		} catch (ObjetoNuloException e) {
			throw new SQLException("Error al mapear PedidosDonacion (ObjetoNulo): " + e.getMessage(), e);
		}
	}

}