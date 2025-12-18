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
import ar.edu.unrn.seminario.modelo.Bien;

public class OrdenEntregaDAOJDBC implements OrdenEntregaDao {

    private UsuarioDao usuarioDao = new UsuarioDAOJDBC();
    private VehiculoDao vehiculoDao = new VehiculoDAOJDBC();
    private BienDao bienDao = new BienDAOJDBC();
    
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
    public int crearOrdenConBienes(OrdenEntrega orden, java.util.Map<Integer, Integer> bienesYCantidades) throws PersistenceException {
        Connection conn = null;
        PreparedStatement stmtOrden = null;
        PreparedStatement stmtFraccionar = null;
        PreparedStatement stmtInsertBien = null;
        PreparedStatement stmtAsociar = null;
        ResultSet generatedKeys = null;
        ResultSet generatedKeysBien = null;
        
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);
            
            stmtOrden = conn.prepareStatement(
                "INSERT INTO ordenes_entrega (fecha_generacion, estado, usuario_beneficiario, usuario_voluntario) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
            
            stmtOrden.setTimestamp(1, new Timestamp(orden.getFechaGeneracion().getTime()));
            stmtOrden.setInt(2, mapEstadoToId(orden.getEstado()));
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

            generatedKeys = stmtOrden.getGeneratedKeys();
            int idOrden;
            if (generatedKeys.next()) {
                idOrden = generatedKeys.getInt(1);
                orden.setId(idOrden);
            } else {
                throw new SQLException("no se pudo obtener el id de la orden");
            }
            
            stmtFraccionar = conn.prepareStatement("UPDATE bienes SET cantidad = cantidad - ? WHERE id = ?");
            stmtInsertBien = conn.prepareStatement(
                "INSERT INTO bienes(id_pedido_donacion, categoria, cantidad, tipo, descripcion, fecha_vencimiento, estado_inventario, id_orden_entrega) "
                + "SELECT id_pedido_donacion, categoria, ?, tipo, descripcion, fecha_vencimiento, 'ENTREGADO', ? FROM bienes WHERE id = ?",
                Statement.RETURN_GENERATED_KEYS);
            stmtAsociar = conn.prepareStatement("UPDATE bienes SET id_orden_entrega = ?, estado_inventario = 'ENTREGADO' WHERE id = ?");
            
            for (java.util.Map.Entry<Integer, Integer> entry : bienesYCantidades.entrySet()) {
                int idBienOriginal = entry.getKey();
                int cantidadSolicitada = entry.getValue();
                
                PreparedStatement stmtVerificar = conn.prepareStatement("SELECT cantidad FROM bienes WHERE id = ?");
                stmtVerificar.setInt(1, idBienOriginal);
                ResultSet rs = stmtVerificar.executeQuery();
                
                if (rs.next()) {
                    int cantidadDisponible = rs.getInt("cantidad");
                    
                    if (cantidadSolicitada == cantidadDisponible) {
                        stmtAsociar.setInt(1, idOrden);
                        stmtAsociar.setInt(2, idBienOriginal);
                        stmtAsociar.executeUpdate();
                    } else {
                        stmtFraccionar.setInt(1, cantidadSolicitada);
                        stmtFraccionar.setInt(2, idBienOriginal);
                        stmtFraccionar.executeUpdate();
                        
                        stmtInsertBien.setInt(1, cantidadSolicitada);
                        stmtInsertBien.setInt(2, idOrden);
                        stmtInsertBien.setInt(3, idBienOriginal);
                        stmtInsertBien.executeUpdate();
                    }
                }
                rs.close();
                stmtVerificar.close();
            }
            
            conn.commit();
            return idOrden;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new PersistenceException("Error al crear orden con bienes: " + e.getMessage(), e);
        } finally {
            if (generatedKeysBien != null) try { generatedKeysBien.close(); } catch (SQLException e) {}
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) {}
            if (stmtOrden != null) try { stmtOrden.close(); } catch (SQLException e) {}
            if (stmtFraccionar != null) try { stmtFraccionar.close(); } catch (SQLException e) {}
            if (stmtInsertBien != null) try { stmtInsertBien.close(); } catch (SQLException e) {}
            if (stmtAsociar != null) try { stmtAsociar.close(); } catch (SQLException e) {}
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