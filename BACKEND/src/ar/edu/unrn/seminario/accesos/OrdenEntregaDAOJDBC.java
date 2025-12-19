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
import ar.edu.unrn.seminario.modelo.Bien;
import ar.edu.unrn.seminario.modelo.EstadoBien;
import ar.edu.unrn.seminario.modelo.EstadoEntrega;
import ar.edu.unrn.seminario.modelo.OrdenEntrega;
import ar.edu.unrn.seminario.modelo.Usuario;

public class OrdenEntregaDAOJDBC implements OrdenEntregaDao {

    private static final String SQL_INSERT = "INSERT INTO ordenes_entrega (fecha_generacion, id_beneficiario, id_voluntario, estado) VALUES (?, ?, ?, ?)";
    private static final String SQL_UPDATE = "UPDATE ordenes_entrega SET id_voluntario = ?, estado = ? WHERE id = ?";
    private static final String SQL_SELECT_ALL = "SELECT * FROM ordenes_entrega";
    private static final String SQL_SELECT_BY_ID = "SELECT * FROM ordenes_entrega WHERE id = ?";
    private static final String SQL_SELECT_BY_ESTADO = "SELECT * FROM ordenes_entrega WHERE estado = ?";
    private static final String SQL_SELECT_BY_BENEFICIARIO = "SELECT oe.* FROM ordenes_entrega oe JOIN usuarios u ON oe.id_beneficiario = u.id WHERE u.usuario = ?";
    
    // SQLs auxiliares para la gestión de bienes dentro de la transacción
    private static final String SQL_BIEN_SELECT_FOR_UPDATE = "SELECT cantidad, descripcion, categoria, fecha_vencimiento, id_pedido_donacion FROM bienes WHERE id = ?";
    private static final String SQL_BIEN_UPDATE_ASIGNAR = "UPDATE bienes SET id_orden_entrega = ?, estado_inventario = ? WHERE id = ?";
    private static final String SQL_BIEN_UPDATE_RESTAR = "UPDATE bienes SET cantidad = cantidad - ? WHERE id = ?";
    private static final String SQL_BIEN_INSERT_FRACCION = "INSERT INTO bienes (id_pedido_donacion, categoria, cantidad, descripcion, fecha_vencimiento, estado_inventario, id_orden_entrega) VALUES (?, ?, ?, ?, ?, ?, ?)";

    private final UsuarioDao usuarioDao;
    private final BienDao bienDao;

    public OrdenEntregaDAOJDBC() {
        this.usuarioDao = new UsuarioDAOJDBC();
        this.bienDao = new BienDAOJDBC(); // Se usa para recuperar los bienes al leer
    }

    @Override
    public void crearOrdenConBienes(OrdenEntrega orden, Map<Integer, Integer> bienesYCantidades) throws PersistenceException {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);

            // 1. Insertar la Orden de Entrega
            int idOrdenGenerada;
            try (PreparedStatement stmtOrden = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
                stmtOrden.setTimestamp(1, Timestamp.valueOf(java.time.LocalDateTime.now()));
                stmtOrden.setInt(2, orden.getBeneficiario().getDni()); 
                
                if (orden.getVoluntario() != null) {
                    stmtOrden.setInt(3, orden.getVoluntario().getDni());
                } else {
                    stmtOrden.setNull(3, java.sql.Types.INTEGER);
                }
                
                stmtOrden.setString(4, EstadoEntrega.PENDIENTE.name());
                
                stmtOrden.executeUpdate();

                try (ResultSet rsKeys = stmtOrden.getGeneratedKeys()) {
                    if (rsKeys.next()) {
                        idOrdenGenerada = rsKeys.getInt(1);
                        orden.setId(idOrdenGenerada);
                    } else {
                        throw new SQLException("No se pudo obtener el ID de la orden de entrega.");
                    }
                }
            }

