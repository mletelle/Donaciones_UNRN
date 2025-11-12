package ar.edu.unrn.seminario.accesos;

import java.sql.Connection;
import java.sql.SQLException;

import ar.edu.unrn.seminario.modelo.Vehiculo;

public interface VehiculoDao {
	
	Vehiculo findByPatente(String patente, Connection conn) throws SQLException;
	
	Vehiculo findDisponible(String tipoVehiculo, Connection conn) throws SQLException;
	
}
