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
import ar.edu.unrn.seminario.modelo.EstadoOrden;
import ar.edu.unrn.seminario.modelo.OrdenRetiro;
import ar.edu.unrn.seminario.modelo.PedidosDonacion;
import ar.edu.unrn.seminario.modelo.Rol;
import ar.edu.unrn.seminario.modelo.Usuario;
import ar.edu.unrn.seminario.modelo.Vehiculo;

public class OrdenRetiroDAOJDBC implements OrdenRetiroDao {

    // SQL 
    private static final String SQL_SELECT_FULL = "SELECT " + 
    		" o.id AS o_id, o.fecha_generacion AS o_fecha, o.estado AS o_estado," +
    		" u.usuario AS u_usuario, u.contrasena AS u_contrasena, u.nombre AS u_nombre, " +
    		" u.correo AS u_correo, u.activo AS u_activo, u.apellido AS u_apellido, " +
    		" u.dni AS u_dni, u.direccion AS u_direccion," +
    		" r.codigo AS r_codigo, r.nombre AS r_nombre, r.activo AS r_activo," +
    		" v.patente AS v_patente, v.tipoVeh AS v_tipoVeh, v.capacidad AS v_capacidad, " +
    		" v.estado AS v_estado_vehiculo " +
    		"FROM " +
    		" ordenes_retiro o " +
    		"LEFT JOIN usuarios u ON o.usuario_voluntario = u.usuario " +
    		"LEFT JOIN roles r ON u.rol = r.codigo " +
    		"LEFT JOIN vehiculos v ON o.patente_vehiculo = v.patente ";

    private static final String SQL_INSERT = "INSERT INTO ordenes_retiro(fecha_generacion, estado, usuario_voluntario, patente_vehiculo) VALUES (?, ?, ?, ?)";
    private static final String SQL_UPDATE = "UPDATE ordenes_retiro SET estado = ? WHERE id = ?";
    private static final String SQL_UPDATE_PEDIDOS_ASOCIAR = "UPDATE pedidos_donacion SET estado = 'EN_EJECUCION', id_orden_retiro = ? WHERE id = ?";
    
    private static final String SQL_SELECT_BY_ID = SQL_SELECT_FULL + " WHERE o.id = ?";
    private static final String SQL_SELECT_BY_ESTADO = SQL_SELECT_FULL + " WHERE UPPER(o.estado) = UPPER(?)";
    private static final String SQL_SELECT_BY_VOLUNTARIO = SQL_SELECT_FULL + " WHERE o.usuario_voluntario = ?";
    private static final String SQL_SELECT_ALL = SQL_SELECT_FULL + " ORDER BY o.fecha_generacion DESC";

    private PedidosDonacionDao pedidoDao = new PedidosDonacionDAOJDBC();

