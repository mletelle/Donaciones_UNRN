package ar.edu.unrn.seminario.accesos;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import ar.edu.unrn.seminario.modelo.PedidosDonacion;

public interface PedidosDonacionDao {

    int create(PedidosDonacion pedido, Connection conn) throws SQLException;

    void update(PedidosDonacion pedido, Connection conn) throws SQLException;

    PedidosDonacion findById(int idPedido, Connection conn) throws SQLException;

    List<PedidosDonacion> findAllPendientes(Connection conn) throws SQLException;

    List<PedidosDonacion> findAll(Connection conn) throws SQLException;

    List<PedidosDonacion> findByOrden(int idOrden, Connection conn) throws SQLException;

    // Nuevo método optimizado para validar pertenencia de pedido a ordennnn
    PedidosDonacion findByIdAndOrden(int idPedido, int idOrdenRetiro, Connection conn) throws SQLException;
	
	int create(PedidosDonacion pedido, Connection conn) throws SQLException;
	
	void update(PedidosDonacion pedido, Connection conn) throws SQLException;
	
	PedidosDonacion findById(int idPedido, Connection conn) throws SQLException;
	
	List<PedidosDonacion> findAllPendientes(Connection conn) throws SQLException;
	
	List<PedidosDonacion> findAll(Connection conn) throws SQLException;
	
	List<PedidosDonacion> findByOrden(int idOrden, Connection conn) throws SQLException;
	
	// Agregado
	List<PedidosDonacion> findByIds(List<Integer> idsPedidos, Connection conn) throws SQLException;
	
}
