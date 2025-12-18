package ar.edu.unrn.seminario.accesos;

import java.util.List;
import ar.edu.unrn.seminario.exception.PersistenceException;
import ar.edu.unrn.seminario.modelo.OrdenEntrega;

public interface OrdenEntregaDao {
    
    int create(OrdenEntrega orden) throws PersistenceException;

    int crearOrdenConBienes(OrdenEntrega orden, java.util.Map<Integer, Integer> bienesYCantidades) throws PersistenceException;

    List<OrdenEntrega> findByBeneficiario(String usuario) throws PersistenceException;
    
    List<OrdenEntrega> findAllPendientes() throws PersistenceException;
    
    List<OrdenEntrega> findAll() throws PersistenceException;
    
    void update(OrdenEntrega orden) throws PersistenceException;
    
    OrdenEntrega findById(int id) throws PersistenceException;
}