            // 2. Procesar cada bien solicitado 
            for (Map.Entry<Integer, Integer> entry : bienesYCantidades.entrySet()) {
                int idBienOriginal = entry.getKey();
                int cantidadSolicitada = entry.getValue();

                // A. Obtener datos actuales del bien
                int cantidadActual = 0;
                String descripcion = null;
                int categoriaId = 0; // O el Enum mapeado
                java.sql.Date fechaVenc = null;
                int idPedido = 0;

                try (PreparedStatement stmtSelect = conn.prepareStatement(SQL_BIEN_SELECT_FOR_UPDATE)) {
                    stmtSelect.setInt(1, idBienOriginal);
                    try (ResultSet rsBien = stmtSelect.executeQuery()) {
                        if (rsBien.next()) {
                            cantidadActual = rsBien.getInt("cantidad");
                            descripcion = rsBien.getString("descripcion");
                            categoriaId = rsBien.getInt("categoria");
                            fechaVenc = rsBien.getDate("fecha_vencimiento");
                            idPedido = rsBien.getInt("id_pedido_donacion");
                        } else {
                            throw new SQLException("Bien con ID " + idBienOriginal + " no encontrado durante la transacción.");
                        }
                    }
                }

                // B. Lógica de Stock
                if (cantidadSolicitada == cantidadActual) {
                    // Caso 1: Se lleva todo el bien
                    try (PreparedStatement stmtUpdate = conn.prepareStatement(SQL_BIEN_UPDATE_ASIGNAR)) {
                        stmtUpdate.setInt(1, idOrdenGenerada);
                        stmtUpdate.setString(2, EstadoBien.ENTREGADO.name()); // O 'RESERVADO' según tu lógica
                        stmtUpdate.setInt(3, idBienOriginal);
                        stmtUpdate.executeUpdate();
                    }
                } else if (cantidadSolicitada < cantidadActual) {
                    // Caso 2: Fraccionamiento.
                    // 2.1 Restar al original
                    try (PreparedStatement stmtRestar = conn.prepareStatement(SQL_BIEN_UPDATE_RESTAR)) {
                        stmtRestar.setInt(1, cantidadSolicitada);
                        stmtRestar.setInt(2, idBienOriginal);
                        stmtRestar.executeUpdate();
                    }

                    // 2.2 Crear nuevo bien para la orden
                    try (PreparedStatement stmtInsertNuevo = conn.prepareStatement(SQL_BIEN_INSERT_FRACCION)) {
                        stmtInsertNuevo.setInt(1, idPedido);
                        stmtInsertNuevo.setInt(2, categoriaId);
                        stmtInsertNuevo.setInt(3, cantidadSolicitada);
                        stmtInsertNuevo.setString(4, descripcion);
                        stmtInsertNuevo.setDate(5, fechaVenc);
                        stmtInsertNuevo.setString(6, EstadoBien.ENTREGADO.name());
                        stmtInsertNuevo.setInt(7, idOrdenGenerada);
                        stmtInsertNuevo.executeUpdate();
                    }
                } else {
                    throw new SQLException("Error de consistencia: Stock insuficiente para el bien " + idBienOriginal);
                }
            }

            conn.commit(); // Confirmar TODOS los cambios

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new PersistenceException("Error al crear orden de entrega con bienes: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public OrdenEntrega findById(int id) throws PersistenceException {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_ID)) {
            
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearOrden(rs);
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("Error al buscar orden de entrega por ID: " + id, e);
        }
        return null;
    }

    @Override
    public List<OrdenEntrega> findAll() throws PersistenceException {
        List<OrdenEntrega> ordenes = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                ordenes.add(mapearOrden(rs));
            }
        } catch (SQLException e) {
            throw new PersistenceException("Error al obtener todas las ordenes de entrega", e);
        }
        return ordenes;
    }

    @Override
    public List<OrdenEntrega> findByEstado(String estado) throws PersistenceException {
        List<OrdenEntrega> ordenes = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_ESTADO)) {
            
            stmt.setString(1, estado);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ordenes.add(mapearOrden(rs));
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("Error al buscar ordenes por estado: " + estado, e);
        }
        return ordenes;
    }

    @Override
    public List<OrdenEntrega> findByBeneficiario(String username) throws PersistenceException {
        List<OrdenEntrega> ordenes = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_BENEFICIARIO)) {
            
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ordenes.add(mapearOrden(rs));
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("Error al buscar ordenes por beneficiario: " + username, e);
        }
        return ordenes;
    }
    
    @Override
    public List<OrdenEntrega> findAllPendientes() throws PersistenceException {
        return findByEstado(EstadoEntrega.PENDIENTE.name());
    }

    @Override
    public void update(OrdenEntrega orden) throws PersistenceException {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {

            if (orden.getVoluntario() != null) {
                stmt.setInt(1, orden.getVoluntario().getDni()); 
            } else {
                stmt.setNull(1, java.sql.Types.INTEGER);
            }

            stmt.setString(2, orden.getEstado().name());
            stmt.setInt(3, orden.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new PersistenceException("Error al actualizar la orden de entrega ID: " + orden.getId(), e);
        }
    }

    // --- Métodos Privados de Mapeo ---

    private OrdenEntrega mapearOrden(ResultSet rs) throws SQLException, PersistenceException {
        OrdenEntrega orden = new OrdenEntrega();
        orden.setId(rs.getInt("id"));
        
        Timestamp ts = rs.getTimestamp("fecha_generacion");
        if (ts != null) {
            // Convertir Timestamp a Date o LocalDateTime según tu modelo
            orden.setFechaGeneracion(ts); 
        }

        String estadoStr = rs.getString("estado");
        orden.setEstado(EstadoEntrega.valueOf(estadoStr));

        // Cargar Beneficiario (Lazy o Eager según necesidad, aquí Eager simple)
        int idBeneficiario = rs.getInt("id_beneficiario");
        if (idBeneficiario > 0) {
            // Nota: Esto podría optimizarse con un JOIN en el SQL principal para no hacer N+1 queries
            Usuario beneficiario = usuarioDao.findByDni(idBeneficiario); 
            orden.setBeneficiario(beneficiario);
        }

        // Cargar Voluntario
        int idVoluntario = rs.getInt("id_voluntario");
        if (!rs.wasNull() && idVoluntario > 0) {
            Usuario voluntario = usuarioDao.findByDni(idVoluntario);
            orden.setVoluntario(voluntario);
        }

        // Cargar Bienes asociados
        List<Bien> bienes = bienDao.findByOrdenEntrega(orden.getId());
        orden.setBienes(bienes);

        return orden;
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

	 private int mapEstadoToId(EstadoEntrega estado) {
	        switch (estado) {
	            case PENDIENTE: return 1;
	            case COMPLETADO: return 3;
	            case CANCELADO: return 4;
	            default: throw new IllegalArgumentException("estado no mapeado: " + estado);
	        }
	    }
}