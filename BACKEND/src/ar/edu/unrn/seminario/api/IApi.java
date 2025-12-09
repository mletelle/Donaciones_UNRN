package ar.edu.unrn.seminario.api;

import java.util.List;
import java.time.LocalDateTime;

import ar.edu.unrn.seminario.dto.*;
import ar.edu.unrn.seminario.exception.*;

public interface IApi {

    // usuarios
    void registrarUsuario(String username, String password, String email, String nombre, Integer rol, String apellido, int dni, String direccion, String contacto, String ubicacion) throws CampoVacioException, ObjetoNuloException, UsuarioInvalidoException;
    UsuarioDTO obtenerUsuario(String username);
    void eliminarUsuario(String username);
    List<UsuarioDTO> obtenerUsuarios();
    void activarUsuario(String username);
    void desactivarUsuario(String username);

    // roles
    List<RolDTO> obtenerRoles();
    List<RolDTO> obtenerRolesActivos();
    void guardarRol(Integer codigo, String descripcion, boolean estado) throws CampoVacioException;
    RolDTO obtenerRolPorCodigo(Integer codigo);
    void activarRol(Integer codigo);
    void desactivarRol(Integer codigo);

    // pedidos y donantes
    void registrarPedidoDonacion(PedidoDonacionDTO pedidoDTO) throws CampoVacioException, ObjetoNuloException;
    List<PedidoDonacionDTO> obtenerPedidosPendientes();
    List<PedidoDonacionDTO> obtenerTodosPedidos();
    List<PedidoDonacionDTO> obtenerPedidosDeOrden(int idOrden);
    String obtenerNombreDonantePorId(int idPedido);

    // ordenes
    void crearOrdenRetiro(List<Integer> idsPedidos, int idVoluntario, String tipoVehiculo) throws ReglaNegocioException, ObjetoNuloException;
    List<OrdenRetiroDTO> obtenerOrdenesDeRetiro(String estado);
    List<OrdenRetiroDTO> obtenerTodasOrdenesRetiro() throws ReglaNegocioException;
    List<OrdenRetiroDTO> obtenerOrdenesAsignadas(String voluntario);

    // visitas y voluntarios
    void registrarVisita(int idOrdenRetiro, int idPedido, LocalDateTime fechaHora, String resultado, String observacion) throws ObjetoNuloException, CampoVacioException, ReglaNegocioException;
    List<VoluntarioDTO> obtenerVoluntarios();
    List<VisitaDTO> obtenerVisitasPorVoluntario(VoluntarioDTO voluntario);

    // inventario
    List<BienDTO> obtenerInventario();
    
    void actualizarBienInventario(BienDTO bienDTO) throws ObjetoNuloException, CampoVacioException, ReglaNegocioException;
  /*  
    void crearOrdenEntrega(int idBeneficiario, List<Integer> idsBienesAEntregar) 
    	    throws ObjetoNuloException, ReglaNegocioException, CampoVacioException;
    */
	List<UsuarioDTO> obtenerDonantes();
}