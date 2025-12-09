package ar.edu.unrn.seminario.accesos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import ar.edu.unrn.seminario.modelo.Rol;
import ar.edu.unrn.seminario.modelo.Usuario;

public class UsuarioDAOJDBC implements UsuarioDao {

	@Override
	public void create(Usuario usuario, Connection conn) throws SQLException {
		PreparedStatement statement = null;
		try {
			statement = conn
					.prepareStatement("INSERT INTO usuarios(usuario, contrasena, nombre, correo, activo, rol, apellido, dni, direccion) "
							+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
// atributos modificados de proyecto base, tiene mas atributos el nuestro
			statement.setString(1, usuario.getUsuario());
			statement.setString(2, usuario.getContrasena());
			statement.setString(3, usuario.getNombre());
			statement.setString(4, usuario.getEmail());
			statement.setBoolean(5, usuario.isActivo());
			statement.setInt(6, usuario.getRol().getCodigo());
			statement.setString(7, usuario.getApellido());
			statement.setInt(8, usuario.getDni());
			statement.setString(9, usuario.obtenerDireccion());
			
			int cantidad = statement.executeUpdate();
			if (cantidad <= 0) {
				throw new SQLException("Error al insertar usuario");
			}
		} finally {
			if (statement != null) statement.close();
		}
	}

	@Override
	public void update(Usuario usuario, Connection conn) throws SQLException {
		PreparedStatement statement = null;
		try {
			statement = conn.prepareStatement(
					"UPDATE usuarios SET contrasena = ?, nombre = ?, correo = ?, activo = ?, apellido = ?, dni = ?, direccion = ? WHERE usuario = ?");
			// no se puede modificar el usuario ni el rol, pero quedan por las dudas
			statement.setString(1, usuario.getContrasena());
			statement.setString(2, usuario.getNombre());
			statement.setString(3, usuario.getEmail());
			statement.setBoolean(4, usuario.isActivo());
			statement.setString(5, usuario.getApellido());
			statement.setInt(6, usuario.getDni());
			statement.setString(7, usuario.obtenerDireccion());
			statement.setString(8, usuario.getUsuario());
			
			statement.executeUpdate();
		} finally {
			if (statement != null) statement.close();
		}
	}

	@Override
	public Usuario find(String username, Connection conn) throws SQLException {
		Usuario usuario = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			statement = conn.prepareStatement(
					"SELECT u.usuario, u.contrasena, u.nombre, u.correo, u.activo, u.apellido, u.dni, u.direccion, r.codigo as codigo_rol, r.nombre as nombre_rol "
							+ "FROM usuarios u JOIN roles r ON (u.rol = r.codigo) "
							+ "WHERE u.usuario = ?");

			statement.setString(1, username);
			rs = statement.executeQuery();
			if (rs.next()) {
				try {
					Rol rol = new Rol(rs.getInt("codigo_rol"), rs.getString("nombre_rol"));
					usuario = new Usuario(
							rs.getString("usuario"), 
							rs.getString("contrasena"), 
							rs.getString("nombre"),
							rs.getString("correo"), 
							rol, 
							rs.getString("apellido"), 
							rs.getInt("dni"), 
							rs.getString("direccion"),
							rs.getString("Contacto"),
							rs.getString("Ubicacion"));
					if (!rs.getBoolean("activo")) {
						usuario.desactivar();
					}
				} catch (Exception e) {
					throw new SQLException("Error buscando Usuario ", e);
				}
			}
		} finally {
			if (rs != null) rs.close();
			if (statement != null) statement.close();
		}
		return usuario;
	}

