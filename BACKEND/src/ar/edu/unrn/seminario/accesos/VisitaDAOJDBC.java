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
import ar.edu.unrn.seminario.modelo.PedidosDonacion;
import ar.edu.unrn.seminario.modelo.ResultadoVisita;
import ar.edu.unrn.seminario.modelo.Usuario;
import ar.edu.unrn.seminario.modelo.Visita;

public class VisitaDAOJDBC implements VisitaDao {
	
	private PedidosDonacionDao pedidoDao = new PedidosDonacionDAOJDBC();

	@Override
	public void registrarVisitaCompleta(int idOrdenRetiro, int idPedido, String resultado, String observacion) throws PersistenceException {
		Connection conn = null;
		PreparedStatement stmtVisita = null;
		PreparedStatement stmtBien = null;
		PreparedStatement stmtPedido = null;
		PreparedStatement stmtOrden = null;
		try {
			conn = ConnectionManager.getConnection();
			conn.setAutoCommit(false);
			
			stmtVisita = conn.prepareStatement(
					"INSERT INTO visitas(id_orden_retiro, id_pedido_donacion, fecha_visita, resultado, observacion) "
					+ "VALUES (?, ?, ?, ?, ?)");
			
			stmtVisita.setInt(1, idOrdenRetiro);
			stmtVisita.setInt(2, idPedido);
			stmtVisita.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
			stmtVisita.setString(4, resultado);
			stmtVisita.setString(5, observacion);
			stmtVisita.executeUpdate();
			
			String nuevoEstadoPedido;
			if ("RECOLECCION_EXITOSA".equals(resultado)) {
				nuevoEstadoPedido = "COMPLETADO";
				stmtBien = conn.prepareStatement("UPDATE bienes SET estado_inventario = 'EN_STOCK' WHERE id_pedido_donacion = ?");
				stmtBien.setInt(1, idPedido);
				stmtBien.executeUpdate();
			} else if ("CANCELADO".equals(resultado)) {
				nuevoEstadoPedido = "COMPLETADO";
			} else {
				nuevoEstadoPedido = "EN_EJECUCION";
			}
			
			stmtPedido = conn.prepareStatement("UPDATE pedidos_donacion SET estado = ? WHERE id = ?");
			stmtPedido.setString(1, nuevoEstadoPedido);
			stmtPedido.setInt(2, idPedido);
			stmtPedido.executeUpdate();
			
			stmtOrden = conn.prepareStatement(
				"UPDATE ordenes_retiro o SET o.estado = ( "
				+ "CASE "
				+ "  WHEN NOT EXISTS (SELECT 1 FROM pedidos_donacion p WHERE p.id_orden_retiro = o.id AND p.estado = 'PENDIENTE') "
				+ "  AND NOT EXISTS (SELECT 1 FROM pedidos_donacion p WHERE p.id_orden_retiro = o.id AND p.estado = 'EN_EJECUCION') "
				+ "  THEN 'COMPLETADO' "
				+ "  WHEN EXISTS (SELECT 1 FROM pedidos_donacion p WHERE p.id_orden_retiro = o.id AND p.estado = 'EN_EJECUCION') "
				+ "  THEN 'EN_EJECUCION' "
				+ "  ELSE o.estado "
				+ "END) "
				+ "WHERE o.id = ?");
			stmtOrden.setInt(1, idOrdenRetiro);
			stmtOrden.executeUpdate();
			
			conn.commit();
		} catch (SQLException e) {
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
			throw new PersistenceException("Error al registrar visita completa: " + e.getMessage(), e);
		} finally {
			if (stmtVisita != null) try { stmtVisita.close(); } catch (SQLException e) {}
			if (stmtBien != null) try { stmtBien.close(); } catch (SQLException e) {}
			if (stmtPedido != null) try { stmtPedido.close(); } catch (SQLException e) {}
			if (stmtOrden != null) try { stmtOrden.close(); } catch (SQLException e) {}
			if (conn != null) try { conn.close(); } catch (SQLException e) {}
		}
	}

	@Override
	public void create(Visita visita, int idOrden, int idPedido) throws PersistenceException {
		Connection conn = null;
		PreparedStatement statement = null;
		try {
			conn = ConnectionManager.getConnection();
			conn.setAutoCommit(false);
			
			statement = conn.prepareStatement(
					"INSERT INTO visitas(id_orden_retiro, id_pedido_donacion, fecha_visita, resultado, observacion) "
					+ "VALUES (?, ?, ?, ?, ?)");
			
			statement.setInt(1, idOrden);
			statement.setInt(2, idPedido);
			statement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
			statement.setString(4, visita.obtenerResultado().toString());
			statement.setString(5, visita.obtenerObservacion());
			
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
			throw new PersistenceException("error al crear visita: " + e.getMessage(), e);
		} finally {
			if (statement != null) try { statement.close(); } catch (SQLException e) {}
			if (conn != null) try { conn.close(); } catch (SQLException e) {}
		}
	}

	@Override
	public List<Visita> findByVoluntario(Usuario voluntario) throws PersistenceException {
		Connection conn = null;
		List<Visita> visitas = new ArrayList<Visita>();
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			conn = ConnectionManager.getConnection();
			statement = conn.prepareStatement(
					"SELECT v.id, v.fecha_visita, v.resultado, v.observacion, v.id_pedido_donacion "
					+ "FROM visitas v "
					+ "JOIN ordenes_retiro o ON v.id_orden_retiro = o.id "
					+ "WHERE o.usuario_voluntario = ?");
			
			statement.setString(1, voluntario.getUsuario());
			rs = statement.executeQuery();
			
			while (rs.next()) {
				try {
					Timestamp timestamp = rs.getTimestamp("fecha_visita");
					LocalDateTime fechaHora = timestamp.toLocalDateTime();
					String observacion = rs.getString("observacion");
					String resultadoStr = rs.getString("resultado");
					int idPedido = rs.getInt("id_pedido_donacion");
					
					ResultadoVisita resultado = ResultadoVisita.fromString(resultadoStr);
					Visita visita = new Visita(fechaHora, resultado, observacion);
					
					if (idPedido > 0) {
						PedidosDonacion pedido = pedidoDao.findById(idPedido);
						if (pedido != null) {
							visita.setPedidoRelacionado(pedido);
						}
					}
					
					visitas.add(visita);
				} catch (Exception e) {
					System.err.println("error creando visita: " + e.getMessage());
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
			throw new PersistenceException("Error al buscar visitas por voluntario: " + e.getMessage(), e);
		} finally {
			if (rs != null) try { rs.close(); } catch (SQLException e) {}
			if (statement != null) try { statement.close(); } catch (SQLException e) {}
			if (conn != null) try { conn.close(); } catch (SQLException e) {}
		}
		return visitas;
	}

}
