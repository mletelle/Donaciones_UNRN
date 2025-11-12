package ar.edu.unrn.seminario.accesos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager {
	
	// La URL completa ya incluye el host, puerto y base de datos
	private static final String URL_DB = "jdbc:mysql://yamanote.proxy.rlwy.net:43821/railway";
	protected static final String user = "root";
	protected static final String pass = "vduEoaIuUWIxXJpQDzQXGBQrfBiTbDaY";
	
	// Nota: El driver ya no es necesario cargarlo explícitamente en JDBC 4.0+
	
	/**
	 * Crea y devuelve una NUEVA conexión a la base de datos.
	 * Ya no almacena ni devuelve una conexión estática.
	 */
	public static Connection getConnection() throws SQLException {
		try {
			// DriverManager.getConnection toma la URL completa, usuario y contraseña.
			return DriverManager.getConnection(URL_DB, user, pass);
		} catch (SQLException sqlEx) {
			System.err.println("Error al conectar a la base de datos: " + URL_DB);
			throw sqlEx; // Re-lanzamos la excepción para que sea manejada por PersistenceApi
		}
	}

	/**
	 * Cierra la conexión de forma segura.
	 * Recibe el objeto Connection específico que se obtuvo.
	 */
	public static void disconnect(Connection conn) {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	// Los métodos connect(), reconnect(), y la variable 'conn' estática han sido eliminados.
}