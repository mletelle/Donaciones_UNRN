package ar.edu.unrn.seminario.api;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Map; // Importante para el Mapa de bienes

import ar.edu.unrn.seminario.dto.*;
import ar.edu.unrn.seminario.exception.*;

public interface IApi {

    // usuarios
    void registrarUsuario(String username, String password, String email, String nombre, Integer rol, 
                          String apellido, int dni, String direccion, 
                          String necesidad, int personasCargo, String prioridad) 
                          throws CampoVacioException, ObjetoNuloException, UsuarioInvalidoException;
                          
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

    // ordenes retiro
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

    // Ordenes entrega
    void crearOrdenEntrega(String userBeneficiario, Map<Integer, Integer> bienesYCantidades, String userVoluntario) 
           throws ObjetoNuloException, ReglaNegocioException, CampoVacioException;
           
    List<OrdenEntregaDTO> obtenerEntregasPorBeneficiario(String username);
    
    // Gestión Operativa de Entregas
    List<OrdenEntregaDTO> obtenerEntregasPendientes();
    void completarEntrega(int idOrden, String usuarioVoluntario) throws ObjetoNuloException;
}