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
    
    private static final String SQL_SELECT_BY_CODIGO = "SELECT r.codigo, r.nombre, r.activo FROM roles r WHERE r.codigo = ?";
    private static final String SQL_SELECT_ALL = "SELECT r.nombre, r.codigo, r.activo FROM roles r";

	@Override
	public Rol find(Integer codigo) throws PersistenceException {
		try (Connection conn = ConnectionManager.getConnection();
		     PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_CODIGO)) {
			stmt.setInt(1, codigo);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return mapearRol(rs);
				}
			}
			return null;
		} catch (SQLException e) {
			throw new PersistenceException("error al buscar rol por codigo " + codigo + ": " + e.getMessage(), e);
		} catch (Exception e) {
			throw new PersistenceException("error al mapear datos del rol: " + e.getMessage(), e);
		}
	}
	
	@Override
	public Rol findById(Integer codigo) throws PersistenceException {
		return find(codigo);
	}

	@Override
	public List<Rol> findAll() throws PersistenceException {
		List<Rol> listado = new ArrayList<>();
		try (Connection conn = ConnectionManager.getConnection();
		     Statement stmt = conn.createStatement();
		     ResultSet rs = stmt.executeQuery(SQL_SELECT_ALL)) {
			while (rs.next()) {
				listado.add(mapearRol(rs));
			}
		} catch (SQLException e) {
			throw new PersistenceException("error al buscar todos los roles: " + e.getMessage(), e);
		} catch (Exception e) {
			throw new PersistenceException("error al mapear lista de roles: " + e.getMessage(), e);
		}
		return listado;
	}
	
	private Rol mapearRol(ResultSet rs) throws Exception {
		Rol rol = new Rol(rs.getInt("codigo"), rs.getString("nombre"));
		rol.setActivo(rs.getBoolean("activo"));
		return rol;
	}

}
