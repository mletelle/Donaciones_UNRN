package ar.edu.unrn.seminario.accesos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import ar.edu.unrn.seminario.modelo.Rol;

public class RolDAOJDBC implements RolDao {

	@Override
	public Rol find(Integer codigo, Connection conn) throws SQLException {
		Rol rol = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			statement = conn.prepareStatement(
					"SELECT r.codigo, r.nombre, r.activo FROM roles r WHERE r.codigo = ?");

			statement.setInt(1, codigo);
			rs = statement.executeQuery();
			if (rs.next()) {
				try {
					rol = new Rol(rs.getInt("codigo"), rs.getString("nombre"));
					rol.setActivo(rs.getBoolean("activo"));
				} catch (Exception e) {
					throw new SQLException("Error Rol", e);
				}
			}
		} finally {
			if (rs != null) rs.close();
			if (statement != null) statement.close();
		}
		return rol;
	}

	@Override
	public List<Rol> findAll(Connection conn) throws SQLException {
		List<Rol> listado = new ArrayList<Rol>();
		Statement sentencia = null;
		ResultSet resultado = null;
		try {
			sentencia = conn.createStatement();
			resultado = sentencia.executeQuery("SELECT r.nombre, r.codigo, r.activo FROM roles r");

			while (resultado.next()) {
				try {
					Rol rol = new Rol();
					rol.setNombre(resultado.getString("nombre"));
					rol.setCodigo(resultado.getInt("codigo"));
					rol.setActivo(resultado.getBoolean("activo"));
					listado.add(rol);
				} catch (Exception e) {
					System.err.println("Error Rol: " + e.getMessage());
				}
			}
		} finally {
			if (resultado != null) resultado.close();
			if (sentencia != null) sentencia.close();
		}
		return listado;
	}

}
