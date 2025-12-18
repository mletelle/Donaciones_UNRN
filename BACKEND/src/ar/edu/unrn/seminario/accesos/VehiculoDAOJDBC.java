package ar.edu.unrn.seminario.accesos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import ar.edu.unrn.seminario.exception.PersistenceException;
import ar.edu.unrn.seminario.modelo.Vehiculo;
// si bien la ui no crea vehiculos, el dao los debe poder buscar
public class VehiculoDAOJDBC implements VehiculoDao {

	@Override
	public Vehiculo findByPatente(String patente) throws PersistenceException {
		Connection conn = null;
		Vehiculo vehiculo = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			conn = ConnectionManager.getConnection();
			statement = conn.prepareStatement(
					"SELECT patente, tipoVeh, capacidad, estado FROM vehiculos WHERE patente = ?");
			statement.setString(1, patente);
			rs = statement.executeQuery();
			
			if (rs.next()) {
				vehiculo = new Vehiculo(
						rs.getString("patente"),
						rs.getString("estado"),
						rs.getString("tipoVeh"),
						rs.getInt("capacidad"));
			}
		} catch (SQLException e) {
			throw new PersistenceException("Error al buscar vehículo por patente: " + e.getMessage(), e);
		} finally {
			if (rs != null) try { rs.close(); } catch (SQLException e) {}
			if (statement != null) try { statement.close(); } catch (SQLException e) {}
			if (conn != null) try { conn.close(); } catch (SQLException e) {}
		}
		return vehiculo;
	}

	@Override
	public Vehiculo findDisponible(String tipoVehiculo) throws PersistenceException {
		Connection conn = null;
		Vehiculo vehiculo = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			conn = ConnectionManager.getConnection();
			statement = conn.prepareStatement(
					"SELECT patente, tipoVeh, capacidad, estado FROM vehiculos WHERE tipoVeh = ? AND estado = 'Disponible' LIMIT 1");
			statement.setString(1, tipoVehiculo);
			rs = statement.executeQuery();
			
			if (rs.next()) {
				vehiculo = new Vehiculo(
						rs.getString("patente"),
						rs.getString("estado"),
						rs.getString("tipoVeh"),
						rs.getInt("capacidad"));
			}
		} catch (SQLException e) {
			throw new PersistenceException("Error al buscar vehículo disponible: " + e.getMessage(), e);
		} finally {
			if (rs != null) try { rs.close(); } catch (SQLException e) {}
			if (statement != null) try { statement.close(); } catch (SQLException e) {}
			if (conn != null) try { conn.close(); } catch (SQLException e) {}
		}
		return vehiculo;
	}

}
