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
import ar.edu.unrn.seminario.modelo.EstadoPedido;
import ar.edu.unrn.seminario.modelo.OrdenRetiro;
import ar.edu.unrn.seminario.modelo.PedidosDonacion;
import ar.edu.unrn.seminario.modelo.Usuario;
import ar.edu.unrn.seminario.modelo.TipoVehiculo;
import ar.edu.unrn.seminario.modelo.TipoBien;
import ar.edu.unrn.seminario.modelo.CategoriaBien;

public class PedidosDonacionDAOJDBC implements PedidosDonacionDao {
    
    private UsuarioDao usuarioDao = new UsuarioDAOJDBC();

    @Override
    public int create(PedidosDonacion pedido) throws PersistenceException {
        Connection conn = null;
        PreparedStatement stmtPedido = null;
        PreparedStatement stmtBienes = null;
        ResultSet generatedKeys = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);
            
            stmtPedido = conn.prepareStatement(
                    "INSERT INTO pedidos_donacion(fecha, tipo_vehiculo, usuario_donante, estado, id_orden_retiro) "
                    + "VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            
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
                throw new SQLException("no se pudo crear pedido");
            }

            generatedKeys = stmtPedido.getGeneratedKeys();
            int idGenerado;
            if (generatedKeys.next()) {
                idGenerado = generatedKeys.getInt(1);
            } else {
                throw new SQLException("no se pudo crear pedido");
            }
            
