package ar.edu.unrn.seminario.api;

import java.util.List;
import ar.edu.unrn.seminario.dto.RolDTO;
import ar.edu.unrn.seminario.dto.UsuarioDTO;
import ar.edu.unrn.seminario.dto.PedidoDonacionDTO;
import ar.edu.unrn.seminario.dto.DonanteDTO;
import ar.edu.unrn.seminario.dto.OrdenRetiroDTO;
import ar.edu.unrn.seminario.dto.VisitaDTO;
import ar.edu.unrn.seminario.dto.VoluntarioDTO;
import ar.edu.unrn.seminario.exception.CampoVacioException;
import ar.edu.unrn.seminario.exception.ObjetoNuloException;
import ar.edu.unrn.seminario.exception.ReglaNegocioException;

public interface IApi {

	void registrarUsuario(String username, String password, String email, String nombre, Integer rol) throws CampoVacioException, ObjetoNuloException;

	UsuarioDTO obtenerUsuario(String username);

	void eliminarUsuario(String username);
	
	List<RolDTO> obtenerRoles();

	List<RolDTO> obtenerRolesActivos();

	void guardarRol(Integer codigo, String descripcion, boolean estado) throws CampoVacioException;

	RolDTO obtenerRolPorCodigo(Integer codigo);

	void activarRol(Integer codigo);

	void desactivarRol(Integer codigo);

	List<UsuarioDTO> obtenerUsuarios();

	void activarUsuario(String username);

	void desactivarUsuario(String username);

	void registrarPedidoDonacion(PedidoDonacionDTO pedidoDTO) throws CampoVacioException, ObjetoNuloException;

	List<DonanteDTO> obtenerDonantes();

	List<PedidoDonacionDTO> obtenerPedidosPendientes();

	List<PedidoDonacionDTO> obtenerPedidosDeOrden(int idOrden);

	List<OrdenRetiroDTO> obtenerOrdenesDeRetiro(String estado);

	void registrarVisita(int idOrdenRetiro, int idPedido, VisitaDTO visitaDTO) throws ObjetoNuloException, CampoVacioException, ReglaNegocioException;

	List<VoluntarioDTO> obtenerVoluntarios();

	void crearOrdenRetiro(List<Integer> idsPedidos, int idVoluntario, String tipoVehiculo) throws ReglaNegocioException, ObjetoNuloException;

	List<OrdenRetiroDTO> obtenerOrdenesAsignadas(String voluntario);

	List<VisitaDTO> obtenerVisitasPorVoluntario(VoluntarioDTO voluntario);

	String obtenerNombreDonantePorId(int idPedido);
}