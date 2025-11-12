package ar.edu.unrn.seminario.accesos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager {
	// por alguna razon el driver no anda, logre hacerlo andar solo con credenciales
	// private static String DRIVER = "com.mysql.jdbc.Driver";.

	// Railway MySQL 
	// Host y Puerto de MYSQL_PUBLIC_URL 
	private static String URL_DB = "jdbc:mysql://yamanote.proxy.rlwy.net:43821/"; 
    // Base de datos de MYSQLDATABASE
	protected static String DB = "railway";
    // Usuario de MYSQLUSER
	protected static String user = "root"; 
    // Contraseña de MYSQL_ROOT_PASSWORD
	protected static String pass = "vduEoaIuUWIxXJpQDzQXGBQrfBiTbDaY"; 
	protected static Connection conn = null;

	public static void connect() {
		try {
			conn = DriverManager.getConnection(URL_DB + DB, user, pass);
		} catch (SQLException sqlEx) {
			System.out.println("No se ha podido conectar a " + URL_DB + DB + ". " + sqlEx.getMessage());
			System.out.println("Error al cargar el driver");
		}
	}

	public static void disconnect() {
		if (conn != null) {
			try {
				conn.close();
				conn = null;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public static void reconnect() {
		disconnect();
		connect();
	}

	public static Connection getConnection() {
		if (conn == null) {
			connect();
		}
		return conn;
	}

}