	@Override
	public List<Usuario> findAll(Connection conn) throws SQLException {
		List<Usuario> usuarios = new ArrayList<Usuario>();
		Statement statement = null;
		ResultSet rs = null;
		try {
			statement = conn.createStatement();
			rs = statement.executeQuery(
					"SELECT u.usuario, u.contrasena, u.nombre, u.correo, u.activo, u.apellido, u.dni, u.direccion, r.codigo as codigo_rol, r.nombre as nombre_rol "
							+ "FROM usuarios u JOIN roles r ON (u.rol = r.codigo)");
// para el listado completo, sin filtro de activos
			while (rs.next()) {
				try {
					Rol rol = new Rol(rs.getInt("codigo_rol"), rs.getString("nombre_rol"));
					Usuario usuario = new Usuario(
							rs.getString("usuario"), 
							rs.getString("contrasena"),
							rs.getString("nombre"), 
							rs.getString("correo"), 
							rol, 
							rs.getString("apellido"), 
							rs.getInt("dni"),
							rs.getString("direccion"),
							rs.getString("Contacto"),
							rs.getString("Ubicacion"));
					if (!rs.getBoolean("activo")) {
						usuario.desactivar();
					}
					usuarios.add(usuario);
				} catch (Exception e) {
					System.err.println("Error encontrando Usuario: " + e.getMessage());
				}
			}
		} finally {
			if (rs != null) rs.close();
			if (statement != null) statement.close();
		}
		return usuarios;
	}

	@Override
	public List<Usuario> findByRol(int codigoRol, Connection conn) throws SQLException {
		List<Usuario> usuarios = new ArrayList<Usuario>();
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			statement = conn.prepareStatement(
					"SELECT u.usuario, u.contrasena, u.nombre, u.correo, u.activo, u.apellido, u.dni, u.direccion, r.codigo as codigo_rol, r.nombre as nombre_rol "
							+ "FROM usuarios u JOIN roles r ON (u.rol = r.codigo) "
							+ "WHERE r.codigo = ? AND u.activo = 1");
			
			statement.setInt(1, codigoRol);
			rs = statement.executeQuery();

			while (rs.next()) {
				try {
					Rol rol = new Rol(rs.getInt("codigo_rol"), rs.getString("nombre_rol"));
					Usuario usuario = new Usuario(
							rs.getString("usuario"), 
							rs.getString("contrasena"),
							rs.getString("nombre"), 
							rs.getString("correo"), 
							rol, 
							rs.getString("apellido"), 
							rs.getInt("dni"),
							rs.getString("direccion"),
							rs.getString("Contacto"),
							rs.getString("Ubicacion"));
					usuarios.add(usuario);
				} catch (Exception e) {
					System.err.println("Error buscando Usuario: " + e.getMessage());
				}
			}
		} finally {
			if (rs != null) rs.close();
			if (statement != null) statement.close();
		}
		return usuarios;
	}
	public Usuario findByDni(int dni, Connection conn) throws SQLException {
	    Usuario usuario = null;
	    PreparedStatement statement = null;
	    ResultSet rs = null;
	    try {
	        statement = conn.prepareStatement(
	                "SELECT u.usuario, u.contrasena, u.nombre, u.correo, u.activo, u.apellido, u.dni, u.direccion, r.codigo as codigo_rol, r.nombre as nombre_rol "
	                        + "FROM usuarios u JOIN roles r ON (u.rol = r.codigo) "
	                        + "WHERE u.dni = ?"); // Búsqueda por DNI

	        statement.setInt(1, dni);
	        rs = statement.executeQuery();
	        
	        if (rs.next()) { // Solo debe haber uno
	            try {
	                Rol rol = new Rol(rs.getInt("codigo_rol"), rs.getString("nombre_rol"));
	                usuario = new Usuario(
	                        rs.getString("usuario"), 
	                        rs.getString("contrasena"), 
	                        rs.getString("nombre"),
	                        rs.getString("correo"), 
	                        rol, 
	                        rs.getString("apellido"), 
	                        rs.getInt("dni"), 
	                        rs.getString("direccion"),
	                        rs.getString("Contacto"),
							rs.getString("Ubicacion"));
	                
	                if (!rs.getBoolean("activo")) {
	                    usuario.desactivar();
	                }
	            } catch (Exception e) {
	                throw new SQLException("Error buscando Usuario por DNI", e);
	            }
	        }
	    } finally {
	        if (rs != null) rs.close();
	        if (statement != null) statement.close();
	    }
	    return usuario;
	}

}