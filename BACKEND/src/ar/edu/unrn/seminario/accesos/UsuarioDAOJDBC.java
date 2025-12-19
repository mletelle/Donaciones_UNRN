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
import ar.edu.unrn.seminario.modelo.Usuario;

public class UsuarioDAOJDBC implements UsuarioDao {

    @Override
    public void create(Usuario usuario) throws PersistenceException {
        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                insertarUsuario(conn, usuario);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new PersistenceException("Error al crear usuario: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new PersistenceException("Error de conexión: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Usuario usuario) throws PersistenceException {
        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                actualizarUsuario(conn, usuario);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new PersistenceException("Error al actualizar usuario: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new PersistenceException("Error de conexión: " + e.getMessage(), e);
        }
    }

    @Override
    public Usuario find(String username) throws PersistenceException {
        String sql = "SELECT u.*, r.codigo as codigo_rol, r.nombre as nombre_rol "
                   + "FROM usuarios u JOIN roles r ON (u.rol = r.codigo) "
                   + "WHERE u.usuario = ?";
        
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new PersistenceException("Error buscando usuario: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new PersistenceException("Error al mapear usuario: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Usuario> findAll() throws PersistenceException {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT u.*, r.codigo as codigo_rol, r.nombre as nombre_rol "
                   + "FROM usuarios u JOIN roles r ON (u.rol = r.codigo)";

        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }
        } catch (Exception e) {
            throw new PersistenceException("Error al buscar todos los usuarios: " + e.getMessage(), e);
        }
        return usuarios;
    }

    @Override
    public List<Usuario> findByRol(int codigoRol) throws PersistenceException {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT u.*, r.codigo as codigo_rol, r.nombre as nombre_rol "
                   + "FROM usuarios u JOIN roles r ON (u.rol = r.codigo) "
                   + "WHERE r.codigo = ? AND u.activo = 1";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, codigoRol);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    usuarios.add(mapearUsuario(rs));
                }
            }
        } catch (Exception e) {
            throw new PersistenceException("Error buscando usuarios por rol: " + e.getMessage(), e);
        }
        return usuarios;
    }

    @Override
    public Usuario findByDni(int dni) throws PersistenceException {
        String sql = "SELECT u.*, r.codigo as codigo_rol, r.nombre as nombre_rol "
                   + "FROM usuarios u JOIN roles r ON (u.rol = r.codigo) "
                   + "WHERE u.dni = ?";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, dni);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }
            return null;
        } catch (Exception e) {
            throw new PersistenceException("Error buscando usuario por DNI: " + e.getMessage(), e);
        }
    }

    private void insertarUsuario(Connection conn, Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuarios(usuario, contrasena, nombre, correo, activo, rol, apellido, dni, direccion, necesidad, personas_cargo, prioridad) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usuario.getUsuario());
            stmt.setString(2, usuario.getContrasena());
            stmt.setString(3, usuario.getNombre());
            stmt.setString(4, usuario.getEmail());
            stmt.setBoolean(5, usuario.isActivo());
            stmt.setInt(6, usuario.getRol().getCodigo());
            stmt.setString(7, usuario.getApellido());
            stmt.setInt(8, usuario.getDni());
            stmt.setString(9, usuario.obtenerDireccion());
            
            if (usuario.getNecesidad() != null) {
                stmt.setString(10, usuario.getNecesidad());
            } else {
                stmt.setNull(10, java.sql.Types.VARCHAR);
            }
            
            if (usuario.getPersonasACargo() != null) {
                stmt.setInt(11, usuario.getPersonasACargo());
            } else {
                stmt.setNull(11, java.sql.Types.INTEGER);
            }
            
            if (usuario.getPrioridad() != null) {
                stmt.setString(12, usuario.getPrioridad());
            } else {
                stmt.setNull(12, java.sql.Types.VARCHAR);
            }
            
            stmt.executeUpdate();
        }
    }

    private void actualizarUsuario(Connection conn, Usuario usuario) throws SQLException {
        String sql = "UPDATE usuarios SET contrasena = ?, nombre = ?, correo = ?, activo = ?, apellido = ?, dni = ?, direccion = ? WHERE usuario = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usuario.getContrasena());
            stmt.setString(2, usuario.getNombre());
            stmt.setString(3, usuario.getEmail());
            stmt.setBoolean(4, usuario.isActivo());
            stmt.setString(5, usuario.getApellido());
            stmt.setInt(6, usuario.getDni());
            stmt.setString(7, usuario.obtenerDireccion());
            stmt.setString(8, usuario.getUsuario());
            
            stmt.executeUpdate();
        }
    }

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
                rs.getString("prioridad")
        );
        
        if (!rs.getBoolean("activo")) {
            u.desactivar();
        }
        return u;
    }
}