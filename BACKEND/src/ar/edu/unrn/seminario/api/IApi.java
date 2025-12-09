package ar.edu.unrn.seminario.api;

import java.util.List;
import java.time.LocalDateTime;

import ar.edu.unrn.seminario.dto.*;
import ar.edu.unrn.seminario.exception.*;

public interface IApi {

    // usuarios
    void registrarUsuario(String username, String password, String email, String nombre, Integer rol, String apellido, int dni, String direccion) throws CampoVacioException, ObjetoNuloException, UsuarioInvalidoException;
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
    List<UsuarioDTO> obtenerDonantes();
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
    List<UsuarioDTO> obtenerVoluntarios();
    List<UsuarioDTO> obtenerBeneficiarios();
    List<VisitaDTO> obtenerVisitasPorVoluntario(UsuarioDTO voluntario);

    // inventario
    List<BienDTO> obtenerInventario();
    
    void actualizarBienInventario(BienDTO bienDTO) throws ObjetoNuloException, CampoVacioException, ReglaNegocioException;

    void darDeBajaBien(int idBien, String motivo) throws ObjetoNuloException, ReglaNegocioException;

    void crearOrdenEntrega(String userBeneficiario, List<Integer> idsBienesAEntregar) 
    	    throws ObjetoNuloException, ReglaNegocioException, CampoVacioException;
}