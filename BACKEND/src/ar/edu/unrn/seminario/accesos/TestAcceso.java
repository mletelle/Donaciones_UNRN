package ar.edu.unrn.seminario.accesos;

import java.sql.Connection;
import java.util.List;

import ar.edu.unrn.seminario.modelo.Rol;

public class TestAcceso {

	public static void main(String[] args) {
		try {
			Connection conn = ConnectionManager.getConnection();
			RolDao rolDao = new RolDAOJDBC();
			List<Rol> roles = rolDao.findAll(conn);

			for (Rol rol : roles) {
				System.out.println(rol);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ConnectionManager.disconnect();
		}

//		UsuarioDao usuarioDao = new UsuarioDAOJDBC();
//		Usuario usuario = new Usuario("ldifabio", "1234", "Lucas", "ldifabio@unrn.edu.ar", new Rol(1, ""));
//		usuarioDao.create(usuario);
		
//		List<Usuario> usuarios = usuarioDao.findAll();
//			for (Usuario u: usuarios) {
//			System.out.println(u);
//		}
			
//		System.out.println(usuarioDao.find("ldifabio"));
	}

}