package ar.edu.unrn.seminario.accesos;

import java.util.List;

import ar.edu.unrn.seminario.exception.PersistenceException;
import ar.edu.unrn.seminario.modelo.Bien;

public interface BienDao {

	void createBatch(List<Bien> bienes, int idPedido) throws PersistenceException;

	void updateEstadoPorPedido(int idPedido, String nuevoEstado) throws PersistenceException;
	
	Bien findById(int id) throws PersistenceException;
	
	void update(Bien bien) throws PersistenceException;
	
	void asociarAOrdenEntrega(int idBien, int idOrdenEntrega, String nuevoEstado) throws PersistenceException;
	
	List<Bien> findByOrdenEntrega(int idOrdenEntrega) throws PersistenceException;
	
	int create(Bien bien, int idPedidoOriginal) throws PersistenceException;
	
	int fraccionarYCrear(int idBienOriginal, int cantidadSolicitada) throws PersistenceException;
	
	int obtenerIdPedidoDeBien(int idBien) throws PersistenceException;
	
	List<Bien> findByEstadoInventario(String estado) throws PersistenceException;
}
