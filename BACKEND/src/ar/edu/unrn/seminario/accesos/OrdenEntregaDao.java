package ar.edu.unrn.seminario.accesos;

import java.util.List;
import ar.edu.unrn.seminario.exception.PersistenceException;
import ar.edu.unrn.seminario.modelo.OrdenEntrega;
import ar.edu.unrn.seminario.modelo.Bien;

public interface OrdenEntregaDao {
    
    int create(OrdenEntrega orden) throws PersistenceException;

    void crearOrdenConBienes(OrdenEntrega orden, List<Bien> bienesNuevos, List<Bien> bienesOriginales) throws PersistenceException;

    List<OrdenEntrega> findByBeneficiario(String usuario) throws PersistenceException;
    
    List<OrdenEntrega> findAllPendientes() throws PersistenceException;
    
    List<OrdenEntrega> findAll() throws PersistenceException;
    
    void update(OrdenEntrega orden) throws PersistenceException;
    
    OrdenEntrega findById(int id) throws PersistenceException;
}