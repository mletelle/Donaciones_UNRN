package ar.edu.unrn.seminario.accesos;

import ar.edu.unrn.seminario.exception.PersistenceException;
import ar.edu.unrn.seminario.modelo.Vehiculo;

public interface VehiculoDao {
	
	Vehiculo findByPatente(String patente) throws PersistenceException;
	
	Vehiculo findDisponible(String tipoVehiculo) throws PersistenceException;
	
}
