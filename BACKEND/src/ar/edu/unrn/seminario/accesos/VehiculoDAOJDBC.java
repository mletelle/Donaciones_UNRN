package ar.edu.unrn.seminario.accesos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import ar.edu.unrn.seminario.modelo.Vehiculo;
// si bien la ui no crea vehiculos, el dao los debe poder buscar
public class VehiculoDAOJDBC implements VehiculoDao {

	@Override
	public Vehiculo findByPatente(String patente, Connection conn) throws SQLException {
		Vehiculo vehiculo = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
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
		} finally {
			if (rs != null) rs.close();
			if (statement != null) statement.close();
		}
		return vehiculo;
	}

	@Override
	public Vehiculo findDisponible(String tipoVehiculo, Connection conn) throws SQLException {
		Vehiculo vehiculo = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
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
		} finally {
			if (rs != null) rs.close();
			if (statement != null) statement.close();
		}
		return vehiculo;
	}

}
