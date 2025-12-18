package ar.edu.unrn.seminario.accesos;


import java.util.List;

import ar.edu.unrn.seminario.exception.PersistenceException;
import ar.edu.unrn.seminario.modelo.Rol;

public interface RolDao {

	Rol find(Integer codigo) throws PersistenceException;
	
	Rol findById(Integer codigo) throws PersistenceException;

	List<Rol> findAll() throws PersistenceException;
}
