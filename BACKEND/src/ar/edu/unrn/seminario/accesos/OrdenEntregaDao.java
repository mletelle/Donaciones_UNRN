package ar.edu.unrn.seminario.accesos;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import ar.edu.unrn.seminario.modelo.OrdenEntrega;

public interface OrdenEntregaDao {
    int create(OrdenEntrega orden, Connection conn) throws SQLException;
    void update(OrdenEntrega orden, Connection conn) throws SQLException;
    OrdenEntrega findById(int id, Connection conn) throws SQLException;
    List<OrdenEntrega> findAll(Connection conn) throws SQLException;
    List<OrdenEntrega> findByVoluntario(String username, Connection conn) throws SQLException;
}