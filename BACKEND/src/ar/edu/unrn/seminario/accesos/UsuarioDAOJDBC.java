package ar.edu.unrn.seminario.accesos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import ar.edu.unrn.seminario.exception.CampoVacioException;
import ar.edu.unrn.seminario.exception.ObjetoNuloException;
import ar.edu.unrn.seminario.modelo.Rol;
import ar.edu.unrn.seminario.modelo.Usuario;

public class UsuarioDAOJDBC implements UsuarioDao {

	@Override
	public void create(Usuario usuario) {
		try {

			Connection conn = ConnectionManager.getConnection();
			// SQL CORREGIDO: Añadidos apellido, dni, direccion
			// Tu dump usa 'correo' para email y DNI es VARCHAR. Ajustado a eso.
			PreparedStatement statement = conn
					.prepareStatement("INSERT INTO usuarios(usuario, contrasena, nombre, apellido, dni, correo, activo, rol, direccion) "
							+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");

			statement.setString(1, usuario.getUsuario());
			statement.setString(2, usuario.getContrasena());
			statement.setString(3, usuario.getNombre());
			statement.setString(4, usuario.getApellido()); 
			statement.setString(5, String.valueOf(usuario.getDni())); // Convertido a String para DNI VARCHAR
			statement.setString(6, usuario.getEmail());
			statement.setBoolean(7, usuario.isActivo());
			statement.setInt(8, usuario.getRol().getCodigo());
			statement.setString(9, usuario.getDireccion()); 
			
			int cantidad = statement.executeUpdate();
			if (cantidad == 0) {
				System.out.println("Error al crear el usuario en la DB.");
				// TODO: disparar Exception propia
			}

		} catch (SQLException e) {
			System.out.println("Error al procesar consulta SQL: " + e.getMessage());
			// TODO: disparar Exception propia
		} catch (Exception e) {
			System.out.println("Error al insertar un usuario: " + e.getMessage());
			// TODO: disparar Exception propia
		} finally {
			ConnectionManager.disconnect();
		}

	}

	@Override
	public void update(Usuario usuario) {
		// TODO Auto-generated method stub

	}

	@Override
	public void remove(Long id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void remove(Usuario rol) {
		// TODO Auto-generated method stub
	}

	@Override
	public Usuario find(String username) {
		Usuario usuario = null;
		try {
			Connection conn = ConnectionManager.getConnection();
			// SQL CORREGIDO: Añadidos u.apellido, u.dni, u.direccion. Se usa 'correo'.
			PreparedStatement statement = conn.prepareStatement(
					"SELECT u.usuario, u.contrasena, u.nombre, u.apellido, u.dni, u.correo, u.activo, u.direccion, r.codigo as codigo_rol, r.nombre as nombre_rol "
							+ " FROM usuarios u JOIN roles r ON (u.rol = r.codigo) " + " WHERE u.usuario = ?");

			statement.setString(1, username);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				Rol rol = new Rol(rs.getInt("codigo_rol"), rs.getString("nombre_rol"));
				
				// CONSTRUCTOR CORREGIDO: Usa los 8 parámetros
				usuario = new Usuario(
						rs.getString("usuario"), 
						rs.getString("contrasena"), 
						rs.getString("nombre"),
						rs.getString("correo"), // Usar 'correo'
						rol, 
						rs.getString("apellido"), 
						Integer.parseInt(rs.getString("dni")), // Convertir DNI VARCHAR a int
						rs.getString("direccion")
				);
				if (rs.getBoolean("activo") == false) { // Cargar estado de activación
					usuario.desactivar();
				}
			}

		} catch (SQLException e) {
			System.out.println("Error al procesar consulta: " + e.getMessage());
		} catch (CampoVacioException | ObjetoNuloException e) {
			System.out.println("Error al crear usuario desde la DB: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("Error general al buscar usuario: " + e.getMessage());
		} finally {
			ConnectionManager.disconnect();
		}

		return usuario;
	}

	@Override
	public List<Usuario> findAll() {
		List<Usuario> usuarios = new ArrayList<Usuario>();
		try {
			Connection conn = ConnectionManager.getConnection();
			Statement statement = conn.createStatement();
			// SQL CORREGIDO: Añadidos u.apellido, u.dni, u.direccion. Se usa 'correo'.
			ResultSet rs = statement.executeQuery(
					"SELECT u.usuario, u.contrasena, u.nombre, u.apellido, u.dni, u.correo, u.activo, u.direccion, r.codigo as codigo_rol, r.nombre as nombre_rol "
							+ "FROM usuarios u JOIN roles r ON (u.rol = r.codigo) ");

			while (rs.next()) {

				Rol rol = new Rol(rs.getInt("codigo_rol"), rs.getString("nombre_rol"));
				
				// CONSTRUCTOR CORREGIDO: Usa los 8 parámetros
				Usuario usuario = new Usuario(
						rs.getString("usuario"), 
						rs.getString("contrasena"),
						rs.getString("nombre"), 
						rs.getString("correo"), // Usar 'correo'
						rol, 
						rs.getString("apellido"), 
						Integer.parseInt(rs.getString("dni")), // Convertir DNI VARCHAR a int
						rs.getString("direccion")
				);
				if (rs.getBoolean("activo") == false) { // Cargar estado de activación
					usuario.desactivar();
				}
				usuarios.add(usuario);
			}
		} catch (SQLException e) {
			System.out.println("Error de mySql\n" + e.toString());
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (CampoVacioException | ObjetoNuloException e) {
			System.out.println("Error al crear usuario desde la DB: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("Error general al buscar usuarios: " + e.getMessage());
			e.printStackTrace();
		} finally {
			ConnectionManager.disconnect();
		}

		return usuarios;
	}

}