package ar.edu.unrn.seminario.accesos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import ar.edu.unrn.seminario.exception.PersistenceException;
import ar.edu.unrn.seminario.modelo.Bien;
import ar.edu.unrn.seminario.modelo.CategoriaBien;
import ar.edu.unrn.seminario.modelo.EstadoPedido;
import ar.edu.unrn.seminario.modelo.OrdenRetiro;
import ar.edu.unrn.seminario.modelo.PedidosDonacion;
import ar.edu.unrn.seminario.modelo.TipoVehiculo;
import ar.edu.unrn.seminario.modelo.Usuario;

public class PedidosDonacionDAOJDBC implements PedidosDonacionDao {
    
    // SQL 
    private static final String SQL_SELECT_BASE = "SELECT id, fecha, tipo_vehiculo, usuario_donante, estado, id_orden_retiro FROM pedidos_donacion";
    
    private static final String SQL_INSERT_PEDIDO = "INSERT INTO pedidos_donacion(fecha, tipo_vehiculo, usuario_donante, estado, id_orden_retiro) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_INSERT_BIENES = "INSERT INTO bienes(id_pedido_donacion, categoria, cantidad, descripcion, fecha_vencimiento, estado_inventario) VALUES (?, ?, ?, ?, ?, ?)";
    
    private static final String SQL_UPDATE = "UPDATE pedidos_donacion SET estado = ?, id_orden_retiro = ? WHERE id = ?";
    
    private static final String SQL_SELECT_BY_ID = SQL_SELECT_BASE + " WHERE id = ?";
    private static final String SQL_SELECT_BY_ID_AND_ORDEN = SQL_SELECT_BASE + " WHERE id = ? AND id_orden_retiro = ?";
    private static final String SQL_SELECT_PENDIENTES = SQL_SELECT_BASE + " WHERE estado = 'PENDIENTE' AND id_orden_retiro IS NULL";
    private static final String SQL_SELECT_ALL = SQL_SELECT_BASE + " ORDER BY fecha DESC";
    private static final String SQL_SELECT_BY_ORDEN = SQL_SELECT_BASE + " WHERE id_orden_retiro = ?";
    private static final String SQL_SELECT_BY_IDS_PREFIX = SQL_SELECT_BASE + " WHERE id IN (";

    private UsuarioDao usuarioDao = new UsuarioDAOJDBC();

    @Override
    public int create(PedidosDonacion pedido) throws PersistenceException {
        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            
            int idGenerado;

            try (PreparedStatement stmtPedido = conn.prepareStatement(SQL_INSERT_PEDIDO, Statement.RETURN_GENERATED_KEYS)) {
                
                stmtPedido.setTimestamp(1, Timestamp.valueOf(pedido.obtenerFecha()));
                stmtPedido.setString(2, pedido.describirTipoVehiculo());
                stmtPedido.setString(3, pedido.getDonante().getUsuario());
                stmtPedido.setString(4, pedido.obtenerEstado());
                
                if (pedido.obtenerOrden() != null) {
                    stmtPedido.setInt(5, pedido.obtenerOrden().getId());
                } else {
                    stmtPedido.setNull(5, java.sql.Types.INTEGER);
                }
                
                int affectedRows = stmtPedido.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("No se pudo crear el pedido, no se afectaron filas.");
                }

                try (ResultSet generatedKeys = stmtPedido.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        idGenerado = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("No se pudo obtener el ID del pedido generado.");
                    }
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

            if (pedido.obtenerBienes() != null && !pedido.obtenerBienes().isEmpty()) {
                try (PreparedStatement stmtBienes = conn.prepareStatement(SQL_INSERT_BIENES)) {
                    for (Bien bien : pedido.obtenerBienes()) {
                        stmtBienes.setInt(1, idGenerado);
                        stmtBienes.setInt(2, mapCategoriaToId(bien.obtenerCategoria()));
                        stmtBienes.setInt(3, bien.obtenerCantidad());
                        
                        if (bien.getDescripcion() != null) {
                            stmtBienes.setString(4, bien.getDescripcion());
                        } else {
                            stmtBienes.setNull(4, java.sql.Types.VARCHAR);
                        }
                        
                        if (bien.getFecVec() != null) {
                            stmtBienes.setDate(5, new java.sql.Date(bien.getFecVec().getTime()));
                        } else {
                            stmtBienes.setNull(5, java.sql.Types.DATE);
                        }
                        
                        String estado = (bien.getEstadoInventario() != null) ? bien.getEstadoInventario().name() : "PENDIENTE";
                        stmtBienes.setString(6, estado);
                        
                        stmtBienes.addBatch();
                    }
                    stmtBienes.executeBatch();
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                }
            }
            
            conn.commit();
            return idGenerado;

        } catch (SQLException e) {
            throw new PersistenceException("Error al crear pedido: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(PedidosDonacion pedido) throws PersistenceException {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(SQL_UPDATE)) {
            
            statement.setString(1, pedido.obtenerEstado());
            
            if (pedido.obtenerOrden() != null) {
                statement.setInt(2, pedido.obtenerOrden().getId());
            } else {
                statement.setNull(2, java.sql.Types.INTEGER);
            }
            
            statement.setInt(3, pedido.getId());
            statement.executeUpdate();
            
        } catch (SQLException e) {
            throw new PersistenceException("Error al actualizar pedido: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void actualizarEstadoYOrdenLote(List<Integer> idsPedidos, String nuevoEstado, int idOrden) throws PersistenceException {
        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {
                for (Integer id : idsPedidos) {
                    stmt.setString(1, nuevoEstado);
                    stmt.setInt(2, idOrden);
                    stmt.setInt(3, id);
                    stmt.addBatch();
                }
                stmt.executeBatch();
                conn.commit();
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new PersistenceException("Error al actualizar lote de pedidos: " + e.getMessage(), e);
        }
    }

    @Override
    public PedidosDonacion findByIdAndOrden(int idPedido, int idOrdenRetiro) throws PersistenceException {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(SQL_SELECT_BY_ID_AND_ORDEN)) {
            
            statement.setInt(1, idPedido);
            statement.setInt(2, idOrdenRetiro);
            
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapearResultadoPedido(rs);
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("Error al buscar pedido por ID y Orden: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<PedidosDonacion> findAllPendientes() throws PersistenceException {
        List<PedidosDonacion> pedidos = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(SQL_SELECT_PENDIENTES)) {
            
            while (rs.next()) {
                procesarFila(rs, pedidos);
            }
        } catch (SQLException e) {
            throw new PersistenceException("Error al buscar pedidos pendientes: " + e.getMessage(), e);
        }
        return pedidos;
    }

    @Override
    public List<PedidosDonacion> findAll() throws PersistenceException {
        List<PedidosDonacion> pedidos = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(SQL_SELECT_ALL)) {
            
            while (rs.next()) {
                procesarFila(rs, pedidos);
            }
        } catch (SQLException e) {
            throw new PersistenceException("Error al buscar todos los pedidos: " + e.getMessage(), e);
        }
        return pedidos;
    }

    @Override
    public List<PedidosDonacion> findByOrden(int idOrden) throws PersistenceException {
        List<PedidosDonacion> pedidos = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(SQL_SELECT_BY_ORDEN)) {
            
            statement.setInt(1, idOrden);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    procesarFila(rs, pedidos);
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("Error al buscar pedidos por orden: " + e.getMessage(), e);
        }
        return pedidos;
    }

