package ar.edu.unrn.seminario.accesos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import ar.edu.unrn.seminario.modelo.OrdenEntrega;
import ar.edu.unrn.seminario.modelo.Usuario;
import ar.edu.unrn.seminario.modelo.Vehiculo;
import ar.edu.unrn.seminario.modelo.Bien;

public class OrdenEntregaDAOJDBC implements OrdenEntregaDao {

    private UsuarioDao usuarioDao = new UsuarioDAOJDBC();
    private VehiculoDao vehiculoDao = new VehiculoDAOJDBC();
    private BienDao bienDao = new BienDAOJDBC(); 

    @Override
    public int create(OrdenEntrega orden, Connection conn) throws SQLException {
        String sql = "INSERT INTO ordenes_entrega (fecha_generacion, estado, usuario_beneficiario, usuario_voluntario) VALUES (?, ?, ?, ?)";
        
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        
        try {
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            stmt.setTimestamp(1, new Timestamp(orden.getFechaGeneracion().getTime()));
            stmt.setInt(2, orden.getEstado());
            stmt.setString(3, orden.getBeneficiario().getUsuario());
            
            if (orden.getVoluntario() != null) {
                stmt.setString(4, orden.getVoluntario().getUsuario());
            } else {
                stmt.setNull(4, java.sql.Types.VARCHAR);
            }

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("No se pudo crear la orden de entrega, no se afectaron filas.");
            }

            generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int id = generatedKeys.getInt(1);
                orden.setId(id); 
                return id;
            } else {
                throw new SQLException("No se pudo crear la orden de entrega, no se obtuvo el ID.");
            }
        } finally {
            if (generatedKeys != null) generatedKeys.close();
            if (stmt != null) stmt.close();
        }
    }

    @Override
    public List<OrdenEntrega> findByBeneficiario(String usuario, Connection conn) throws SQLException {
        List<OrdenEntrega> ordenes = new ArrayList<>();
        String sql = "SELECT * FROM ordenes_entrega WHERE usuario_beneficiario = ? ORDER BY fecha_generacion DESC";

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, usuario);
            rs = stmt.executeQuery();

            while (rs.next()) {
                OrdenEntrega orden = mapearOrden(rs, conn);
                ordenes.add(orden);
            }
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        }
        return ordenes;
    }

    @Override
    public List<OrdenEntrega> findAllPendientes(Connection conn) throws SQLException {
        List<OrdenEntrega> ordenes = new ArrayList<>();
        String sql = "SELECT * FROM ordenes_entrega WHERE estado = 1 ORDER BY fecha_generacion ASC";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ordenes.add(mapearOrden(rs, conn));
            }
        }
        return ordenes;
    }

    @Override
    public void update(OrdenEntrega orden, Connection conn) throws SQLException {
        String sql = "UPDATE ordenes_entrega SET estado = ?, usuario_voluntario = ?, fecha_ejecucion = ? WHERE id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orden.getEstado());
            
            if (orden.getVoluntario() != null) stmt.setString(2, orden.getVoluntario().getUsuario());
            else stmt.setNull(2, java.sql.Types.VARCHAR);
            
            if (orden.getEstado() == OrdenEntrega.ESTADO_COMPLETADO) {
                 stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            } else {
                 stmt.setNull(3, java.sql.Types.TIMESTAMP);
            }
            
            stmt.setInt(4, orden.getId());
            
            stmt.executeUpdate();
        }
    }
    
    @Override
    public OrdenEntrega findById(int id, Connection conn) throws SQLException {
        String sql = "SELECT * FROM ordenes_entrega WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapearOrden(rs, conn);
            }
        }
        return null;
    }

    private OrdenEntrega mapearOrden(ResultSet rs, Connection conn) throws SQLException {
        String nombreBeneficiario = rs.getString("usuario_beneficiario");
        Usuario beneficiario = usuarioDao.find(nombreBeneficiario, conn);
        
        int idOrden = rs.getInt("id");
        List<Bien> bienesDeLaOrden = bienDao.findByOrdenEntrega(idOrden, conn);
        
        OrdenEntrega orden = new OrdenEntrega(beneficiario, bienesDeLaOrden);
        orden.setId(idOrden);
        orden.setFechaGeneracion(rs.getTimestamp("fecha_generacion"));
        orden.setEstado(rs.getInt("estado"));
        
        String nombreVoluntario = rs.getString("usuario_voluntario");
        if (nombreVoluntario != null) {
            Usuario voluntario = usuarioDao.find(nombreVoluntario, conn);
            orden.setVoluntario(voluntario);
        }

        String patente = rs.getString("patente_vehiculo");
        if (patente != null) {
            Vehiculo vehiculo = vehiculoDao.findByPatente(patente, conn);
            orden.setVehiculo(vehiculo);
        }

        return orden;
    }
}