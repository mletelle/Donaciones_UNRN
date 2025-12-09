package ar.edu.unrn.seminario.accesos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ar.edu.unrn.seminario.modelo.Bien;

public class BienDAOJDBC implements BienDao {

    @Override
    public void createBatch(List<Bien> bienes, int idPedido, Connection conn) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = conn.prepareStatement(
                    "INSERT INTO bienes(id_pedido_donacion, categoria, cantidad, tipo, descripcion, fecha_vencimiento, estado_inventario) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)");
            
            for (Bien bien : bienes) {
                statement.setInt(1, idPedido);
                statement.setInt(2, bien.obtenerCategoria());
                statement.setInt(3, bien.obtenerCantidad());
                statement.setInt(4, bien.obtenerTipo());
                
                if (bien.getDescripcion() != null) statement.setString(5, bien.getDescripcion());
                else statement.setNull(5, java.sql.Types.VARCHAR);
                
                if (bien.getFecVec() != null) statement.setDate(6, new java.sql.Date(bien.getFecVec().getTime()));
                else statement.setNull(6, java.sql.Types.DATE);
                
                // Si el objeto no tiene estado, por defecto es PENDIENTE
                String estado = bien.getEstadoInventario() != null ? bien.getEstadoInventario() : "PENDIENTE";
                statement.setString(7, estado);

                statement.addBatch();
            }
            statement.executeBatch();
        } finally {
            if (statement != null) statement.close();
        }
    }

    @Override
    public void updateEstadoPorPedido(int idPedido, String nuevoEstado, Connection conn) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = conn.prepareStatement(
                "UPDATE bienes SET estado_inventario = ? WHERE id_pedido_donacion = ?");
            statement.setString(1, nuevoEstado);
            statement.setInt(2, idPedido);
            statement.executeUpdate();
        } finally {
            if (statement != null) statement.close();
        }
    }

    @Override
    public List<Bien> findByEstadoInventario(String estado, Connection conn) throws SQLException {
        List<Bien> bienes = new ArrayList<>();
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            statement = conn.prepareStatement("SELECT * FROM bienes WHERE estado_inventario = ?");
            statement.setString(1, estado);
            rs = statement.executeQuery();
            
            while (rs.next()) {
                bienes.add(mapearBien(rs));
            }
        } finally {
            if (rs != null) rs.close();
            if (statement != null) statement.close();
        }
        return bienes;
    }

    @Override
    public Bien findById(int id, Connection conn) throws SQLException {
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            statement = conn.prepareStatement("SELECT * FROM bienes WHERE id = ?");
            statement.setInt(1, id);
            rs = statement.executeQuery();
            if (rs.next()) {
                return mapearBien(rs);
            }
        } finally {
            if (rs != null) rs.close();
            if (statement != null) statement.close();
        }
        return null;
    }

    @Override
    public void update(Bien bien, Connection conn) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = conn.prepareStatement(
                "UPDATE bienes SET cantidad = ?, descripcion = ?, estado_inventario = ? WHERE id = ?");
            
            statement.setInt(1, bien.obtenerCantidad());
            statement.setString(2, bien.getDescripcion());
            statement.setString(3, bien.getEstadoInventario());
            statement.setInt(4, bien.getId()); // Usamos el ID del objeto Bien
            
            statement.executeUpdate();
        } finally {
            if (statement != null) statement.close();
        }
    }

    @Override
    public void asociarAOrdenEntrega(int idBien, int idOrdenEntrega, String nuevoEstado, Connection conn) throws SQLException {
        try (PreparedStatement statement = conn.prepareStatement(
                "UPDATE bienes SET id_orden_entrega = ?, estado_inventario = ? WHERE id = ?")) {
            statement.setInt(1, idOrdenEntrega);
            statement.setString(2, nuevoEstado);
            statement.setInt(3, idBien);
            statement.executeUpdate();
        }
    }

    // Helper para mapear ResultSet a Objeto Bien
    private Bien mapearBien(ResultSet rs) throws SQLException {
        try {
            Bien bien = new Bien(
                rs.getInt("tipo"),
                rs.getInt("cantidad"),
                rs.getInt("categoria")
            );
            bien.setId(rs.getInt("id")); 
            
            bien.setDescripcion(rs.getString("descripcion"));
            bien.setEstadoInventario(rs.getString("estado_inventario"));
            
            if (rs.getDate("fecha_vencimiento") != null) {
                bien.setFecVec(new java.util.Date(rs.getDate("fecha_vencimiento").getTime()));
            }
            return bien;
        } catch (Exception e) {
            throw new SQLException("Error mapeando bien: " + e.getMessage(), e);
        }
    }
}