    @Override
    public PedidosDonacion findById(int idPedido) throws PersistenceException {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(SQL_SELECT_BY_ID)) {
            
            statement.setInt(1, idPedido);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapearResultadoPedido(rs);
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("Error al buscar pedido por ID: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<PedidosDonacion> findByIds(List<Integer> ids) throws PersistenceException {
        List<PedidosDonacion> pedidos = new ArrayList<>();
        if (ids == null || ids.isEmpty()) {
            return pedidos;
        }

        StringBuilder sqlBuilder = new StringBuilder(SQL_SELECT_BY_IDS_PREFIX);
        for (int i = 0; i < ids.size(); i++) {
            sqlBuilder.append("?");
            if (i < ids.size() - 1) {
                sqlBuilder.append(", ");
            }
        }
        sqlBuilder.append(")");

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(sqlBuilder.toString())) {
            
            for (int i = 0; i < ids.size(); i++) {
                statement.setInt(i + 1, ids.get(i));
            }

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    procesarFila(rs, pedidos);
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("Error al buscar pedidos por lista de IDs: " + e.getMessage(), e);
        }
        
        return pedidos;
    }

    private void procesarFila(ResultSet rs, List<PedidosDonacion> lista) {
        try {
            PedidosDonacion pedido = mapearResultadoPedido(rs);
            if (pedido != null) {
                lista.add(pedido);
            }
        } catch (Exception e) {
            System.err.println("Error al mapear pedido (ID=" + obtenerIdSeguro(rs) + "): " + e.getMessage());
        }
    }
    
    private int obtenerIdSeguro(ResultSet rs) {
        try { return rs.getInt("id"); } catch (SQLException e) { return -1; }
    }

    private PedidosDonacion mapearResultadoPedido(ResultSet rs) throws SQLException {
        try {
            int id = rs.getInt("id");
            String usuarioDonante = rs.getString("usuario_donante");
            
            Usuario donante = usuarioDao.find(usuarioDonante);
            
            if (donante == null) {
                throw new SQLException("Donante no encontrado: " + usuarioDonante);
            }
            
            LocalDateTime fecha = rs.getTimestamp("fecha").toLocalDateTime();
            String tipoVehiculoStr = rs.getString("tipo_vehiculo");
            
            TipoVehiculo tipoVehiculo = TipoVehiculo.valueOf(tipoVehiculoStr.trim().toUpperCase());
            
            PedidosDonacion pedido = new PedidosDonacion(id, fecha, tipoVehiculo, donante);
            
            String estadoStr = rs.getString("estado");
            EstadoPedido estado = EstadoPedido.fromString(estadoStr.toUpperCase());
            pedido.setEstado(estado);
            
            int idOrdenRetiro = rs.getInt("id_orden_retiro");
            if (!rs.wasNull()) {
                OrdenRetiro ordenTemp = new OrdenRetiro(idOrdenRetiro);
                pedido.asignarOrden(ordenTemp);
            }
            
            return pedido;
        } catch (Exception e) {
            throw new SQLException("Error interno al mapear PedidosDonacion: " + e.getMessage(), e);
        }
    }
    
    private int mapCategoriaToId(CategoriaBien categoria) {
        if (categoria == null) return 10; // Default OTROS
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
            default: throw new IllegalArgumentException("Categoría no mapeada: " + categoria);
        }
    }
}