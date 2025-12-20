package ar.edu.unrn.seminario.accesos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager {
	
	private static final String URL_DB = "jdbc:mysql://localhost:3306/";
	private static final String DB = "seminario_2025_1";
	private static final String USER = "seminario"; 
	private static final String PASS = "Seminario_Pass_123!";
	
	static {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("error cargando driver mysql", e);
		}
	}
	
	private ConnectionManager() {}
	
	public static Connection getConnection() throws SQLException {
		String url = URL_DB + DB + "?useSSL=false&allowPublicKeyRetrieval=true";
		return DriverManager.getConnection(url, USER, PASS);
	}
}

