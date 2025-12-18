package ar.edu.unrn.seminario.accesos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import ar.edu.unrn.seminario.exception.PersistenceException;
import ar.edu.unrn.seminario.modelo.Rol;

public class RolDAOJDBC implements RolDao {

	@Override
	public Rol find(Integer codigo) throws PersistenceException {
		Connection conn = null;
		Rol rol = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			conn = ConnectionManager.getConnection();
			statement = conn.prepareStatement(
					"SELECT r.codigo, r.nombre, r.activo FROM roles r WHERE r.codigo = ?");

			statement.setInt(1, codigo);
			rs = statement.executeQuery();
			if (rs.next()) {
				try {
					rol = new Rol(rs.getInt("codigo"), rs.getString("nombre"));
					rol.setActivo(rs.getBoolean("activo"));
				} catch (Exception e) {
					throw new PersistenceException("Error al mapear rol: " + e.getMessage(), e);
				}
			}
		} catch (SQLException e) {
			throw new PersistenceException("Error al buscar rol por código: " + e.getMessage(), e);
		} finally {
			if (rs != null) try { rs.close(); } catch (SQLException e) {}
			if (statement != null) try { statement.close(); } catch (SQLException e) {}
			if (conn != null) try { conn.close(); } catch (SQLException e) {}
		}
		return rol;
	}
	
	@Override
	public Rol findById(Integer codigo) throws PersistenceException {
		return find(codigo);
	}

	@Override
	public List<Rol> findAll() throws PersistenceException {
		Connection conn = null;
		List<Rol> listado = new ArrayList<Rol>();
		Statement sentencia = null;
		ResultSet resultado = null;
		try {
			conn = ConnectionManager.getConnection();
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
					System.err.println("error rol: " + e.getMessage());
				}
			}
		} catch (SQLException e) {
			throw new PersistenceException("Error al buscar todos los roles: " + e.getMessage(), e);
		} finally {
			if (resultado != null) try { resultado.close(); } catch (SQLException e) {}
			if (sentencia != null) try { sentencia.close(); } catch (SQLException e) {}
			if (conn != null) try { conn.close(); } catch (SQLException e) {}
		}
		return listado;
	}

}
