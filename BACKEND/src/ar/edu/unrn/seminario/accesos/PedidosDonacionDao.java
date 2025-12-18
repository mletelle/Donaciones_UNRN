package ar.edu.unrn.seminario.accesos;

import java.util.List;

import ar.edu.unrn.seminario.exception.PersistenceException;
import ar.edu.unrn.seminario.modelo.PedidosDonacion;

public interface PedidosDonacionDao {

    PedidosDonacion findById(int idPedido  ) throws PersistenceException;

    // Nuevo método optimizado para validar pertenencia de pedido a orden
    PedidosDonacion findByIdAndOrden(int idPedido, int idOrdenRetiro  ) throws PersistenceException;
	
	int create(PedidosDonacion pedido  ) throws PersistenceException;
	
	void update(PedidosDonacion pedido  ) throws PersistenceException;
	
	
	List<PedidosDonacion> findAllPendientes(  ) throws PersistenceException;
	
	List<PedidosDonacion> findAll(  ) throws PersistenceException;
	
	List<PedidosDonacion> findByOrden(int idOrden  ) throws PersistenceException;
	
	List<PedidosDonacion> findByIds(List<Integer> idsPedidos  ) throws PersistenceException;
	
	void actualizarEstadoYOrdenLote(List<Integer> idsPedidos, String nuevoEstado, int idOrden) throws PersistenceException;
	
}
