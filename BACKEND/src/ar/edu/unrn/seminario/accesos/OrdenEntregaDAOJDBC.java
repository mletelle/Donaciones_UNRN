package ar.edu.unrn.seminario.accesos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import ar.edu.unrn.seminario.exception.PersistenceException;
import ar.edu.unrn.seminario.modelo.OrdenEntrega;
import ar.edu.unrn.seminario.modelo.Usuario;
import ar.edu.unrn.seminario.modelo.Vehiculo;
import ar.edu.unrn.seminario.modelo.EstadoEntrega;
import ar.edu.unrn.seminario.modelo.EstadoBien;
import ar.edu.unrn.seminario.modelo.Bien;

public class OrdenEntregaDAOJDBC implements OrdenEntregaDao {

    private static final String SQL_INSERT = "INSERT INTO ordenes_entrega (fecha_generacion, id_beneficiario, id_voluntario, estado) VALUES (?, ?, ?, ?)";
    private static final String SQL_UPDATE = "UPDATE ordenes_entrega SET id_voluntario = ?, estado = ? WHERE id = ?";
    private static final String SQL_SELECT_ALL = "SELECT * FROM ordenes_entrega";
    private static final String SQL_SELECT_BY_ID = "SELECT * FROM ordenes_entrega WHERE id = ?";
    private static final String SQL_SELECT_BY_ESTADO = "SELECT * FROM ordenes_entrega WHERE estado = ?";
    private static final String SQL_SELECT_BY_BENEFICIARIO = "SELECT oe.* FROM ordenes_entrega oe JOIN usuarios u ON oe.id_beneficiario = u.id WHERE u.usuario = ?";
    
    private static final String SQL_BIEN_SELECT_FOR_UPDATE = "SELECT cantidad, descripcion, categoria, fecha_vencimiento, id_pedido_donacion FROM bienes WHERE id = ?";
    private static final String SQL_BIEN_UPDATE_ASIGNAR = "UPDATE bienes SET id_orden_entrega = ?, estado_inventario = ? WHERE id = ?";
    private static final String SQL_BIEN_UPDATE_RESTAR = "UPDATE bienes SET cantidad = cantidad - ? WHERE id = ?";
    private static final String SQL_BIEN_INSERT_FRACCION = "INSERT INTO bienes (id_pedido_donacion, categoria, cantidad, descripcion, fecha_vencimiento, estado_inventario, id_orden_entrega) VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    private final UsuarioDao usuarioDao;
    private final BienDao bienDao;
    private final VehiculoDao vehiculoDao;
    
    public OrdenEntregaDAOJDBC() {
        this.usuarioDao = new UsuarioDAOJDBC();
        this.bienDao = new BienDAOJDBC();
        this.vehiculoDao = new VehiculoDAOJDBC();
    }

    @Override
    public int create(OrdenEntrega orden) throws PersistenceException {
        Connection conn = null;
        String sql = "INSERT INTO ordenes_entrega (fecha_generacion, estado, usuario_beneficiario, usuario_voluntario) VALUES (?, ?, ?, ?)";
        
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);
            
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            stmt.setTimestamp(1, new Timestamp(orden.getFechaGeneracion().getTime()));
            stmt.setInt(2, mapEstadoToId(orden.getEstado()));
            stmt.setString(3, orden.getBeneficiario().getUsuario());
            
