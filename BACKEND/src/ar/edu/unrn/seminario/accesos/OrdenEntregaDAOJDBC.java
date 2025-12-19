package ar.edu.unrn.seminario.accesos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ar.edu.unrn.seminario.exception.PersistenceException;
import ar.edu.unrn.seminario.modelo.OrdenEntrega;
import ar.edu.unrn.seminario.modelo.Usuario;
import ar.edu.unrn.seminario.modelo.Vehiculo;
import ar.edu.unrn.seminario.modelo.EstadoEntrega;
import ar.edu.unrn.seminario.modelo.EstadoBien;
import ar.edu.unrn.seminario.modelo.Bien;

public class OrdenEntregaDAOJDBC implements OrdenEntregaDao {
    // SQL
    private static final String SQL_INSERT_ORDEN = "INSERT INTO ordenes_entrega (fecha_generacion, estado, usuario_beneficiario, usuario_voluntario) VALUES (?, ?, ?, ?)";
    private static final String SQL_BIEN_SELECT_FOR_UPDATE = "SELECT * FROM bienes WHERE id = ? FOR UPDATE";
    
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
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);
            
            stmt = conn.prepareStatement(SQL_INSERT_ORDEN, Statement.RETURN_GENERATED_KEYS);
            
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
                throw new SQLException("No se pudo crear la orden de entrega.");
            }

            generatedKeys = stmt.getGeneratedKeys();
            int id;
            if (generatedKeys.next()) {
                id = generatedKeys.getInt(1);
                orden.setId(id);
            } else {
                throw new SQLException("No se obtuvo el ID de la orden creada.");
            }
            
            conn.commit();
            return id;
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
            throw new PersistenceException("Error al crear orden de entrega: " + e.getMessage(), e);
        } finally {
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) {}
            if (stmt != null) try { stmt.close(); } catch (SQLException e) {}
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
    }

    @Override
    public void crearOrdenConBienes(OrdenEntrega orden, Map<Integer, Integer> bienesYCantidades) throws PersistenceException {
        Connection conn = null;
        PreparedStatement stmtOrden = null;
        ResultSet generatedKeys = null;
        PreparedStatement stmtSelectBien = null;
        PreparedStatement stmtUpdateAsignar = null;
        PreparedStatement stmtUpdateRestar = null;
        PreparedStatement stmtInsertFraccion = null;
        ResultSet rsBien = null;

        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);


            stmtOrden = conn.prepareStatement(SQL_INSERT_ORDEN, Statement.RETURN_GENERATED_KEYS);
            stmtOrden.setTimestamp(1, new Timestamp(orden.getFechaGeneracion().getTime()));
            stmtOrden.setInt(2, mapEstadoToId(orden.getEstado())); 
            stmtOrden.setString(3, orden.getBeneficiario().getUsuario());
            
            if (orden.getVoluntario() != null) {
                stmtOrden.setString(4, orden.getVoluntario().getUsuario());
            } else {
                stmtOrden.setNull(4, java.sql.Types.VARCHAR);
            }

            int affectedRows = stmtOrden.executeUpdate();
            if (affectedRows == 0) throw new SQLException("No se pudo crear la orden.");

            generatedKeys = stmtOrden.getGeneratedKeys();
            int idOrden;
            if (generatedKeys.next()) {
                idOrden = generatedKeys.getInt(1);
                orden.setId(idOrden);
            } else {
                throw new SQLException("No se obtuvo ID de la orden.");
            }

            stmtSelectBien = conn.prepareStatement(SQL_BIEN_SELECT_FOR_UPDATE);
            stmtUpdateAsignar = conn.prepareStatement(SQL_BIEN_UPDATE_ASIGNAR);
            stmtUpdateRestar = conn.prepareStatement(SQL_BIEN_UPDATE_RESTAR);
            stmtInsertFraccion = conn.prepareStatement(SQL_BIEN_INSERT_FRACCION);

            for (Map.Entry<Integer, Integer> entry : bienesYCantidades.entrySet()) {
                int idBienOriginal = entry.getKey();
                int cantidadSolicitada = entry.getValue();

                stmtSelectBien.setInt(1, idBienOriginal);
                rsBien = stmtSelectBien.executeQuery();
                
                if (!rsBien.next()) throw new SQLException("El bien ID " + idBienOriginal + " no existe.");
                
                int cantidadDisponible = rsBien.getInt("cantidad");
                
                if (cantidadDisponible < cantidadSolicitada) {
                    throw new SQLException("Stock insuficiente para bien ID " + idBienOriginal + ". Disponible: " + cantidadDisponible);
                }

                if (cantidadDisponible == cantidadSolicitada) {
                    stmtUpdateAsignar.setInt(1, idOrden);
                    stmtUpdateAsignar.setString(2, EstadoBien.ENTREGADO.name());
                    stmtUpdateAsignar.setInt(3, idBienOriginal);
                    stmtUpdateAsignar.executeUpdate();
                } else {
                    stmtUpdateRestar.setInt(1, cantidadSolicitada);
                    stmtUpdateRestar.setInt(2, idBienOriginal);
                    stmtUpdateRestar.executeUpdate();
                    int idPedido = rsBien.getInt("id_pedido_donacion");
                    int categoriaInt = rsBien.getInt("categoria"); 
                    String descripcion = rsBien.getString("descripcion");
                    java.sql.Date fechaVenc = rsBien.getDate("fecha_vencimiento");

                    stmtInsertFraccion.setInt(1, idPedido);
                    stmtInsertFraccion.setInt(2, categoriaInt);
                    stmtInsertFraccion.setInt(3, cantidadSolicitada);
                    stmtInsertFraccion.setString(4, descripcion);
                    stmtInsertFraccion.setDate(5, fechaVenc);
                    stmtInsertFraccion.setString(6, EstadoBien.ENTREGADO.name());
                    stmtInsertFraccion.setInt(7, idOrden);
                    
                    stmtInsertFraccion.executeUpdate();
                }
                rsBien.close();
            }

            conn.commit(); 

        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
            throw new PersistenceException("Error transaccional al crear orden con bienes: " + e.getMessage(), e);
        } finally {
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) {}
            if (stmtOrden != null) try { stmtOrden.close(); } catch (SQLException e) {}
            if (stmtSelectBien != null) try { stmtSelectBien.close(); } catch (SQLException e) {}
            if (stmtUpdateAsignar != null) try { stmtUpdateAsignar.close(); } catch (SQLException e) {}
            if (stmtUpdateRestar != null) try { stmtUpdateRestar.close(); } catch (SQLException e) {}
            if (stmtInsertFraccion != null) try { stmtInsertFraccion.close(); } catch (SQLException e) {}
            if (rsBien != null) try { rsBien.close(); } catch (SQLException e) {}
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
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
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
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

	@Override
	public void crearOrdenConBienes(OrdenEntrega orden, List<Bien> bienesNuevos, List<Bien> bienesOriginales)
			throws PersistenceException {
	}
}