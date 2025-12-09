package ar.edu.unrn.seminario.accesos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import ar.edu.unrn.seminario.modelo.OrdenEntrega;
import ar.edu.unrn.seminario.modelo.PedidosDonacion;
import ar.edu.unrn.seminario.modelo.Usuario;

public class OrdenEntregaDAOJDBC implements OrdenEntregaDao {

    private UsuarioDao usuarioDao = new UsuarioDAOJDBC();
    private PedidosDonacionDao pedidoDao = new PedidosDonacionDAOJDBC();

    @Override
    public int create(OrdenEntrega orden, Connection conn) throws SQLException {
        PreparedStatement statement = null;
        ResultSet generatedKeys = null;
        try {
            statement = conn.prepareStatement(
                    "INSERT INTO ordenes_entrega(fecha_generacion, estado, usuario_voluntario, usuario_beneficiario, id_pedido_origen) "
                    + "VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);

            statement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            statement.setInt(2, orden.obtenerEstado());
            
            if (orden.obtenerVoluntario() != null) statement.setString(3, orden.obtenerVoluntario().getUsuario());
            else statement.setNull(3, java.sql.Types.VARCHAR);

            statement.setString(4, orden.obtenerBeneficiario().getUsuario());
            
            if (orden.getPedidoOrigen() != null) statement.setInt(5, orden.getPedidoOrigen().getId());
            else statement.setNull(5, java.sql.Types.INTEGER);

            if (statement.executeUpdate() == 0) throw new SQLException("Fallo creación orden entrega.");

            generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) return generatedKeys.getInt(1);
            else throw new SQLException("No ID.");
        } finally {
            if (generatedKeys != null) generatedKeys.close();
            if (statement != null) statement.close();
        }
    }

    @Override
    public void update(OrdenEntrega orden, Connection conn) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = conn.prepareStatement("UPDATE ordenes_entrega SET estado=?, usuario_voluntario=? WHERE id=?");
            statement.setInt(1, orden.obtenerEstado());
            if (orden.obtenerVoluntario() != null) statement.setString(2, orden.obtenerVoluntario().getUsuario());
            else statement.setNull(2, java.sql.Types.VARCHAR);
            statement.setInt(3, orden.getId());
            statement.executeUpdate();
        } finally {
            if (statement != null) statement.close();
        }
    }

    @Override
    public List<OrdenEntrega> findAll(Connection conn) throws SQLException {
        List<OrdenEntrega> lista = new ArrayList<>();
        Statement st = null;
        ResultSet rs = null;
        try {
            st = conn.createStatement();
            rs = st.executeQuery("SELECT * FROM ordenes_entrega");
            while (rs.next()) lista.add(mapear(rs, conn));
        } finally {
            if (rs != null) rs.close();
            if (st != null) st.close();
        }
        return lista;
    }

    @Override
    public OrdenEntrega findById(int id, Connection conn) throws SQLException {
        // Implementar similar a findAll con WHERE id=?
        return null;
    }

    @Override
    public List<OrdenEntrega> findByVoluntario(String username, Connection conn) throws SQLException {
        // Implementar similar a findAll con WHERE usuario_voluntario=?
        return new ArrayList<>();
    }

    private OrdenEntrega mapear(ResultSet rs, Connection conn) throws SQLException {
        Usuario ben = usuarioDao.find(rs.getString("usuario_beneficiario"), conn);
        Usuario vol = usuarioDao.find(rs.getString("usuario_voluntario"), conn);
        PedidosDonacion ped = pedidoDao.findById(rs.getInt("id_pedido_origen"), conn);
        return new OrdenEntrega(rs.getInt("id"), rs.getInt("estado"), ben, vol, ped);
    }
}