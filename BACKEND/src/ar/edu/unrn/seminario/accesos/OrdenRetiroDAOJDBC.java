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
	public int create(OrdenRetiro orden, Connection conn) throws SQLException {
		PreparedStatement statement = null;
		ResultSet generatedKeys = null;
		try {
			statement = conn.prepareStatement(
					"INSERT INTO ordenes_retiro(fecha_generacion, estado, usuario_voluntario, patente_vehiculo) "
					+ "VALUES (?, ?, ?, ?)",
					Statement.RETURN_GENERATED_KEYS);
			
			statement.setTimestamp(1, Timestamp.valueOf(orden.obtenerFechaCreacion()));
			statement.setString(2, orden.obtenerNombreEstado()); // Usa el toString() del Enum ("Pendiente")
			
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
				throw new SQLException("No se pudo crear la orden");
			}
			
			generatedKeys = statement.getGeneratedKeys();
			if (generatedKeys.next()) {
				int generatedId = generatedKeys.getInt(1);
				orden.setId(generatedId); // Actualiza el ID del objeto en memoria
				return generatedId;
			} else {
				throw new SQLException("No se pudo obtener el ID de la orden");
			}
		} finally {
			if (generatedKeys != null) generatedKeys.close();
			if (statement != null) statement.close();
		}
	}

	@Override
	public void update(OrdenRetiro orden, Connection conn) throws SQLException {
		PreparedStatement statement = null;
		try {
			statement = conn.prepareStatement(
					"UPDATE ordenes_retiro SET estado = ? WHERE id = ?");
			
			statement.setString(1, orden.obtenerNombreEstado()); // Usa el toString() del Enum
			statement.setInt(2, orden.getId());
			
			statement.executeUpdate();
		} finally {
			if (statement != null) statement.close();
		}
	}

	@Override
	public OrdenRetiro findById(int idOrden, Connection conn) throws SQLException {
		OrdenRetiro orden = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			statement = conn.prepareStatement(
					"SELECT id, fecha_generacion, estado, usuario_voluntario, patente_vehiculo "
					+ "FROM ordenes_retiro WHERE id = ?");
			statement.setInt(1, idOrden);
			rs = statement.executeQuery();
			
			if (rs.next()) {
				orden = mapearResultadoOrden(rs, conn); 
			}
		} finally {
			if (rs != null) rs.close();
			if (statement != null) statement.close();
		}
		return orden;
	}

	@Override
	public List<OrdenRetiro> findByEstado(String estado, Connection conn) throws SQLException {
		List<OrdenRetiro> ordenes = new ArrayList<OrdenRetiro>();
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
            // Se utiliza la consulta definida al principio junto con un WHERE para validar incluso con mayusculas.
			// antes creaba una nueva consulta
			statement = conn.prepareStatement(SELECT_ORDEN_FULL + " WHERE UPPER(o.estado) = UPPER(?)");
			statement.setString(1, estado);
			rs = statement.executeQuery();
			
			while (rs.next()) {
				try {
					OrdenRetiro orden = mapearResultadoOrden(rs, conn);
					if (orden != null) {
						ordenes.add(orden);
					}
				} catch (Exception e) {
					System.err.println("Error al crear OrdenRetiro (ID=" + rs.getInt("o_id") + "): " + e.getMessage());
					e.printStackTrace();
				}
			}
		} finally {
			if (rs != null) rs.close();
			if (statement != null) statement.close();
		}
		return ordenes;
	}

	@Override
	public List<OrdenRetiro> findAll(Connection conn) throws SQLException {
		List<OrdenRetiro> ordenes = new ArrayList<OrdenRetiro>();
		Statement statement = null;
		ResultSet rs = null;
		try {
			statement = conn.createStatement();
            // Ahora se usa la consulta nueva
			rs = statement.executeQuery(SELECT_ORDEN_FULL + " ORDER BY o.fecha_generacion DESC"); 
			
			while (rs.next()) {
				try {
                    // El mapeador ahora recibe los datos del JOIN
					OrdenRetiro orden = mapearResultadoOrden(rs, conn); 
					if (orden != null) {
						ordenes.add(orden);
					}
				} catch (Exception e) {
					System.err.println("Error al crear OrdenRetiro (ID=" + rs.getInt("o_id") + "): " + e.getMessage());
					e.printStackTrace();
				}
			}
		} finally {
			if (rs != null) rs.close();
			if (statement != null) statement.close();
		}
		return ordenes;
	}

	public List<OrdenRetiro> findByVoluntario(String username, Connection conn) throws SQLException {
		List<OrdenRetiro> ordenes = new ArrayList<OrdenRetiro>();
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
            // Usa la consulta JOIN y filtrar por el alias 'o.'
			statement = conn.prepareStatement(
                    SELECT_ORDEN_FULL + " WHERE o.usuario_voluntario = ?");
			
            statement.setString(1, username);
			rs = statement.executeQuery();
			
			while (rs.next()) {
				try {
                    // Ahora el mapeador recibirá las columnas que espera
					OrdenRetiro orden = mapearResultadoOrden(rs, conn); 
					if (orden != null) {
						ordenes.add(orden);
					}
				} catch (Exception e) {
					System.err.println("Error al crear OrdenRetiro (ID=" + rs.getInt("o_id") + "): " + e.getMessage());
					e.printStackTrace();
				}
			}
		} finally {
			if (rs != null) rs.close();
			if (statement != null) statement.close();
		}
		return ordenes;
	}
	
	private OrdenRetiro mapearResultadoOrden(ResultSet rs, Connection conn) throws SQLException {
		try {
			
			int idOrden = rs.getInt("o_id");
			List<PedidosDonacion> pedidos = pedidoDao.findByOrden(idOrden, conn);
			
			if (pedidos == null || pedidos.isEmpty()) {
				System.err.println("Orden ID=" + idOrden + " no tiene pedidos asociados, se omite");
				return null;
			}
			
			OrdenRetiro orden = new OrdenRetiro(pedidos, null);
			orden.setId(idOrden);
			
			String estadoStr = rs.getString("o_estado");
			EstadoOrden estado = EstadoOrden.fromString(estadoStr.toUpperCase());
			orden.setEstado(estado);
			
            // Ya no se llama a usuarioDao.find() ni a vehiculoDao.findByPatente()

            // Construir Voluntario (Usuario) desde el JOIN
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

			orden.actualizarEstadoAutomatico();
			
			return orden;
		} catch (Exception e) {
			throw new SQLException("Error al mapear OrdenRetiro: " + e.getMessage(), e);
		}
	}
}