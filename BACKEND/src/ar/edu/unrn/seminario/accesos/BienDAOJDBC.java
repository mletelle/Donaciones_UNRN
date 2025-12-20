package ar.edu.unrn.seminario.accesos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import ar.edu.unrn.seminario.exception.PersistenceException;
import ar.edu.unrn.seminario.modelo.Bien;
import ar.edu.unrn.seminario.modelo.EstadoBien;
import ar.edu.unrn.seminario.modelo.CategoriaBien;

public class BienDAOJDBC implements BienDao {

    private static final String SQL_INSERT = "INSERT INTO bienes(id_pedido_donacion, categoria, cantidad, descripcion, fecha_vencimiento, estado_inventario) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE = "UPDATE bienes SET cantidad = ?, descripcion = ?, estado_inventario = ?, fecha_vencimiento = ? WHERE id = ?";
    private static final String SQL_SELECT_BY_ID = "SELECT * FROM bienes WHERE id = ?";
    private static final String SQL_SELECT_BY_ESTADO = "SELECT * FROM bienes WHERE estado_inventario = ? AND cantidad > 0";
    private static final String SQL_UPDATE_ESTADO_PEDIDO = "UPDATE bienes SET estado_inventario = ? WHERE id_pedido_donacion = ?";
    private static final String SQL_SELECT_BY_ORDEN = "SELECT * FROM bienes WHERE id_orden_entrega = ?";
    private static final String SQL_ASOCIAR_ORDEN = "UPDATE bienes SET id_orden_entrega = ?, estado_inventario = ? WHERE id = ?";
    private static final String SQL_SELECT_ID_PEDIDO = "SELECT id_pedido_donacion FROM bienes WHERE id = ?";

    @Override
    public void createBatch(List<Bien> bienes, int idPedido) throws PersistenceException {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(SQL_INSERT)) {
            
            conn.setAutoCommit(false);
            
            try {
                for (Bien bien : bienes) {
                    setParametrosBien(statement, bien, idPedido);
                    statement.addBatch();
                }
                statement.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new PersistenceException("error al crear lote de bienes: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateEstadoPorPedido(int idPedido, String nuevoEstado) throws PersistenceException {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(SQL_UPDATE_ESTADO_PEDIDO)) {
            statement.setString(1, nuevoEstado);
            statement.setInt(2, idPedido);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException("error al actualizar estado de bienes del pedido " + idPedido + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<Bien> findByEstadoInventario(String estado) throws PersistenceException {
        List<Bien> bienes = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(SQL_SELECT_BY_ESTADO)) {
            statement.setString(1, estado);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    bienes.add(mapearBien(rs));
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("error al buscar bienes por estado: " + estado, e);
        }
        return bienes;
    }

    @Override
    public Bien findById(int id) throws PersistenceException {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(SQL_SELECT_BY_ID)) {
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapearBien(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new PersistenceException("error al buscar bien por id " + id + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Bien bien) throws PersistenceException {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(SQL_UPDATE)) {
            statement.setInt(1, bien.obtenerCantidad());
            statement.setString(2, bien.getDescripcion());
            statement.setString(3, bien.getEstadoInventario().name());
            
            if (bien.getFecVec() != null) {
                statement.setDate(4, new java.sql.Date(bien.getFecVec().getTime()));
            } else {
                statement.setNull(4, Types.DATE);
            }
            
            statement.setInt(5, bien.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException("error al actualizar bien ID " + bien.getId() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void asociarAOrdenEntrega(int idBien, int idOrdenEntrega, String nuevoEstado) throws PersistenceException {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(SQL_ASOCIAR_ORDEN)) {
            statement.setInt(1, idOrdenEntrega);
            statement.setString(2, nuevoEstado);
            statement.setInt(3, idBien);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException("error al asociar bien " + idBien + " a orden " + idOrdenEntrega + ": " + e.getMessage(), e);
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
        List<Bien> bienes = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(SQL_SELECT_BY_ORDEN)) {
            statement.setInt(1, idOrdenEntrega);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    bienes.add(mapearBien(rs));
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("error al buscar bienes de orden " + idOrdenEntrega + ": " + e.getMessage(), e);
        }
        return bienes;
    }
    
    @Override
    public int create(Bien bien, int idPedidoOriginal) throws PersistenceException {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            setParametrosBien(stmt, bien, idPedidoOriginal);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("no se pudo crear el bien, no se modificaron filas");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("no se obtuvo el id del bien creado");
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("error al crear bien: " + e.getMessage(), e);
        }
    }
    
    @Override
    public int fraccionarYCrear(int idBienOriginal, int cantidadSolicitada) throws PersistenceException {
        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement stmtUpdate = conn.prepareStatement("UPDATE bienes SET cantidad = cantidad - ? WHERE id = ?");
                 PreparedStatement stmtInsert = conn.prepareStatement(
                     "INSERT INTO bienes(id_pedido_donacion, categoria, cantidad, descripcion, fecha_vencimiento, estado_inventario) " +
                     "SELECT id_pedido_donacion, categoria, ?, descripcion, fecha_vencimiento, ? FROM bienes WHERE id = ?",
                     Statement.RETURN_GENERATED_KEYS)) {
                
                stmtUpdate.setInt(1, cantidadSolicitada);
                stmtUpdate.setInt(2, idBienOriginal);
                stmtUpdate.executeUpdate();
                
                stmtInsert.setInt(1, cantidadSolicitada);
                stmtInsert.setString(2, EstadoBien.EN_STOCK.name());
                stmtInsert.setInt(3, idBienOriginal);
                stmtInsert.executeUpdate();
                
                try (ResultSet generatedKeys = stmtInsert.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int idGenerado = generatedKeys.getInt(1);
                        conn.commit();
                        return idGenerado;
                    } else {
                        throw new SQLException("no se pudo obtener el id del bien fraccionado");
                    }
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new PersistenceException("error al fraccionar bien " + idBienOriginal + ": " + e.getMessage(), e);
        }
    }
    
    @Override
    public int obtenerIdPedidoDeBien(int idBien) throws PersistenceException {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_ID_PEDIDO)) {
            stmt.setInt(1, idBien);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new PersistenceException("error al obtener pedido del bien " + idBien + ": " + e.getMessage(), e);
        }
    }
    
    private void setParametrosBien(PreparedStatement stmt, Bien bien, int idPedido) throws SQLException {
        stmt.setInt(1, idPedido);
        stmt.setInt(2, mapCategoriaToId(bien.obtenerCategoria()));
        stmt.setInt(3, bien.obtenerCantidad());
        
        if (bien.getDescripcion() != null) {
            stmt.setString(4, bien.getDescripcion());
        } else {
            stmt.setNull(4, Types.VARCHAR);
        }
        
        if (bien.getFecVec() != null) {
            stmt.setDate(5, new java.sql.Date(bien.getFecVec().getTime()));
        } else {
            stmt.setNull(5, Types.DATE);
        }
        
        String estado = bien.getEstadoInventario() != null ? bien.getEstadoInventario().name() : "PENDIENTE";
        stmt.setString(6, estado);
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