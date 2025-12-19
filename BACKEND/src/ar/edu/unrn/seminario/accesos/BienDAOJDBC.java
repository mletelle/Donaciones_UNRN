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
import ar.edu.unrn.seminario.modelo.CategoriaBien;
import ar.edu.unrn.seminario.modelo.EstadoBien;

public class BienDAOJDBC implements BienDao {

    private static final String SQL_INSERT = "INSERT INTO bienes(id_pedido_donacion, categoria, cantidad, descripcion, fecha_vencimiento, estado_inventario) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE = "UPDATE bienes SET cantidad = ?, descripcion = ?, estado_inventario = ?, fecha_vencimiento = ? WHERE id = ?";
    private static final String SQL_SELECT_BY_ID = "SELECT * FROM bienes WHERE id = ?";
    private static final String SQL_SELECT_BY_ESTADO = "SELECT * FROM bienes WHERE estado_inventario = ? AND cantidad > 0";
    private static final String SQL_UPDATE_ESTADO_PEDIDO = "UPDATE bienes SET estado_inventario = ? WHERE id_pedido_donacion = ?";

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
            throw new PersistenceException("Error al crear lote de bienes: " + e.getMessage(), e);
        }
    }

    @Override
    public int create(Bien bien, int idPedidoOriginal) throws PersistenceException {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            setParametrosBien(stmt, bien, idPedidoOriginal);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("No se pudo crear el bien, no se modificaron filas.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("No se obtuvo el ID del bien creado.");
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("Error al crear un solo bien: " + e.getMessage(), e);
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
            throw new PersistenceException("Error al actualizar bien ID " + bien.getId() + ": " + e.getMessage(), e);
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
            throw new PersistenceException("Error al actualizar estado de bienes del pedido " + idPedido, e);
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
            throw new PersistenceException("Error al buscar bienes por estado: " + estado, e);
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
        } catch (SQLException e) {
            throw new PersistenceException("Error al buscar bien por ID: " + id, e);
        }
        return null;
    }

    @Override
    public void asociarAOrdenEntrega(int idBien, int idOrdenEntrega, String nuevoEstado) throws PersistenceException {
        String SQL_UPDATE_LOCAL = "UPDATE bienes SET id_orden_entrega = ?, estado_inventario = ? WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(SQL_UPDATE_LOCAL)) {

            statement.setInt(1, idOrdenEntrega);
            statement.setString(2, nuevoEstado);
            statement.setInt(3, idBien);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new PersistenceException("Error al asociar bien " + idBien + " a orden " + idOrdenEntrega, e);
        }
    }

    @Override
    public List<Bien> findByOrdenEntrega(int idOrdenEntrega) throws PersistenceException {
        List<Bien> bienes = new ArrayList<>();
        String SQL_SELECT_LOCAL = "SELECT * FROM bienes WHERE id_orden_entrega = ?";
        
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(SQL_SELECT_LOCAL)) {
            
            statement.setInt(1, idOrdenEntrega);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    bienes.add(mapearBien(rs));
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("Error al buscar bienes por orden de entrega: " + idOrdenEntrega, e);
        }
        return bienes;
    }

    @Override
    public int fraccionarYCrear(int idBienOriginal, int cantidadSolicitada) throws PersistenceException {
        String SQL_UPDATE_LOCAL = "UPDATE bienes SET cantidad = cantidad - ? WHERE id = ?";
        String SQL_SELECT_LOCAL = "SELECT id_pedido_donacion, categoria, ?, descripcion, fecha_vencimiento, ? FROM bienes WHERE id = ?";
        String SQL_INSERT_LOCAL = "INSERT INTO bienes(id_pedido_donacion, categoria, cantidad, descripcion, fecha_vencimiento, estado_inventario) " + SQL_SELECT_LOCAL                           ;
        
        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false); 

            try (PreparedStatement stmtUpdate = conn.prepareStatement(SQL_UPDATE_LOCAL);
                 PreparedStatement stmtInsert = conn.prepareStatement(SQL_INSERT_LOCAL, Statement.RETURN_GENERATED_KEYS)) {

                
                stmtUpdate.setInt(1, cantidadSolicitada);
                stmtUpdate.setInt(2, idBienOriginal);
                stmtUpdate.executeUpdate();


                stmtInsert.setInt(1, cantidadSolicitada);
                stmtInsert.setString(2, EstadoBien.EN_STOCK.name());
                stmtInsert.setInt(3, idBienOriginal);
                stmtInsert.executeUpdate();

                conn.commit(); // Todo salió bien

                try (ResultSet generatedKeys = stmtInsert.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("No se pudo obtener el ID del bien fraccionado.");
                    }
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new PersistenceException("Error al fraccionar el bien ID: " + idBienOriginal, e);
        }
    }

    @Override
    public int obtenerIdPedidoDeBien(int idBien) throws PersistenceException {
        String SQL_SELECT_LOCAL = "SELECT id_pedido_donacion FROM bienes WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_LOCAL)) {
            
            stmt.setInt(1, idBien);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

            return 0; 
        } catch (SQLException e) {
            throw new PersistenceException("Error al obtener pedido del bien: " + idBien, e);
        }
    }

    private void setParametrosBien(PreparedStatement statement, Bien bien, int idPedido) throws SQLException {
        statement.setInt(1, idPedido);
        statement.setInt(2, mapCategoriaToId(bien.obtenerCategoria()));
        statement.setInt(3, bien.obtenerCantidad());

        if (bien.getDescripcion() != null) {
            statement.setString(4, bien.getDescripcion());
        } else {
            statement.setNull(4, Types.VARCHAR);
        }

        if (bien.getFecVec() != null) {
            statement.setDate(5, new java.sql.Date(bien.getFecVec().getTime()));
        } else {
            statement.setNull(5, Types.DATE);
        }

        String estado = (bien.getEstadoInventario() != null) 
                        ? bien.getEstadoInventario().name() 
                        : EstadoBien.PENDIENTE.name();
        statement.setString(6, estado);
    }

    private Bien mapearBien(ResultSet rs) throws SQLException {
        try {
            CategoriaBien categoria = mapIdToCategoria(rs.getInt("categoria"));
            Bien bien = new Bien(rs.getInt("cantidad"), categoria);
            
            bien.setId(rs.getInt("id"));
            bien.setDescripcion(rs.getString("descripcion"));
            
            String estadoStr = rs.getString("estado_inventario");
            bien.setEstadoInventario(EstadoBien.fromString(estadoStr));

            if (rs.getDate("fecha_vencimiento") != null) {
                bien.setFecVec(new java.util.Date(rs.getDate("fecha_vencimiento").getTime()));
            }
            return bien;
        } catch (Exception e) {
            throw new SQLException("Error mapeando resultado a objeto Bien: " + e.getMessage(), e);
        }
    }

    private int mapCategoriaToId(CategoriaBien categoria) {
        if (categoria == null) return 10; 
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
            default: return 10;
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
            default: return CategoriaBien.OTROS; 
        }
    }
}