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
            // Se agrega 'prioridad' al INSERT
            statement = conn.prepareStatement(
                "INSERT INTO usuarios(usuario, contrasena, nombre, correo, activo, rol, apellido, dni, direccion, necesidad, personas_cargo, prioridad) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            
            statement.setString(1, usuario.getUsuario());
            statement.setString(2, usuario.getContrasena());
            statement.setString(3, usuario.getNombre());
            statement.setString(4, usuario.getEmail());
            statement.setBoolean(5, usuario.isActivo());
            statement.setInt(6, usuario.getRol().getCodigo());
            statement.setString(7, usuario.getApellido());
            statement.setInt(8, usuario.getDni());
            statement.setString(9, usuario.obtenerDireccion());
            
            if (usuario.getNecesidad() != null) statement.setString(10, usuario.getNecesidad());
            else statement.setNull(10, java.sql.Types.VARCHAR);
            
            if (usuario.getPersonasACargo() != null) statement.setInt(11, usuario.getPersonasACargo());
            else statement.setNull(11, java.sql.Types.INTEGER);
            
            if (usuario.getPrioridad() != null) statement.setString(12, usuario.getPrioridad());
            else statement.setNull(12, java.sql.Types.VARCHAR);
            
            int cantidad = statement.executeUpdate();
            if (cantidad <= 0) throw new SQLException("Error al insertar usuario");
            
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
                    "SELECT u.*, r.codigo as codigo_rol, r.nombre as nombre_rol "
                            + "FROM usuarios u JOIN roles r ON (u.rol = r.codigo) "
                            + "WHERE u.usuario = ?");

            statement.setString(1, username);
            rs = statement.executeQuery();
            if (rs.next()) {
                usuario = mapearUsuario(rs);
            }
        } catch (Exception e) {
            throw new SQLException("Error buscando Usuario " + username, e);
        } finally {
            if (rs != null) rs.close();
            if (statement != null) statement.close();
        }
        return usuario;
    }

    @Override
    public List<Usuario> findAll(Connection conn) throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = conn.createStatement();
            rs = statement.executeQuery(
                    "SELECT u.*, r.codigo as codigo_rol, r.nombre as nombre_rol "
                            + "FROM usuarios u JOIN roles r ON (u.rol = r.codigo)");
            while (rs.next()) {
                try {
                    usuarios.add(mapearUsuario(rs));
                } catch (Exception e) {
                    System.err.println("Error mapeando usuario: " + e.getMessage());
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
        List<Usuario> usuarios = new ArrayList<>();
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            statement = conn.prepareStatement(
                    "SELECT u.*, r.codigo as codigo_rol, r.nombre as nombre_rol "
                            + "FROM usuarios u JOIN roles r ON (u.rol = r.codigo) "
                            + "WHERE r.codigo = ? AND u.activo = 1");
            
            statement.setInt(1, codigoRol);
            rs = statement.executeQuery();
            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }
        } catch (Exception e) {
            throw new SQLException("Error buscando por rol", e);
        } finally {
            if (rs != null) rs.close();
            if (statement != null) statement.close();
        }
        return usuarios;
    }

    @Override
    public Usuario findByDni(int dni, Connection conn) throws SQLException {
        Usuario usuario = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            statement = conn.prepareStatement(
                    "SELECT u.*, r.codigo as codigo_rol, r.nombre as nombre_rol "
                            + "FROM usuarios u JOIN roles r ON (u.rol = r.codigo) "
                            + "WHERE u.dni = ?");

            statement.setInt(1, dni);
            rs = statement.executeQuery();
            if (rs.next()) {
                usuario = mapearUsuario(rs);
            }
        } catch (Exception e) {
            throw new SQLException("Error buscando Usuario por DNI", e);
        } finally {
            if (rs != null) rs.close();
            if (statement != null) statement.close();
        }
        return usuario;
    }
    
    // Método auxiliar para no repetir código de mapeo en todos los finds
    private Usuario mapearUsuario(ResultSet rs) throws Exception {
        Rol rol = new Rol(rs.getInt("codigo_rol"), rs.getString("nombre_rol"));
        
        Usuario u = new Usuario(
                rs.getString("usuario"), 
                rs.getString("contrasena"), 
                rs.getString("nombre"),
                rs.getString("correo"), 
                rol, 
                rs.getString("apellido"), 
                rs.getInt("dni"), 
                rs.getString("direccion"),
                rs.getString("necesidad"),
                rs.getObject("personas_cargo") != null ? rs.getInt("personas_cargo") : 0,
                rs.getString("prioridad") // Nuevo campo
        );
        
        if (!rs.getBoolean("activo")) {
            u.desactivar();
        }
        return u;
    }
}