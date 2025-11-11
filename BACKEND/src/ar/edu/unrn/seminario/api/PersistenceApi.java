package ar.edu.unrn.seminario.api;

import java.util.ArrayList;
import java.util.List;

import ar.edu.unrn.seminario.accesos.RolDAOJDBC;
import ar.edu.unrn.seminario.accesos.RolDao;
import ar.edu.unrn.seminario.accesos.UsuarioDAOJDBC;
import ar.edu.unrn.seminario.accesos.UsuarioDao;
import ar.edu.unrn.seminario.dto.RolDTO;
import ar.edu.unrn.seminario.dto.UsuarioDTO;
import ar.edu.unrn.seminario.exception.CampoVacioException;
import ar.edu.unrn.seminario.exception.ObjetoNuloException;
import ar.edu.unrn.seminario.exception.ReglaNegocioException;
import ar.edu.unrn.seminario.modelo.Rol;
import ar.edu.unrn.seminario.modelo.Usuario;
import ar.edu.unrn.seminario.dto.PedidoDonacionDTO;
import ar.edu.unrn.seminario.dto.DonanteDTO;
import ar.edu.unrn.seminario.dto.OrdenRetiroDTO;
import ar.edu.unrn.seminario.dto.VisitaDTO;
import ar.edu.unrn.seminario.dto.VoluntarioDTO;

public class PersistenceApi implements IApi {

	private RolDao rolDao;
	private UsuarioDao usuarioDao;

	public PersistenceApi() {
		rolDao = new RolDAOJDBC();
		usuarioDao = new UsuarioDAOJDBC();
	}

	// La firma ahora coincide con IApi (8 parámetros)
	@Override
	public void registrarUsuario(String username, String password, String email, String nombre, Integer codigoRol, String apellido, int dni, String direccion) 
			throws CampoVacioException, ObjetoNuloException { // Añadimos throws
		
		Rol rol = rolDao.find(codigoRol);
		if (rol == null) {
			throw new ObjetoNuloException("El rol con código " + codigoRol + " no existe.");
		}
		
		// Llama al constructor de Usuario con 8 parámetros
		Usuario usuario = new Usuario(username, password, nombre, email, rol, apellido, dni, direccion);
		this.usuarioDao.create(usuario);
	}


	@Override
	public List<UsuarioDTO> obtenerUsuarios() {
		List<UsuarioDTO> dtos = new ArrayList<>();
		List<Usuario> usuarios = usuarioDao.findAll();
		for (Usuario u : usuarios) {
			dtos.add(new UsuarioDTO(u.getUsuario(), u.getContrasena(), u.getNombre(), u.getEmail(),
					u.getRol().getNombre(), u.isActivo(), u.obtenerEstado()));
		}
		return dtos;
	}

	@Override
	public UsuarioDTO obtenerUsuario(String username) {
		Usuario u = usuarioDao.find(username);
		if (u == null) {
			return null;
		}
		return new UsuarioDTO(u.getUsuario(), u.getContrasena(), u.getNombre(), u.getEmail(),
					u.getRol().getNombre(), u.isActivo(), u.obtenerEstado());
	}

	@Override
	public void eliminarUsuario(String username) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<RolDTO> obtenerRoles() {
		List<Rol> roles = rolDao.findAll();
		List<RolDTO> rolesDTO = new ArrayList<>(0);
		for (Rol rol : roles) {
			rolesDTO.add(new RolDTO(rol.getCodigo(), rol.getNombre(), rol.isActivo()));
		}
		return rolesDTO;
	}

	@Override
	public List<RolDTO> obtenerRolesActivos() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void guardarRol(Integer codigo, String descripcion, boolean estado) {
		// TODO Auto-generated method stub

	}

	@Override
	public RolDTO obtenerRolPorCodigo(Integer codigo) {
		Rol rol = rolDao.find(codigo);
		if (rol == null) {
			return null;
		}
		RolDTO rolDTO = new RolDTO(rol.getCodigo(), rol.getNombre(), rol.isActivo());
		return rolDTO;
	}

	@Override
	public void activarRol(Integer codigo) {
		// TODO Auto-generated method stub

	}

	@Override
	public void desactivarRol(Integer codigo) {
		// TODO Auto-generated method stub

	}

	@Override
	public void activarUsuario(String username) {
		// TODO Auto-generated method stub

	}

	@Override
	public void desactivarUsuario(String username) {
		// TODO Auto-generated method stub

	}

	// --- Métodos de IApi no implementados por PersistenceApi ---
	// (Estos métodos lanzarán un error si se intentan usar,
	// lo cual es correcto ya que solo implementaste la persistencia de Usuarios y Roles)

	@Override
	public void registrarPedidoDonacion(PedidoDonacionDTO pedidoDTO) throws CampoVacioException, ObjetoNuloException {
		throw new UnsupportedOperationException("Este método no está implementado en PersistenceApi.");
	}

	@Override
	public List<DonanteDTO> obtenerDonantes() {
		throw new UnsupportedOperationException("Este método no está implementado en PersistenceApi.");
	}

	@Override
	public List<PedidoDonacionDTO> obtenerPedidosPendientes() {
		throw new UnsupportedOperationException("Este método no está implementado en PersistenceApi.");
	}

	@Override
	public List<PedidoDonacionDTO> obtenerPedidosDeOrden(int idOrden) {
		throw new UnsupportedOperationException("Este método no está implementado en PersistenceApi.");
	}

	@Override
	public List<OrdenRetiroDTO> obtenerOrdenesDeRetiro(String estado) {
		throw new UnsupportedOperationException("Este método no está implementado en PersistenceApi.");
	}

	@Override
	public void registrarVisita(int idOrdenRetiro, int idPedido, VisitaDTO visitaDTO) throws ObjetoNuloException, CampoVacioException, ReglaNegocioException {
		throw new UnsupportedOperationException("Este método no está implementado en PersistenceApi.");
	}

	@Override
	public List<VoluntarioDTO> obtenerVoluntarios() {
		throw new UnsupportedOperationException("Este método no está implementado en PersistenceApi.");
	}

	@Override
	public void crearOrdenRetiro(List<Integer> idsPedidos, int idVoluntario, String tipoVehiculo) throws ReglaNegocioException, ObjetoNuloException {
		throw new UnsupportedOperationException("Este método no está implementado en PersistenceApi.");
	}

	@Override
	public List<OrdenRetiroDTO> obtenerOrdenesAsignadas(String voluntario) {
		throw new UnsupportedOperationException("Este método no está implementado en PersistenceApi.");
	}

	@Override
	public List<VisitaDTO> obtenerVisitasPorVoluntario(VoluntarioDTO voluntario) {
		throw new UnsupportedOperationException("Este método no está implementado en PersistenceApi.");
	}

	@Override
	public String obtenerNombreDonantePorId(int idPedido) {
		throw new UnsupportedOperationException("Este método no está implementado en PersistenceApi.");
	}
}