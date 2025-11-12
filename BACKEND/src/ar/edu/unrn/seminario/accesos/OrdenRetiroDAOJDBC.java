package ar.edu.unrn.seminario.accesos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import ar.edu.unrn.seminario.modelo.OrdenRetiro;
import ar.edu.unrn.seminario.modelo.PedidosDonacion;
import ar.edu.unrn.seminario.modelo.Usuario;
import ar.edu.unrn.seminario.modelo.Vehiculo;

public class OrdenRetiroDAOJDBC implements OrdenRetiroDao {
	
	private UsuarioDao usuarioDao = new UsuarioDAOJDBC();
	private VehiculoDao vehiculoDao = new VehiculoDAOJDBC();
	private PedidosDonacionDao pedidoDao = new PedidosDonacionDAOJDBC();

	@Override
	public int create(OrdenRetiro orden, Connection conn) throws SQLException {
		PreparedStatement statement = null;
		ResultSet generatedKeys = null;
		try {
			statement = conn.prepareStatement(
					"INSERT INTO ordenes_retiro(fecha_generacion, estado, usuario_voluntario, patente_vehiculo) "
					+ "VALUES (?, ?, ?, ?)",
					Statement.RETURN_GENERATED_KEYS);
			// mismo que el contructor de memory pero cambian a prepared statement
			statement.setTimestamp(1, Timestamp.valueOf(orden.obtenerFechaCreacion()));
			statement.setString(2, orden.obtenerNombreEstado());
			
			// usuario_voluntario
			Usuario voluntario = orden.obtenerVoluntarioPrincipal();
			if (voluntario != null) {
				statement.setString(3, voluntario.getUsuario());
			} else {
				statement.setNull(3, java.sql.Types.VARCHAR);
			}
			
			// patente_vehiculo
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
				return generatedKeys.getInt(1);
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
			// solo se puede actualizar el estado por ahora, no editables otros campos
			statement.setString(1, orden.obtenerNombreEstado());
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
			statement = conn.prepareStatement(
					"SELECT id, fecha_generacion, estado, usuario_voluntario, patente_vehiculo "
					+ "FROM ordenes_retiro WHERE estado = ?");
			statement.setString(1, estado);
			rs = statement.executeQuery();
			
			while (rs.next()) {
				try {
					OrdenRetiro orden = mapearResultadoOrden(rs, conn);
					ordenes.add(orden);
				} catch (Exception e) {
					System.err.println("Error al crear OrdenRetiro: " + e.getMessage());
				}
			}
		} finally {
			if (rs != null) rs.close();
			if (statement != null) statement.close();
		}
		return ordenes;
	}

	@Override
	public List<OrdenRetiro> findByVoluntario(String username, Connection conn) throws SQLException {
		List<OrdenRetiro> ordenes = new ArrayList<OrdenRetiro>();
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			statement = conn.prepareStatement(
					"SELECT id, fecha_generacion, estado, usuario_voluntario, patente_vehiculo "
					+ "FROM ordenes_retiro WHERE usuario_voluntario = ?");
			statement.setString(1, username);
			rs = statement.executeQuery();
			
			while (rs.next()) {
				try {
					OrdenRetiro orden = mapearResultadoOrden(rs, conn);
					ordenes.add(orden);
				} catch (Exception e) {
					System.err.println("Error al crear OrdenRetiro: " + e.getMessage());
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
			int idOrden = rs.getInt("id");
			List<PedidosDonacion> pedidos = pedidoDao.findByOrden(idOrden, conn); // obtener pedidos para esta orden
			OrdenRetiro orden = new OrdenRetiro(pedidos, null); // crea orden sin voluntario ni vehiculo
			String usuarioVoluntario = rs.getString("usuario_voluntario"); // obtener y asignar voluntario
			if (usuarioVoluntario != null) {
				Usuario voluntario = usuarioDao.find(usuarioVoluntario, conn);
				if (voluntario != null) {
					orden.asignarVoluntario(voluntario);
				}
			}
			
			String patenteVehiculo = rs.getString("patente_vehiculo"); // obtener y asignar vehiculo
			if (patenteVehiculo != null) {
				Vehiculo vehiculo = vehiculoDao.findByPatente(patenteVehiculo, conn);
				if (vehiculo != null) {
					orden.asignarVehiculo(vehiculo);
				}
			}
			// no asignamos visitas aqui para evitar complejidad
			// se pueden cargar aparte con VisitaDao si es necesario
			return orden;
		} catch (Exception e) {
			throw new SQLException("Error al mapear OrdenRetiro", e);
		}
	}

}
