package ar.edu.unrn.seminario.accesos;

import java.sql.Connection;
import java.sql.SQLException;
import ar.edu.unrn.seminario.modelo.OrdenEntrega;

public interface OrdenEntregaDao {
    int create(OrdenEntrega orden, Connection conn) throws SQLException;
}
