package ar.edu.unrn.seminario.api;

import java.util.List;
import ar.edu.unrn.seminario.dto.RolDTO;
import ar.edu.unrn.seminario.dto.UsuarioDTO;
import ar.edu.unrn.seminario.dto.PedidoDonacionDTO;
import ar.edu.unrn.seminario.dto.DonanteDTO;
import ar.edu.unrn.seminario.dto.OrdenRetiroDTO;
import ar.edu.unrn.seminario.dto.VisitaDTO;
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

	void generarOrdenRetiro(int idPedidoDonacion) throws ObjetoNuloException, ReglaNegocioException;

	List<OrdenRetiroDTO> obtenerOrdenesDeRetiro(String estado);

	OrdenRetiroDTO obtenerOrdenDeRetiroDetalle(int idOrden);

	void registrarVisita(int idOrdenRetiro, VisitaDTO visitaDTO) throws ObjetoNuloException, CampoVacioException;

	void actualizarEstadoOrdenRetiro(int idOrdenRetiro, int nuevoEstado) throws ReglaNegocioException;
}
