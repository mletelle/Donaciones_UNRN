package ar.edu.unrn.seminario.accesos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
// import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import ar.edu.unrn.seminario.exception.PersistenceException;
import ar.edu.unrn.seminario.modelo.EstadoOrden;
import ar.edu.unrn.seminario.modelo.OrdenRetiro;
import ar.edu.unrn.seminario.modelo.PedidosDonacion;
import ar.edu.unrn.seminario.modelo.Rol;
// import ar.edu.unrn.seminario.modelo.Ubicacion;
import ar.edu.unrn.seminario.modelo.Usuario;
import ar.edu.unrn.seminario.modelo.Vehiculo;

public class OrdenRetiroDAOJDBC implements OrdenRetiroDao {
	//private UsuarioDao usuarioDao = new UsuarioDAOJDBC();
	//private VehiculoDao vehiculoDao = new VehiculoDAOJDBC();
	private PedidosDonacionDao pedidoDao = new PedidosDonacionDAOJDBC();

    // JOIN en la consulta principal para traer todos los datos de Usuario y Vehiculo de una sola vez.
    private static final String SELECT_ORDEN_FULL = 
        "SELECT " +
        " o.id AS o_id, o.fecha_generacion AS o_fecha, o.estado AS o_estado," +
        " u.usuario AS u_usuario, u.contrasena AS u_contrasena, u.nombre AS u_nombre, " +
        " u.correo AS u_correo, u.activo AS u_activo, u.apellido AS u_apellido, " +
        " u.dni AS u_dni, u.direccion AS u_direccion," +
        " r.codigo AS r_codigo, r.nombre AS r_nombre, r.activo AS r_activo," +
        " v.patente AS v_patente, v.tipoVeh AS v_tipoVeh, v.capacidad AS v_capacidad, " +
        " v.estado AS v_estado_vehiculo " +
        "FROM " +
        " ordenes_retiro o " +
        "LEFT JOIN usuarios u ON o.usuario_voluntario = u.usuario " +
        "LEFT JOIN roles r ON u.rol = r.codigo " +
        "LEFT JOIN vehiculos v ON o.patente_vehiculo = v.patente ";
	
