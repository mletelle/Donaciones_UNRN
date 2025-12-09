package ar.edu.unrn.seminario.accesos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import ar.edu.unrn.seminario.modelo.OrdenEntrega;

public class OrdenEntregaDAOJDBC implements OrdenEntregaDao {

    public int create(OrdenEntrega orden, Connection conn) throws SQLException {
        String sql = "INSERT INTO ordenes_entrega (fecha_generacion, estado, usuario_beneficiario) VALUES (?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setDate(1, new java.sql.Date(orden.getFechaGeneracion().getTime()));
            stmt.setString(2, "PENDIENTE");
            stmt.setString(3, orden.getBeneficiario().getUsuario());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                throw new SQLException("error creando orden de entrega");
            }
        }
    }
}
