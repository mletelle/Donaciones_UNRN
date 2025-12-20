package ar.edu.unrn.seminario.accesos;


import java.util.List;

import ar.edu.unrn.seminario.exception.PersistenceException;
import ar.edu.unrn.seminario.modelo.Usuario;

public interface UsuarioDao {
	
	void create(Usuario usuario) throws PersistenceException;

	void update(Usuario usuario) throws PersistenceException;

	Usuario find(String username) throws PersistenceException;

	List<Usuario> findAll() throws PersistenceException;
	
	List<Usuario> findByRol(int codigoRol) throws PersistenceException;

	Usuario findByDni(int dni) throws PersistenceException;
}