            if (orden.getVoluntario() != null) {
                stmt.setString(4, orden.getVoluntario().getUsuario());
            } else {
                stmt.setNull(4, java.sql.Types.VARCHAR);
            }

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("no se pudo crear la orden de entrega, no se afectaron filas");
            }

            generatedKeys = stmt.getGeneratedKeys();
            int id;
            if (generatedKeys.next()) {
                id = generatedKeys.getInt(1);
                orden.setId(id);
            } else {
                throw new SQLException("no se pudo crear la orden de entrega, no se obtuvo el id");
            }
            
            conn.commit();
            return id;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new PersistenceException("Error al crear orden de entrega: " + e.getMessage(), e);
        } finally {
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) {}
            if (stmt != null) try { stmt.close(); } catch (SQLException e) {}
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
    }

    @Override
    public void crearOrdenConBienes(OrdenEntrega orden, List<Bien> bienesNuevos, List<Bien> bienesOriginales) throws PersistenceException {
        String sqlInsertOrden = "INSERT INTO ordenes_entrega (fecha_generacion, estado, usuario_beneficiario, usuario_voluntario) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                int idOrden;
                
                try (PreparedStatement stmtOrden = conn.prepareStatement(sqlInsertOrden, Statement.RETURN_GENERATED_KEYS)) {
                    stmtOrden.setTimestamp(1, new Timestamp(orden.getFechaGeneracion().getTime()));
                    stmtOrden.setString(2, orden.getEstado().name());
                    stmtOrden.setString(3, orden.getBeneficiario().getUsuario());
                    
                    if (orden.getVoluntario() != null) {
                        stmtOrden.setString(4, orden.getVoluntario().getUsuario());
                    } else {
                        stmtOrden.setNull(4, java.sql.Types.VARCHAR);
                    }
                    
                    int affectedRows = stmtOrden.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("no se pudo crear la orden de entrega");
                    }
                    
                    try (ResultSet generatedKeys = stmtOrden.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            idOrden = generatedKeys.getInt(1);
                            orden.setId(idOrden);
                        } else {
                            throw new SQLException("no se pudo obtener el id de la orden");
                        }
                    }
                }
                
                insertarBienesNuevos(conn, bienesNuevos, idOrden);
                actualizarBienesOriginales(conn, bienesOriginales, idOrden);
                
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new PersistenceException("error al crear orden con bienes: " + e.getMessage(), e);
        }
    }
    
    private void insertarBienesNuevos(Connection conn, List<Bien> bienesNuevos, int idOrden) throws SQLException {
        if (bienesNuevos == null || bienesNuevos.isEmpty()) {
            return;
        }
        
        try (PreparedStatement stmt = conn.prepareStatement(SQL_BIEN_INSERT_FRACCION, Statement.RETURN_GENERATED_KEYS)) {
            for (Bien bien : bienesNuevos) {
                stmt.setInt(1, bien.getIdPedidoDonacion());
                stmt.setString(2, bien.getCategoria().name());
                stmt.setInt(3, bien.obtenerCantidad());
                stmt.setString(4, bien.obtenerDescripcion());
                stmt.setDate(5, java.sql.Date.valueOf(bien.getFechaVencimiento()));
                stmt.setString(6, bien.getEstadoInventario().name());
                stmt.setInt(7, idOrden);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }
    
    private void actualizarBienesOriginales(Connection conn, List<Bien> bienesOriginales, int idOrden) throws SQLException {
        if (bienesOriginales == null || bienesOriginales.isEmpty()) {
            return;
        }
        
        String sqlUpdate = "UPDATE bienes SET cantidad = ?, estado_inventario = ?, id_orden_entrega = ? WHERE id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {
            for (Bien bien : bienesOriginales) {
                stmt.setInt(1, bien.obtenerCantidad());
                stmt.setString(2, bien.getEstadoInventario().name());
                
                if (bien.getEstadoInventario() == EstadoBien.ENTREGADO) {
                    stmt.setInt(3, idOrden);
                } else {
                    stmt.setNull(3, java.sql.Types.INTEGER);
                }
                
                stmt.setInt(4, bien.getId());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    @Override
    public List<OrdenEntrega> findByBeneficiario(String usuario) throws PersistenceException {
        Connection conn = null;
        List<OrdenEntrega> ordenes = new ArrayList<>();
        String sql = "SELECT * FROM ordenes_entrega WHERE usuario_beneficiario = ? ORDER BY fecha_generacion DESC";

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionManager.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, usuario);
            rs = stmt.executeQuery();

            while (rs.next()) {
                OrdenEntrega orden = mapearOrden(rs);
                ordenes.add(orden);
            }
        } catch (SQLException e) {
            throw new PersistenceException("Error al buscar órdenes de entrega por beneficiario: " + e.getMessage(), e);
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) {}
            if (stmt != null) try { stmt.close(); } catch (SQLException e) {}
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
        return ordenes;
    }

    @Override
    public List<OrdenEntrega> findAllPendientes() throws PersistenceException {
        Connection conn = null;
        List<OrdenEntrega> ordenes = new ArrayList<>();
        String sql = "SELECT * FROM ordenes_entrega WHERE estado = 1 ORDER BY fecha_generacion ASC";
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConnectionManager.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                ordenes.add(mapearOrden(rs));
            }
        } catch (SQLException e) {
            throw new PersistenceException("Error al buscar órdenes de entrega pendientes: " + e.getMessage(), e);
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) {}
            if (stmt != null) try { stmt.close(); } catch (SQLException e) {}
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
        return ordenes;
    }

    @Override
    public List<OrdenEntrega> findAll() throws PersistenceException {
        Connection conn = null;
        List<OrdenEntrega> ordenes = new ArrayList<>();
        String sql = "SELECT * FROM ordenes_entrega ORDER BY fecha_generacion DESC";
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConnectionManager.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                ordenes.add(mapearOrden(rs));
            }
        } catch (SQLException e) {
            throw new PersistenceException("Error al buscar todas las órdenes de entrega: " + e.getMessage(), e);
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) {}
            if (stmt != null) try { stmt.close(); } catch (SQLException e) {}
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
        return ordenes;
    }

    @Override
    public void update(OrdenEntrega orden) throws PersistenceException {
        Connection conn = null;
        String sql = "UPDATE ordenes_entrega SET estado = ?, usuario_voluntario = ?, fecha_ejecucion = ? WHERE id = ?";
        PreparedStatement stmt = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);
            
            stmt = conn.prepareStatement(sql);
			stmt.setInt(1, mapEstadoToId(orden.getEstado()));
			
			if (orden.getVoluntario() != null) stmt.setString(2, orden.getVoluntario().getUsuario());
			else stmt.setNull(2, java.sql.Types.VARCHAR);
			
			if (orden.getEstado() == EstadoEntrega.COMPLETADO) {
				stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
			} else {
				stmt.setNull(3, java.sql.Types.TIMESTAMP);
			}
            
            stmt.setInt(4, orden.getId());
            
            stmt.executeUpdate();
            
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new PersistenceException("Error al actualizar orden de entrega: " + e.getMessage(), e);
        } finally {
            if (stmt != null) try { stmt.close(); } catch (SQLException e) {}
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
    }
    
    @Override
    public OrdenEntrega findById(int id) throws PersistenceException {
        Connection conn = null;
        String sql = "SELECT * FROM ordenes_entrega WHERE id = ?";
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConnectionManager.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            if (rs.next()) return mapearOrden(rs);
        } catch (SQLException e) {
            throw new PersistenceException("Error al buscar orden de entrega por ID: " + e.getMessage(), e);
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) {}
            if (stmt != null) try { stmt.close(); } catch (SQLException e) {}
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
        return null;
    }

    private OrdenEntrega mapearOrden(ResultSet rs) throws SQLException, PersistenceException {
        String nombreBeneficiario = rs.getString("usuario_beneficiario");
        Usuario beneficiario = usuarioDao.find(nombreBeneficiario);
        
        int idOrden = rs.getInt("id");
        List<Bien> bienesDeLaOrden = bienDao.findByOrdenEntrega(idOrden);
        
        OrdenEntrega orden = new OrdenEntrega(beneficiario, bienesDeLaOrden);
        orden.setId(idOrden);
        orden.setFechaGeneracion(rs.getTimestamp("fecha_generacion"));
        
        int estadoId = rs.getInt("estado");
        EstadoEntrega estado = mapIdToEstado(estadoId);
        orden.forzarEstadoDesdeBD(estado);
        
        String nombreVoluntario = rs.getString("usuario_voluntario");
        if (nombreVoluntario != null) {
            Usuario voluntario = usuarioDao.find(nombreVoluntario);
            orden.setVoluntario(voluntario);
        }

        String patente = rs.getString("patente_vehiculo");
        if (patente != null) {
            Vehiculo vehiculo = vehiculoDao.findByPatente(patente);
            orden.setVehiculo(vehiculo);
        }

        return orden;
    }
    
    private int mapEstadoToId(EstadoEntrega estado) {
        switch (estado) {
            case PENDIENTE: return 1;
            case COMPLETADO: return 3;
            case CANCELADO: return 4;
            default: throw new IllegalArgumentException("estado no mapeado: " + estado);
        }
    }
    
    private EstadoEntrega mapIdToEstado(int id) {
        switch (id) {
            case 1: return EstadoEntrega.PENDIENTE;
            case 3: return EstadoEntrega.COMPLETADO;
            case 4: return EstadoEntrega.CANCELADO;
            default: throw new IllegalArgumentException("id de estado desconocido en bd: " + id);
        }
    }
}