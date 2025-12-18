package ar.edu.unrn.seminario.accesos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import ar.edu.unrn.seminario.exception.PersistenceException;
import ar.edu.unrn.seminario.modelo.Bien;
import ar.edu.unrn.seminario.modelo.EstadoBien;
import ar.edu.unrn.seminario.modelo.CategoriaBien;

public class BienDAOJDBC implements BienDao {

    @Override
    public void createBatch(List<Bien> bienes, int idPedido) throws PersistenceException {
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);
            
            statement = conn.prepareStatement(
                    "INSERT INTO bienes(id_pedido_donacion, categoria, cantidad, descripcion, fecha_vencimiento, estado_inventario) "
                    + "VALUES (?, ?, ?, ?, ?, ?)");
            
            for (Bien bien : bienes) {
                statement.setInt(1, idPedido);
                statement.setInt(2, mapCategoriaToId(bien.obtenerCategoria()));
                statement.setInt(3, bien.obtenerCantidad());
                
                if (bien.getDescripcion() != null) statement.setString(4, bien.getDescripcion());
                else statement.setNull(4, java.sql.Types.VARCHAR);
                
                if (bien.getFecVec() != null) statement.setDate(5, new java.sql.Date(bien.getFecVec().getTime()));
                else statement.setNull(5, java.sql.Types.DATE);
                
                String estado = bien.getEstadoInventario() != null ? bien.getEstadoInventario().name() : "PENDIENTE";
                statement.setString(6, estado);

                statement.addBatch();
            }
            statement.executeBatch();
            
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new PersistenceException("error al crear batch de bienes: " + e.getMessage(), e);
        } finally {
            if (statement != null) try { statement.close(); } catch (SQLException e) {}
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
    }

    @Override
    public void updateEstadoPorPedido(int idPedido, String nuevoEstado) throws PersistenceException {
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);
            
            statement = conn.prepareStatement(
                "UPDATE bienes SET estado_inventario = ? WHERE id_pedido_donacion = ?");
            statement.setString(1, nuevoEstado);
            statement.setInt(2, idPedido);
            statement.executeUpdate();
            
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new PersistenceException("error al actualizar estado de bienes: " + e.getMessage(), e);
        } finally {
            if (statement != null) try { statement.close(); } catch (SQLException e) {}
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
    }

    @Override
    public List<Bien> findByEstadoInventario(String estado) throws PersistenceException {
        Connection conn = null;
        List<Bien> bienes = new ArrayList<>();
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            conn = ConnectionManager.getConnection();
            statement = conn.prepareStatement("SELECT * FROM bienes WHERE estado_inventario = ?");
            statement.setString(1, estado);
            rs = statement.executeQuery();
            
            while (rs.next()) {
                bienes.add(mapearBien(rs));
            }
        } catch (SQLException e) {
            throw new PersistenceException("error al buscar bienes por estado: " + e.getMessage(), e);
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) {}
            if (statement != null) try { statement.close(); } catch (SQLException e) {}
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
        return bienes;
    }

    @Override
    public Bien findById(int id) throws PersistenceException {
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            conn = ConnectionManager.getConnection();
            statement = conn.prepareStatement("SELECT * FROM bienes WHERE id = ?");
            statement.setInt(1, id);
            rs = statement.executeQuery();
            if (rs.next()) {
                return mapearBien(rs);
            }
        } catch (SQLException e) {
            throw new PersistenceException("error al buscar bien por id: " + e.getMessage(), e);
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) {}
            if (statement != null) try { statement.close(); } catch (SQLException e) {}
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
        return null;
    }

    @Override
    public void update(Bien bien) throws PersistenceException {
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);
            
            statement = conn.prepareStatement(
                "UPDATE bienes SET cantidad = ?, descripcion = ?, estado_inventario = ?, fecha_vencimiento = ? WHERE id = ?");
            
            statement.setInt(1, bien.obtenerCantidad());
            statement.setString(2, bien.getDescripcion());
            statement.setString(3, bien.getEstadoInventario().name());

            if (bien.getFecVec() != null) {
                statement.setDate(4, new java.sql.Date(bien.getFecVec().getTime()));
            } else {
                statement.setNull(4, java.sql.Types.DATE);
            }

            statement.setInt(5, bien.getId()); 
            
            statement.executeUpdate();
            
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new PersistenceException("error al actualizar bien: " + e.getMessage(), e);
        } finally {
            if (statement != null) try { statement.close(); } catch (SQLException e) {}
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
    }

    @Override
    public void asociarAOrdenEntrega(int idBien, int idOrdenEntrega, String nuevoEstado) throws PersistenceException {
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);
            
            statement = conn.prepareStatement(
                "UPDATE bienes SET id_orden_entrega = ?, estado_inventario = ? WHERE id = ?");
            statement.setInt(1, idOrdenEntrega);
            statement.setString(2, nuevoEstado);
            statement.setInt(3, idBien);
            statement.executeUpdate();
            
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new PersistenceException("error al asociar bien a orden: " + e.getMessage(), e);
        } finally {
            if (statement != null) try { statement.close(); } catch (SQLException e) {}
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
    }

    private Bien mapearBien(ResultSet rs) throws SQLException {
        try {
            CategoriaBien categoria = mapIdToCategoria(rs.getInt("categoria"));
            
            Bien bien = new Bien(rs.getInt("cantidad"), categoria);
            bien.setId(rs.getInt("id")); 
            
            bien.setDescripcion(rs.getString("descripcion"));
            bien.setEstadoInventario(EstadoBien.fromString(rs.getString("estado_inventario")));
            
            if (rs.getDate("fecha_vencimiento") != null) {
                bien.setFecVec(new java.util.Date(rs.getDate("fecha_vencimiento").getTime()));
            }
            return bien;
        } catch (Exception e) {
            throw new SQLException("error mapeando bien: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Bien> findByOrdenEntrega(int idOrdenEntrega) throws PersistenceException {
        Connection conn = null;
        List<Bien> bienes = new ArrayList<>();
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            conn = ConnectionManager.getConnection();
            statement = conn.prepareStatement("SELECT * FROM bienes WHERE id_orden_entrega = ?");
            statement.setInt(1, idOrdenEntrega);
            rs = statement.executeQuery();
            
            while (rs.next()) {
                bienes.add(mapearBien(rs));
            }
        } catch (SQLException e) {
            throw new PersistenceException("error al buscar bienes por orden: " + e.getMessage(), e);
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) {}
            if (statement != null) try { statement.close(); } catch (SQLException e) {}
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
        return bienes;
    }
    
    @Override
    public int create(Bien bien, int idPedidoOriginal) throws PersistenceException {
        Connection conn = null;
        String sql = "INSERT INTO bienes(id_pedido_donacion, categoria, cantidad, descripcion, fecha_vencimiento, estado_inventario) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";
        
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        
        try {
            conn = ConnectionManager.getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            stmt.setInt(1, idPedidoOriginal); 
            stmt.setInt(2, mapCategoriaToId(bien.obtenerCategoria()));
            stmt.setInt(3, bien.obtenerCantidad());
            
            if (bien.getDescripcion() != null) stmt.setString(4, bien.getDescripcion());
            else stmt.setNull(4, java.sql.Types.VARCHAR);
            
            if (bien.getFecVec() != null) stmt.setDate(5, new java.sql.Date(bien.getFecVec().getTime()));
            else stmt.setNull(5, java.sql.Types.DATE);
            
            stmt.setString(6, bien.getEstadoInventario().name());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) throw new SQLException("no se pudo crear el bien fraccionado");

            generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                throw new SQLException("no se obtuvo id del bien");
            }
        } catch (SQLException e) {
            throw new PersistenceException("error al crear bien: " + e.getMessage(), e);
        } finally {
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) {}
            if (stmt != null) try { stmt.close(); } catch (SQLException e) {}
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
    }
    
    @Override
    public int fraccionarYCrear(int idBienOriginal, int cantidadSolicitada) throws PersistenceException {
        Connection conn = null;
        PreparedStatement stmtUpdate = null;
        PreparedStatement stmtInsert = null;
        ResultSet generatedKeys = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);
            
            stmtUpdate = conn.prepareStatement("UPDATE bienes SET cantidad = cantidad - ? WHERE id = ?");
            stmtUpdate.setInt(1, cantidadSolicitada);
            stmtUpdate.setInt(2, idBienOriginal);
            stmtUpdate.executeUpdate();
            
            stmtInsert = conn.prepareStatement(
                "INSERT INTO bienes(id_pedido_donacion, categoria, cantidad, descripcion, fecha_vencimiento, estado_inventario) " +
                "SELECT id_pedido_donacion, categoria, ?, descripcion, fecha_vencimiento, ? FROM bienes WHERE id = ?",
                Statement.RETURN_GENERATED_KEYS
            );
            stmtInsert.setInt(1, cantidadSolicitada);
            stmtInsert.setString(2, EstadoBien.EN_STOCK.name());
            stmtInsert.setInt(3, idBienOriginal);
            stmtInsert.executeUpdate();
            
            generatedKeys = stmtInsert.getGeneratedKeys();
            int idGenerado;
            if (generatedKeys.next()) {
                idGenerado = generatedKeys.getInt(1);
            } else {
                throw new SQLException("no se pudo obtener el id del bien fraccionado");
            }
            
            conn.commit();
            return idGenerado;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new PersistenceException("error al fraccionar bien: " + e.getMessage(), e);
        } finally {
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) {}
            if (stmtUpdate != null) try { stmtUpdate.close(); } catch (SQLException e) {}
            if (stmtInsert != null) try { stmtInsert.close(); } catch (SQLException e) {}
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
    }
    
    @Override
    public int obtenerIdPedidoDeBien(int idBien) throws PersistenceException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConnectionManager.getConnection();
            stmt = conn.prepareStatement("SELECT id_pedido_donacion FROM bienes WHERE id = ?");
            stmt.setInt(1, idBien);
            rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
            return 0;
        } catch (SQLException e) {
            throw new PersistenceException("error al obtener pedido del bien: " + e.getMessage(), e);
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) {}
            if (stmt != null) try { stmt.close(); } catch (SQLException e) {}
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
    }
    

    
    private int mapCategoriaToId(CategoriaBien categoria) {
        switch (categoria) {
            case ROPA: return 1;
            case MUEBLES: return 2;
            case ALIMENTOS: return 3;
            case ELECTRODOMESTICOS: return 4;
            case HERRAMIENTAS: return 5;
            case JUGUETES: return 6;
            case LIBROS: return 7;
            case MEDICAMENTOS: return 8;
            case HIGIENE: return 9;
            case OTROS: return 10;
            default: throw new IllegalArgumentException("categoria no mapeada: " + categoria);
        }
    }
    
    private CategoriaBien mapIdToCategoria(int id) {
        switch (id) {
            case 1: return CategoriaBien.ROPA;
            case 2: return CategoriaBien.MUEBLES;
            case 3: return CategoriaBien.ALIMENTOS;
            case 4: return CategoriaBien.ELECTRODOMESTICOS;
            case 5: return CategoriaBien.HERRAMIENTAS;
            case 6: return CategoriaBien.JUGUETES;
            case 7: return CategoriaBien.LIBROS;
            case 8: return CategoriaBien.MEDICAMENTOS;
            case 9: return CategoriaBien.HIGIENE;
            case 10: return CategoriaBien.OTROS;
            default: throw new IllegalArgumentException("id categoria desconocido: " + id);
        }
    }
}