            if (pedido.obtenerBienes() != null && !pedido.obtenerBienes().isEmpty()) {
                stmtBienes = conn.prepareStatement(
                    "INSERT INTO bienes(id_pedido_donacion, categoria, cantidad, tipo, descripcion, fecha_vencimiento, estado_inventario) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)");
                
                for (Bien bien : pedido.obtenerBienes()) {
                    stmtBienes.setInt(1, idGenerado);
                    stmtBienes.setInt(2, mapCategoriaToId(bien.obtenerCategoria()));
                    stmtBienes.setInt(3, bien.obtenerCantidad());
                    stmtBienes.setInt(4, mapTipoToId(bien.obtenerTipo()));
                    
                    if (bien.getDescripcion() != null) stmtBienes.setString(5, bien.getDescripcion());
                    else stmtBienes.setNull(5, java.sql.Types.VARCHAR);
                    
                    if (bien.getFecVec() != null) stmtBienes.setDate(6, new java.sql.Date(bien.getFecVec().getTime()));
                    else stmtBienes.setNull(6, java.sql.Types.DATE);
                    
                    String estado = bien.getEstadoInventario() != null ? bien.getEstadoInventario().name() : "PENDIENTE";
                    stmtBienes.setString(7, estado);
                    
                    stmtBienes.addBatch();
                }
                stmtBienes.executeBatch();
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
            throw new PersistenceException("error al crear pedido: " + e.getMessage(), e);
        } finally {
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) {}
            if (stmtPedido != null) try { stmtPedido.close(); } catch (SQLException e) {}
            if (stmtBienes != null) try { stmtBienes.close(); } catch (SQLException e) {}
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
    }

    @Override
    public void update(PedidosDonacion pedido) throws PersistenceException {
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);
            
            statement = conn.prepareStatement(
                    "UPDATE pedidos_donacion SET estado = ?, id_orden_retiro = ? WHERE id = ?");
            
            statement.setString(1, pedido.obtenerEstado());
            
            if (pedido.obtenerOrden() != null) {
                statement.setInt(2, pedido.obtenerOrden().getId());
            } else {
                statement.setNull(2, java.sql.Types.INTEGER);
            }
            
            statement.setInt(3, pedido.getId());
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
            throw new PersistenceException("error al actualizar pedido: " + e.getMessage(), e);
        } finally {
            if (statement != null) try { statement.close(); } catch (SQLException e) {}
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
    }
    
    @Override
    public void actualizarEstadoYOrdenLote(List<Integer> idsPedidos, String nuevoEstado, int idOrden) throws PersistenceException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);
            
            stmt = conn.prepareStatement("UPDATE pedidos_donacion SET estado = ?, id_orden_retiro = ? WHERE id = ?");
            for (Integer id : idsPedidos) {
                stmt.setString(1, nuevoEstado);
                stmt.setInt(2, idOrden);
                stmt.setInt(3, id);
                stmt.addBatch();
            }
            stmt.executeBatch();
            
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new PersistenceException("error al actualizar lote de pedidos: " + e.getMessage(), e);
        } finally {
            if (stmt != null) try { stmt.close(); } catch (SQLException e) {}
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
    }

    @Override
    public PedidosDonacion findByIdAndOrden(int idPedido, int idOrdenRetiro  ) throws PersistenceException {
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            conn = ConnectionManager.getConnection();
            statement = conn.prepareStatement(
                "SELECT id, fecha, tipo_vehiculo, usuario_donante, estado, id_orden_retiro " +
                "FROM pedidos_donacion WHERE id = ? AND id_orden_retiro = ?");
            statement.setInt(1, idPedido);
            statement.setInt(2, idOrdenRetiro);
            rs = statement.executeQuery();

            if (rs.next()) {
                return mapearResultadoPedido(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new PersistenceException("error al buscar pedido por id y orden: " + e.getMessage(), e);
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) {}
            if (statement != null) try { statement.close(); } catch (SQLException e) {}
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
    }

    @Override
    public List<PedidosDonacion> findAllPendientes() throws PersistenceException {
        Connection conn = null;
        List<PedidosDonacion> pedidos = new ArrayList<PedidosDonacion>();
        Statement statement = null;
        ResultSet rs = null;
        try {
            conn = ConnectionManager.getConnection();
            statement = conn.createStatement();
            rs = statement.executeQuery(
                    "SELECT id, fecha, tipo_vehiculo, usuario_donante, estado, id_orden_retiro "
                    + "FROM pedidos_donacion WHERE estado = 'PENDIENTE' AND id_orden_retiro IS NULL");
            
            while (rs.next()) {
                try {
                    PedidosDonacion pedido = mapearResultadoPedido(rs);
                    pedidos.add(pedido);
                } catch (Exception e) {
                    System.err.println("error al crear registro: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("error al buscar pedidos pendientes: " + e.getMessage(), e);
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) {}
            if (statement != null) try { statement.close(); } catch (SQLException e) {}
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
        return pedidos;
    }

    @Override
    public List<PedidosDonacion> findAll() throws PersistenceException {
        Connection conn = null;
        List<PedidosDonacion> pedidos = new ArrayList<PedidosDonacion>();
        Statement statement = null;
        ResultSet rs = null;
        try {
            conn = ConnectionManager.getConnection();
            statement = conn.createStatement();
            rs = statement.executeQuery(
                    "SELECT id, fecha, tipo_vehiculo, usuario_donante, estado, id_orden_retiro "
                    + "FROM pedidos_donacion ORDER BY fecha DESC");
            
            while (rs.next()) {
                try {
                    PedidosDonacion pedido = mapearResultadoPedido(rs);
                    pedidos.add(pedido);
                } catch (Exception e) {
                    System.err.println("error al crear registro: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("error al buscar todos los pedidos: " + e.getMessage(), e);
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) {}
            if (statement != null) try { statement.close(); } catch (SQLException e) {}
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
        return pedidos;
    }

    @Override
    public List<PedidosDonacion> findByOrden(int idOrden) throws PersistenceException {
        Connection conn = null;
        List<PedidosDonacion> pedidos = new ArrayList<PedidosDonacion>();
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            conn = ConnectionManager.getConnection();
            statement = conn.prepareStatement(
                    "SELECT id, fecha, tipo_vehiculo, usuario_donante, estado, id_orden_retiro "
                    + "FROM pedidos_donacion WHERE id_orden_retiro = ?");
            statement.setInt(1, idOrden);
            rs = statement.executeQuery();
            
            while (rs.next()) {
                try {
                    PedidosDonacion pedido = mapearResultadoPedido(rs);
                    pedidos.add(pedido);
                } catch (Exception e) {
                    System.err.println("error al crear pedidosdonacion: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("error al buscar pedidos por orden: " + e.getMessage(), e);
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) {}
            if (statement != null) try { statement.close(); } catch (SQLException e) {}
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
        return pedidos;
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
            
            // Seteamos el estado
            String estadoStr = rs.getString("estado");
            EstadoPedido estado = EstadoPedido.fromString(estadoStr.toUpperCase());
            pedido.setEstado(estado);
            
            // Cargar id_orden_retiro si existe
            Integer idOrdenRetiro = rs.getInt("id_orden_retiro");
            if (!rs.wasNull() && idOrdenRetiro != null) {
                OrdenRetiro ordenTemp = new OrdenRetiro(idOrdenRetiro);
                pedido.asignarOrden(ordenTemp);
            }
            
            return pedido;
        } catch (Exception e) {
            throw new SQLException("Error al mapear PedidosDonacion: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<PedidosDonacion> findByIds(List<Integer> ids) throws PersistenceException {
        Connection conn = null;
        List<PedidosDonacion> pedidos = new ArrayList<>();
        if (ids == null || ids.isEmpty()) {
            return pedidos;
        }

        PreparedStatement statement = null;
        ResultSet rs = null;
        
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            placeholders.append("?");
            if (i < ids.size() - 1) {
                placeholders.append(", ");
            }
        }

        String sql = "SELECT id, fecha, tipo_vehiculo, usuario_donante, estado, id_orden_retiro "
                   + "FROM pedidos_donacion WHERE id IN (" + placeholders.toString() + ")";

        try {
            conn = ConnectionManager.getConnection();
            statement = conn.prepareStatement(sql);
            for (int i = 0; i < ids.size(); i++) {
                statement.setInt(i + 1, ids.get(i));
            }

            rs = statement.executeQuery();
            while (rs.next()) {
                try {
                    PedidosDonacion pedido = mapearResultadoPedido(rs);
                    pedidos.add(pedido);
                } catch (Exception e) {
                    System.err.println("error al mapear pedidosdonacion: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("error al buscar pedidos por ids: " + e.getMessage(), e);
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) {}
            if (statement != null) try { statement.close(); } catch (SQLException e) {}
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
        
        return pedidos;
    }

    @Override
    public PedidosDonacion findById(int idPedido) throws PersistenceException {
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            conn = ConnectionManager.getConnection();
            statement = conn.prepareStatement(
                "SELECT id, fecha, tipo_vehiculo, usuario_donante, estado, id_orden_retiro " +
                "FROM pedidos_donacion WHERE id = ?");
            statement.setInt(1, idPedido);
            rs = statement.executeQuery();

            if (rs.next()) {
                return mapearResultadoPedido(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new PersistenceException("error al buscar pedido por id: " + e.getMessage(), e);
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) {}
            if (statement != null) try { statement.close(); } catch (SQLException e) {}
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
    }
    
    // ---------------------------------------------------------
    // CONVERTIDORES (reutilizados de BienDAOJDBC)
    // ---------------------------------------------------------
    
    private int mapTipoToId(TipoBien tipo) {
        switch (tipo) {
            case ALIMENTO: return 1;
            case ROPA: return 2;
            case MOBILIARIO: return 3;
            case HIGIENE: return 4;
            default: throw new IllegalArgumentException("tipo no mapeado: " + tipo);
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
}