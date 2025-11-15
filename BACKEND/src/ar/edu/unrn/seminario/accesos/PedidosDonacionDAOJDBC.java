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

import ar.edu.unrn.seminario.modelo.EstadoPedido;
import ar.edu.unrn.seminario.modelo.OrdenRetiro;
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
					"INSERT INTO pedidos_donacion(fecha, tipo_vehiculo, usuario_donante, estado, id_orden_retiro) "
					+ "VALUES (?, ?, ?, ?, ?)",
					Statement.RETURN_GENERATED_KEYS); // el generated keys para obtener el id autogenerado, si no fallaba
			
			statement.setTimestamp(1, Timestamp.valueOf(pedido.obtenerFecha()));
			statement.setString(2, pedido.describirTipoVehiculo());
			statement.setString(3, pedido.getDonante().getUsuario());
			statement.setString(4, pedido.obtenerEstado());
			
			// id_orden_retiro puede ser null
			if (pedido.obtenerOrden() != null) {
				statement.setInt(5, pedido.obtenerOrden().getId());
			} else {
				statement.setNull(5, java.sql.Types.INTEGER);// sin esto tira error
			}
			
			int affectedRows = statement.executeUpdate();
			if (affectedRows == 0) {
				throw new SQLException("No se pudo crear. Error.");
			}

			generatedKeys = statement.getGeneratedKeys(); // sirve para obtener las claves generadas automaticamente por la base de datos
			if (generatedKeys.next()) {
				return generatedKeys.getInt(1);
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
					"SELECT id, fecha, tipo_vehiculo, usuario_donante, estado, id_orden_retiro "
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
			rs = statement.executeQuery(
					"SELECT id, fecha, tipo_vehiculo, usuario_donante, estado, id_orden_retiro "
					+ "FROM pedidos_donacion WHERE estado = 'PENDIENTE'");
			
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
	public List<PedidosDonacion> findAll(Connection conn) throws SQLException {
		List<PedidosDonacion> pedidos = new ArrayList<PedidosDonacion>();
		Statement statement = null;
		ResultSet rs = null;
		try {
			statement = conn.createStatement();
			rs = statement.executeQuery(
					"SELECT id, fecha, tipo_vehiculo, usuario_donante, estado, id_orden_retiro "
					+ "FROM pedidos_donacion ORDER BY fecha DESC");
			
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
					"SELECT id, fecha, tipo_vehiculo, usuario_donante, estado, id_orden_retiro "
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
			int id = rs.getInt("id");
			String usuarioDonante = rs.getString("usuario_donante");
			Usuario donante = usuarioDao.find(usuarioDonante, conn);
			
			if (donante == null) {
				throw new SQLException("Donante no encontrado: " + usuarioDonante);
			}
			
			LocalDateTime fecha = rs.getTimestamp("fecha").toLocalDateTime();
			String tipoVehiculo = rs.getString("tipo_vehiculo");
			
			PedidosDonacion pedido = new PedidosDonacion(fecha, tipoVehiculo, donante);
			pedido.setId(id);
			
			String estadoStr = rs.getString("estado");
			EstadoPedido estado = EstadoPedido.fromString(estadoStr.toUpperCase());
			pedido.setEstado(estado);
			
			// cargar id_orden_retiro si existe
			Integer idOrdenRetiro = rs.getInt("id_orden_retiro");
			if (!rs.wasNull() && idOrdenRetiro != null) {
				// crear una orden temporal solo con el id para mantener la referencia
				OrdenRetiro ordenTemp = new OrdenRetiro(idOrdenRetiro);
				pedido.asignarOrden(ordenTemp);
			}
			
			return pedido;
		} catch (Exception e) {
			throw new SQLException("Error al mapear PedidosDonacion: " + e.getMessage(), e);
		}
	}
	

	@Override
	public List<PedidosDonacion> findByIds(List<Integer> ids, Connection conn) throws SQLException {
	    List<PedidosDonacion> pedidos = new ArrayList<>();
	    if (ids == null || ids.isEmpty()) {
	        return pedidos; // Devolver lista vacía si no hay IDs
	    }

	    PreparedStatement statement = null;
	    ResultSet rs = null;
	    
	    // construccion del string (?, ?, ?)
	    StringBuilder placeholders = new StringBuilder();
	    for (int i = 0; i < ids.size(); i++) {
	        placeholders.append("?");
	        if (i < ids.size() - 1) {
	            placeholders.append(", ");
	        }
	    }

	    // creacion de consulta
	    String sql = "SELECT id, fecha, tipo_vehiculo, usuario_donante, estado, id_orden_retiro "
	               + "FROM pedidos_donacion WHERE id IN (" + placeholders.toString() + ")";

	    try {
	        statement = conn.prepareStatement(sql);
	        // setteo de parametros
	        for (int i = 0; i < ids.size(); i++) {
	            statement.setInt(i + 1, ids.get(i)); // Los parámetros JDBC son 1-indexados
	        }

	        // Ejecutar y mapear
	        rs = statement.executeQuery();
	        while (rs.next()) {
	            try {
	                PedidosDonacion pedido = mapearResultadoPedido(rs, conn);
	                pedidos.add(pedido);
	            } catch (Exception e) {
	                System.err.println("Error al mapear PedidosDonacion: " + e.getMessage());
	            }
	        }
	    } finally {
	        if (rs != null) rs.close();
	        if (statement != null) statement.close();
	    }
	    
	    return pedidos;
	}

	}
	