    @Override
    public int create(OrdenRetiro orden) throws PersistenceException {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            
            setParametrosOrden(statement, orden);

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("No se pudo crear la orden, no se afectaron filas.");
            }
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    orden.setId(generatedId);
                    return generatedId;
                } else {
                    throw new SQLException("No se pudo obtener el ID de la orden creada.");
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("Error al crear orden de retiro: " + e.getMessage(), e);
        }
    }

    @Override
    public int crearOrdenConPedidos(OrdenRetiro orden, List<Integer> idsPedidos) throws PersistenceException {
        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false); 
            
            int generatedId;
            
            try (PreparedStatement stmtOrden = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
                setParametrosOrden(stmtOrden, orden);
                
                int affectedRows = stmtOrden.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("No se pudo crear la orden.");
                }
                
                try (ResultSet generatedKeys = stmtOrden.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedId = generatedKeys.getInt(1);
                        orden.setId(generatedId);
                    } else {
                        throw new SQLException("No se pudo obtener el ID de la orden.");
                    }
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

            try (PreparedStatement stmtPedidos = conn.prepareStatement(SQL_UPDATE_PEDIDOS_ASOCIAR)) {
                for (Integer idPedido : idsPedidos) {
                    stmtPedidos.setInt(1, generatedId);
                    stmtPedidos.setInt(2, idPedido);
                    stmtPedidos.addBatch();
                }
                stmtPedidos.executeBatch();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
            conn.commit(); 
            return generatedId;
            
        } catch (SQLException e) {
            throw new PersistenceException("Error al crear orden con pedidos: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(OrdenRetiro orden) throws PersistenceException {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(SQL_UPDATE)) {
            
            statement.setString(1, orden.obtenerNombreEstado());
            statement.setInt(2, orden.getId());
            
            statement.executeUpdate();
            
        } catch (SQLException e) {
            throw new PersistenceException("Error al actualizar orden de retiro: " + e.getMessage(), e);
        }
    }

    @Override
    public OrdenRetiro findById(int idOrden) throws PersistenceException {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(SQL_SELECT_BY_ID)) {
            
            statement.setInt(1, idOrden);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapearResultadoOrden(rs);
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("Error al buscar orden de retiro por ID: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<OrdenRetiro> findByEstado(String estado) throws PersistenceException {
        List<OrdenRetiro> ordenes = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(SQL_SELECT_BY_ESTADO)) {
            
            statement.setString(1, estado);
            
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    procesarFila(rs, ordenes);
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("Error al buscar órdenes de retiro por estado: " + e.getMessage(), e);
        }
        return ordenes;
    }

    @Override
    public List<OrdenRetiro> findAll() throws PersistenceException {
        List<OrdenRetiro> ordenes = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(SQL_SELECT_ALL)) {
            
            while (rs.next()) {
                procesarFila(rs, ordenes);
            }
        } catch (SQLException e) {
            throw new PersistenceException("Error al buscar todas las órdenes de retiro: " + e.getMessage(), e);
        }
        return ordenes;
    }

    @Override
    public List<OrdenRetiro> findByVoluntario(String username) throws PersistenceException {
        List<OrdenRetiro> ordenes = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(SQL_SELECT_BY_VOLUNTARIO)) {
            
            statement.setString(1, username);
            
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    procesarFila(rs, ordenes);
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("Error al buscar órdenes de retiro por voluntario: " + e.getMessage(), e);
        }
        return ordenes;
    }

    private void setParametrosOrden(PreparedStatement statement, OrdenRetiro orden) throws SQLException {
        statement.setTimestamp(1, Timestamp.valueOf(orden.obtenerFechaCreacion()));
        statement.setString(2, orden.obtenerNombreEstado());
        
        Usuario voluntario = orden.obtenerVoluntarioPrincipal();
        if (voluntario != null) {
            statement.setString(3, voluntario.getUsuario());
        } else {
            statement.setNull(3, java.sql.Types.VARCHAR);
        }
        
        Vehiculo vehiculo = orden.obtenerVehiculo();
        if (vehiculo != null) {
            statement.setString(4, vehiculo.getPatente());
        } else {
            statement.setNull(4, java.sql.Types.VARCHAR);
        }
    }


    private void procesarFila(ResultSet rs, List<OrdenRetiro> ordenes) {
        try {
            OrdenRetiro orden = mapearResultadoOrden(rs);
            if (orden != null) {
                ordenes.add(orden);
            }
        } catch (Exception e) {
            try {
                System.err.println("Error al mapear orden de retiro (ID=" + rs.getInt("o_id") + "): " + e.getMessage());
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        }
    }
    
    private OrdenRetiro mapearResultadoOrden(ResultSet rs) throws SQLException {
        try {
            int idOrden = rs.getInt("o_id");
            List<PedidosDonacion> pedidos = pedidoDao.findByOrden(idOrden);
            
            if (pedidos == null || pedidos.isEmpty()) {
                System.err.println("Orden ID=" + idOrden + " no tiene pedidos asociados, se omite.");
                return null;
            }
            
            OrdenRetiro orden = new OrdenRetiro(pedidos, null);
            orden.setId(idOrden);
            
            String estadoStr = rs.getString("o_estado");
            try {
                EstadoOrden estado = EstadoOrden.valueOf(estadoStr.trim().toUpperCase());
                orden.forzarEstadoDesdeBD(estado);
            } catch (IllegalArgumentException e) {
                throw new PersistenceException("Estado inconsistente en BD: " + estadoStr, e);
            }
        
            String usuarioVoluntario = rs.getString("u_usuario");
            if (usuarioVoluntario != null) {
                Rol rol = new Rol(rs.getInt("r_codigo"), rs.getString("r_nombre"));
                rol.setActivo(rs.getBoolean("r_activo"));
                
                Usuario voluntario = new Usuario(
                    usuarioVoluntario,
                    rs.getString("u_contrasena"),
                    rs.getString("u_nombre"),
                    rs.getString("u_correo"),
                    rol,
                    rs.getString("u_apellido"),
                    rs.getInt("u_dni"),
                    rs.getString("u_direccion")
                );
                if (!rs.getBoolean("u_activo")) {
                    voluntario.desactivar();
                }
                orden.asignarVoluntario(voluntario);
            }
            
            String patenteVehiculo = rs.getString("v_patente");
            if (patenteVehiculo != null) {
                Vehiculo vehiculo = new Vehiculo(
                    patenteVehiculo,
                    rs.getString("v_estado_vehiculo"),
                    rs.getString("v_tipoVeh"),
                    rs.getInt("v_capacidad")
                );
                orden.asignarVehiculo(vehiculo);
            }
            
            return orden;
        } catch (Exception e) {
            throw new SQLException("Error al mapear OrdenRetiro: " + e.getMessage(), e);
        }
    }
}