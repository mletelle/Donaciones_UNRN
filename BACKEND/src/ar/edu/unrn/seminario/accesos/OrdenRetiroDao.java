package ar.edu.unrn.seminario.accesos;

import java.util.List;

import ar.edu.unrn.seminario.exception.PersistenceException;
import ar.edu.unrn.seminario.modelo.OrdenRetiro;

public interface OrdenRetiroDao {
	
	int create(OrdenRetiro orden) throws PersistenceException;
	
	int crearOrdenConPedidos(OrdenRetiro orden, List<Integer> idsPedidos) throws PersistenceException;
	
	void update(OrdenRetiro orden) throws PersistenceException;
	
	OrdenRetiro findById(int idOrden) throws PersistenceException;
	
	List<OrdenRetiro> findByEstado(String estado) throws PersistenceException;
	
	List<OrdenRetiro> findAll() throws PersistenceException;
	
	List<OrdenRetiro> findByVoluntario(String username) throws PersistenceException;
	
}
