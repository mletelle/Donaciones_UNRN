package ar.edu.unrn.seminario.accesos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import ar.edu.unrn.seminario.exception.PersistenceException;
import ar.edu.unrn.seminario.modelo.EstadoBien;
import ar.edu.unrn.seminario.modelo.EstadoPedido;
import ar.edu.unrn.seminario.modelo.PedidosDonacion;
import ar.edu.unrn.seminario.modelo.ResultadoVisita;
import ar.edu.unrn.seminario.modelo.Usuario;
import ar.edu.unrn.seminario.modelo.Visita;

public class VisitaDAOJDBC implements VisitaDao {
    private PedidosDonacionDao pedidoDao = new PedidosDonacionDAOJDBC();

    @Override
    public void registrarVisitaCompleta(int idOrdenRetiro, int idPedido, String resultadoStr, String observacion) 
            throws PersistenceException {
       
        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false); 
            
            try {
                ResultadoVisita resultado = ResultadoVisita.fromString(resultadoStr);
                insertarVisita(conn, idOrdenRetiro, idPedido, resultado, observacion);
                procesarImpactoEnPedido(conn, idPedido, resultado);
                actualizarEstadoOrdenRetiro(conn, idOrdenRetiro);
                conn.commit();

            } catch (SQLException | IllegalArgumentException e) {
                conn.rollback();
                throw new PersistenceException("transaccion fallida al registrar visita: " + e.getMessage(), e);
            }

        } catch (SQLException e) {
            throw new PersistenceException("error de conexión con la base de datos: " + e.getMessage(), e);
        }
    }

    private void insertarVisita(Connection conn, int idOrden, int idPedido, ResultadoVisita resultado, String obs) 
            throws SQLException {
        String sql = "INSERT INTO visitas(id_orden_retiro, id_pedido_donacion, fecha_visita, resultado, observacion) "
                   + "VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idOrden);
            stmt.setInt(2, idPedido);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(4, resultado.toString());
            stmt.setString(5, obs);
            stmt.executeUpdate();
        }
    }

    private void procesarImpactoEnPedido(Connection conn, int idPedido, ResultadoVisita resultado) throws SQLException {
        EstadoPedido nuevoEstado;
        
        if (resultado == ResultadoVisita.RECOLECCION_EXITOSA) {
            nuevoEstado = EstadoPedido.COMPLETADO; 
            actualizarStockBienes(conn, idPedido, EstadoBien.EN_STOCK);
        } else if (resultado == ResultadoVisita.CANCELADO) {
            nuevoEstado = EstadoPedido.COMPLETADO; 
        } else {
            nuevoEstado = EstadoPedido.EN_EJECUCION; 
        }

        actualizarEstadoPedido(conn, idPedido, nuevoEstado);
    }

    private void actualizarStockBienes(Connection conn, int idPedido, EstadoBien nuevoEstado) throws SQLException {
        String sql = "UPDATE bienes SET estado_inventario = ? WHERE id_pedido_donacion = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nuevoEstado.toString());
            stmt.setInt(2, idPedido);
            stmt.executeUpdate();
        }
    }

    private void actualizarEstadoPedido(Connection conn, int idPedido, EstadoPedido estado) throws SQLException {
        String sql = "UPDATE pedidos_donacion SET estado = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, estado.toString());
            stmt.setInt(2, idPedido);
            stmt.executeUpdate();
        }
    }

    private void actualizarEstadoOrdenRetiro(Connection conn, int idOrden) throws SQLException {
        String sql = "UPDATE ordenes_retiro o SET o.estado = ("
                   + "  CASE "
                   + "    WHEN NOT EXISTS (SELECT 1 FROM pedidos_donacion p WHERE p.id_orden_retiro = o.id AND p.estado IN (?, ?)) "
                   + "    THEN ? " 
                   + "    WHEN EXISTS (SELECT 1 FROM pedidos_donacion p WHERE p.id_orden_retiro = o.id AND p.estado = ?) "
                   + "    THEN ? " 
                   + "    ELSE o.estado "
                   + "  END) "
                   + "WHERE o.id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, EstadoPedido.PENDIENTE.toString());
            stmt.setString(2, EstadoPedido.EN_EJECUCION.toString()); 
            stmt.setString(3, "COMPLETADO"); 
            stmt.setString(4, EstadoPedido.EN_EJECUCION.toString());
            stmt.setString(5, "EN_EJECUCION"); 
            stmt.setInt(6, idOrden);
            
            stmt.executeUpdate();
        }
    }

    @Override
    public void create(Visita visita, int idOrden, int idPedido) throws PersistenceException {
        try (Connection conn = ConnectionManager.getConnection()) {
            insertarVisita(conn, idOrden, idPedido, visita.obtenerResultado(), visita.obtenerObservacion());
        } catch (SQLException e) {
            throw new PersistenceException("Error al crear visita individual: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Visita> findByVoluntario(Usuario voluntario) throws PersistenceException {
        List<Visita> visitas = new ArrayList<>();
        String sql = "SELECT v.id, v.fecha_visita, v.resultado, v.observacion, v.id_pedido_donacion "
                   + "FROM visitas v "
                   + "JOIN ordenes_retiro o ON v.id_orden_retiro = o.id "
                   + "WHERE o.usuario_voluntario = ?";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, voluntario.getUsuario());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    visitas.add(mapearVisita(rs));
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("Error buscando visitas: " + e.getMessage(), e);
        }
        return visitas;
    }

    private Visita mapearVisita(ResultSet rs) throws SQLException, PersistenceException {
        try {
            LocalDateTime fecha = rs.getTimestamp("fecha_visita").toLocalDateTime();
            String obs = rs.getString("observacion");
            ResultadoVisita resultado = ResultadoVisita.fromString(rs.getString("resultado"));
            
            Visita visita = new Visita(fecha, resultado, obs);
            
            int idPedido = rs.getInt("id_pedido_donacion");
            if (idPedido > 0) {
                PedidosDonacion pedido = pedidoDao.findById(idPedido);
                if (pedido != null) visita.setPedidoRelacionado(pedido);
            }
            return visita;
        } catch (Exception e) {
            throw new SQLException("Error mapeando datos de visita", e);
        }
    }
}