	@Override
	public int create(OrdenRetiro orden) throws PersistenceException {
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet generatedKeys = null;
		try {
			conn = ConnectionManager.getConnection();
			conn.setAutoCommit(false);
			
			statement = conn.prepareStatement(
					"INSERT INTO ordenes_retiro(fecha_generacion, estado, usuario_voluntario, patente_vehiculo) "
					+ "VALUES (?, ?, ?, ?)",
					Statement.RETURN_GENERATED_KEYS);
			
			statement.setTimestamp(1, Timestamp.valueOf(orden.obtenerFechaCreacion()));
			statement.setString(2, orden.obtenerNombreEstado());
			
			Usuario voluntario = orden.obtenerVoluntarioPrincipal();
			if (voluntario != null) {
				statement.setString(3, voluntario.getUsuario());
			} else {
				statement.setNull(3, java.sql.Types.VARCHAR);
			}
			
			Vehiculo vehiculo = orden.obtenerVehiculo();
			if (vehiculo != null) {
				statement.setString(4, vehiculo.getPatente());
			} else {
				statement.setNull(4, java.sql.Types.VARCHAR);
			}
			
			int affectedRows = statement.executeUpdate();
			if (affectedRows == 0) {
				throw new SQLException("no se pudo crear la orden");
			}
			
			generatedKeys = statement.getGeneratedKeys();
			int generatedId;
			if (generatedKeys.next()) {
				generatedId = generatedKeys.getInt(1);
				orden.setId(generatedId);
			} else {
				throw new SQLException("no se pudo obtener el id de la orden");
			}
			
			conn.commit();
			return generatedId;
		} catch (SQLException e) {
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
			throw new PersistenceException("Error al crear orden de retiro: " + e.getMessage(), e);
		} finally {
			if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) {}
			if (statement != null) try { statement.close(); } catch (SQLException e) {}
			if (conn != null) try { conn.close(); } catch (SQLException e) {}
		}
	}

	@Override
	public int crearOrdenConPedidos(OrdenRetiro orden, List<Integer> idsPedidos) throws PersistenceException {
		Connection conn = null;
		PreparedStatement stmtOrden = null;
		PreparedStatement stmtPedidos = null;
		ResultSet generatedKeys = null;
		try {
			conn = ConnectionManager.getConnection();
			conn.setAutoCommit(false);
			
			stmtOrden = conn.prepareStatement(
					"INSERT INTO ordenes_retiro(fecha_generacion, estado, usuario_voluntario, patente_vehiculo) "
					+ "VALUES (?, ?, ?, ?)",
					Statement.RETURN_GENERATED_KEYS);
			
			stmtOrden.setTimestamp(1, Timestamp.valueOf(orden.obtenerFechaCreacion()));
			stmtOrden.setString(2, orden.obtenerNombreEstado());
			
			Usuario voluntario = orden.obtenerVoluntarioPrincipal();
			if (voluntario != null) {
				stmtOrden.setString(3, voluntario.getUsuario());
			} else {
				stmtOrden.setNull(3, java.sql.Types.VARCHAR);
			}
			
			Vehiculo vehiculo = orden.obtenerVehiculo();
			if (vehiculo != null) {
				stmtOrden.setString(4, vehiculo.getPatente());
			} else {
				stmtOrden.setNull(4, java.sql.Types.VARCHAR);
			}
			
			int affectedRows = stmtOrden.executeUpdate();
			if (affectedRows == 0) {
				throw new SQLException("no se pudo crear la orden");
			}
			
			generatedKeys = stmtOrden.getGeneratedKeys();
			int generatedId;
			if (generatedKeys.next()) {
				generatedId = generatedKeys.getInt(1);
				orden.setId(generatedId);
			} else {
				throw new SQLException("no se pudo obtener el id de la orden");
			}
			
			stmtPedidos = conn.prepareStatement("UPDATE pedidos_donacion SET estado = 'EN_EJECUCION', id_orden_retiro = ? WHERE id = ?");
			for (Integer idPedido : idsPedidos) {
				stmtPedidos.setInt(1, generatedId);
				stmtPedidos.setInt(2, idPedido);
				stmtPedidos.addBatch();
			}
			stmtPedidos.executeBatch();
			
			conn.commit();
			return generatedId;
		} catch (SQLException e) {
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
			throw new PersistenceException("Error al crear orden con pedidos: " + e.getMessage(), e);
		} finally {
			if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) {}
			if (stmtOrden != null) try { stmtOrden.close(); } catch (SQLException e) {}
			if (stmtPedidos != null) try { stmtPedidos.close(); } catch (SQLException e) {}
			if (conn != null) try { conn.close(); } catch (SQLException e) {}
		}
	}

	@Override
	public void update(OrdenRetiro orden) throws PersistenceException {
		Connection conn = null;
		PreparedStatement statement = null;
		try {
			conn = ConnectionManager.getConnection();
			conn.setAutoCommit(false);
			
			statement = conn.prepareStatement(
					"UPDATE ordenes_retiro SET estado = ? WHERE id = ?");
			
			statement.setString(1, orden.obtenerNombreEstado());
			statement.setInt(2, orden.getId());
			
			statement.executeUpdate();
			
			conn.commit();
		} catch (SQLException e) {
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
			throw new PersistenceException("Error al actualizar orden de retiro: " + e.getMessage(), e);
		} finally {
			if (statement != null) try { statement.close(); } catch (SQLException e) {}
			if (conn != null) try { conn.close(); } catch (SQLException e) {}
		}
	}

	@Override
	public OrdenRetiro findById(int idOrden) throws PersistenceException {
		Connection conn = null;
		OrdenRetiro orden = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			conn = ConnectionManager.getConnection();
			statement = conn.prepareStatement(
					"SELECT id, fecha_generacion, estado, usuario_voluntario, patente_vehiculo "
					+ "FROM ordenes_retiro WHERE id = ?");
			statement.setInt(1, idOrden);
			rs = statement.executeQuery();
			
			if (rs.next()) {
				orden = mapearResultadoOrden(rs); 
			}
		} catch (SQLException e) {
			throw new PersistenceException("Error al buscar orden de retiro por ID: " + e.getMessage(), e);
		} finally {
			if (rs != null) try { rs.close(); } catch (SQLException e) {}
			if (statement != null) try { statement.close(); } catch (SQLException e) {}
			if (conn != null) try { conn.close(); } catch (SQLException e) {}
		}
		return orden;
	}

	@Override
	public List<OrdenRetiro> findByEstado(String estado) throws PersistenceException {
		Connection conn = null;
		List<OrdenRetiro> ordenes = new ArrayList<OrdenRetiro>();
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			conn = ConnectionManager.getConnection();
			statement = conn.prepareStatement(SELECT_ORDEN_FULL + " WHERE UPPER(o.estado) = UPPER(?)");
			statement.setString(1, estado);
			rs = statement.executeQuery();
			
			while (rs.next()) {
				try {
					OrdenRetiro orden = mapearResultadoOrden(rs);
					if (orden != null) {
						ordenes.add(orden);
					}
				} catch (Exception e) {
					System.err.println("error al crear ordenretiro (id=" + rs.getInt("o_id") + "): " + e.getMessage());
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
			throw new PersistenceException("Error al buscar órdenes de retiro por estado: " + e.getMessage(), e);
		} finally {
			if (rs != null) try { rs.close(); } catch (SQLException e) {}
			if (statement != null) try { statement.close(); } catch (SQLException e) {}
			if (conn != null) try { conn.close(); } catch (SQLException e) {}
		}
		return ordenes;
	}

	@Override
	public List<OrdenRetiro> findAll() throws PersistenceException {
		Connection conn = null;
		List<OrdenRetiro> ordenes = new ArrayList<OrdenRetiro>();
		Statement statement = null;
		ResultSet rs = null;
		try {
			conn = ConnectionManager.getConnection();
			statement = conn.createStatement();
			rs = statement.executeQuery(SELECT_ORDEN_FULL + " ORDER BY o.fecha_generacion DESC"); 
			
			while (rs.next()) {
				try {
					OrdenRetiro orden = mapearResultadoOrden(rs); 
					if (orden != null) {
						ordenes.add(orden);
					}
				} catch (Exception e) {
					System.err.println("error al crear ordenretiro (id=" + rs.getInt("o_id") + "): " + e.getMessage());
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
			throw new PersistenceException("Error al buscar todas las órdenes de retiro: " + e.getMessage(), e);
		} finally {
			if (rs != null) try { rs.close(); } catch (SQLException e) {}
			if (statement != null) try { statement.close(); } catch (SQLException e) {}
			if (conn != null) try { conn.close(); } catch (SQLException e) {}
		}
		return ordenes;
	}

	public List<OrdenRetiro> findByVoluntario(String username) throws PersistenceException {
		Connection conn = null;
		List<OrdenRetiro> ordenes = new ArrayList<OrdenRetiro>();
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			conn = ConnectionManager.getConnection();
			statement = conn.prepareStatement(
                    SELECT_ORDEN_FULL + " WHERE o.usuario_voluntario = ?");
			
            statement.setString(1, username);
			rs = statement.executeQuery();
			
			while (rs.next()) {
				try {
					OrdenRetiro orden = mapearResultadoOrden(rs); 
					if (orden != null) {
						ordenes.add(orden);
					}
				} catch (Exception e) {
					System.err.println("error al crear ordenretiro (id=" + rs.getInt("o_id") + "): " + e.getMessage());
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
			throw new PersistenceException("Error al buscar órdenes de retiro por voluntario: " + e.getMessage(), e);
		} finally {
			if (rs != null) try { rs.close(); } catch (SQLException e) {}
			if (statement != null) try { statement.close(); } catch (SQLException e) {}
			if (conn != null) try { conn.close(); } catch (SQLException e) {}
		}
		return ordenes;
	}
	
	private OrdenRetiro mapearResultadoOrden(ResultSet rs) throws SQLException {
		try {
			
			int idOrden = rs.getInt("o_id");
			List<PedidosDonacion> pedidos = pedidoDao.findByOrden(idOrden);
			
			if (pedidos == null || pedidos.isEmpty()) {
				System.err.println("Orden ID=" + idOrden + " no tiene pedidos asociados, se omite");
				return null;
			}
			
			OrdenRetiro orden = new OrdenRetiro(pedidos, null);
			orden.setId(idOrden);
			
			String estadoStr = rs.getString("o_estado");
			try {
				EstadoOrden estado = EstadoOrden.valueOf(estadoStr.trim().toUpperCase());
				orden.forzarEstadoDesdeBD(estado);
			} catch (IllegalArgumentException e) {
				throw new PersistenceException("estado inconsistente en bd: " + estadoStr, e);
			}
		
			String usuarioVoluntario = rs.getString("u_usuario");
			if (usuarioVoluntario != null) {
                // Crear Rol desde el JOIN
                Rol rol = new Rol(rs.getInt("r_codigo"), rs.getString("r_nombre"));
                rol.setActivo(rs.getBoolean("r_activo"));
                
                // Crear Usuario desde el JOIN
				Usuario voluntario = new Usuario(
                    usuarioVoluntario,
                    rs.getString("u_contrasena"),
                    rs.getString("u_nombre"),
                    rs.getString("u_correo"),
                    rol,
                    rs.getString("u_apellido"),
                    rs.getInt("u_dni"),
                    rs.getString("u_direccion")
                );
                if (!rs.getBoolean("u_activo")) {
                    voluntario.desactivar();
                }
				orden.asignarVoluntario(voluntario);
			}
			
            // Construir Vehiculo desde el JOIN
			String patenteVehiculo = rs.getString("v_patente");
			if (patenteVehiculo != null) {
				Vehiculo vehiculo = new Vehiculo(
                    patenteVehiculo,
                    rs.getString("v_estado_vehiculo"), // Usamos el alias 'v_estado_vehiculo'
                    rs.getString("v_tipoVeh"),
                    rs.getInt("v_capacidad")
                );
				orden.asignarVehiculo(vehiculo);
			}
			
			return orden;
		} catch (Exception e) {
			throw new SQLException("Error al mapear OrdenRetiro: " + e.getMessage(), e);
		}
	}
}