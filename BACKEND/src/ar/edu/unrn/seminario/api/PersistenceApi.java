package ar.edu.unrn.seminario.api;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import ar.edu.unrn.seminario.accesos.BienDao;
import ar.edu.unrn.seminario.accesos.BienDAOJDBC;
import ar.edu.unrn.seminario.accesos.ConnectionManager;
import ar.edu.unrn.seminario.accesos.OrdenRetiroDao;
import ar.edu.unrn.seminario.accesos.OrdenRetiroDAOJDBC;
import ar.edu.unrn.seminario.accesos.PedidosDonacionDao;
import ar.edu.unrn.seminario.accesos.PedidosDonacionDAOJDBC;
import ar.edu.unrn.seminario.accesos.RolDAOJDBC;
import ar.edu.unrn.seminario.accesos.RolDao;
import ar.edu.unrn.seminario.accesos.UsuarioDAOJDBC;
import ar.edu.unrn.seminario.accesos.UsuarioDao;
import ar.edu.unrn.seminario.accesos.VehiculoDao;
import ar.edu.unrn.seminario.accesos.VehiculoDAOJDBC;
import ar.edu.unrn.seminario.accesos.VisitaDao;
import ar.edu.unrn.seminario.accesos.VisitaDAOJDBC;
import ar.edu.unrn.seminario.dto.DonanteDTO;
import ar.edu.unrn.seminario.dto.OrdenRetiroDTO;
import ar.edu.unrn.seminario.dto.PedidoDonacionDTO;
import ar.edu.unrn.seminario.dto.RolDTO;
import ar.edu.unrn.seminario.dto.UsuarioDTO;
import ar.edu.unrn.seminario.dto.VisitaDTO;
import ar.edu.unrn.seminario.dto.VoluntarioDTO;
import ar.edu.unrn.seminario.dto.BienDTO;
import ar.edu.unrn.seminario.exception.CampoVacioException;
import ar.edu.unrn.seminario.exception.ObjetoNuloException;
import ar.edu.unrn.seminario.exception.ReglaNegocioException;
import ar.edu.unrn.seminario.modelo.Bien;
import ar.edu.unrn.seminario.modelo.OrdenRetiro;
import ar.edu.unrn.seminario.modelo.PedidosDonacion;
import ar.edu.unrn.seminario.modelo.Rol;
import ar.edu.unrn.seminario.modelo.Usuario;
import ar.edu.unrn.seminario.modelo.Vehiculo;
import ar.edu.unrn.seminario.modelo.Visita;
import ar.edu.unrn.seminario.modelo.ResultadoVisita;

public class PersistenceApi implements IApi {

	private RolDao rolDao;
	private UsuarioDao usuarioDao;
	private PedidosDonacionDao pedidoDao;
	private BienDao bienDao;
	private OrdenRetiroDao ordenDao;
	private VehiculoDao vehiculoDao;
	private VisitaDao visitaDao;

	public PersistenceApi() {
		rolDao = new RolDAOJDBC();
		usuarioDao = new UsuarioDAOJDBC();
		pedidoDao = new PedidosDonacionDAOJDBC();
		bienDao = new BienDAOJDBC();
		ordenDao = new OrdenRetiroDAOJDBC();
		vehiculoDao = new VehiculoDAOJDBC();
		visitaDao = new VisitaDAOJDBC();
	}

	@Override
	public void registrarUsuario(String username, String password, String email, String nombre, Integer codigoRol,
			String apellido, int dni, String direccion) throws CampoVacioException, ObjetoNuloException {
		Connection conn = null;
		try {
			conn = ConnectionManager.getConnection();
			conn.setAutoCommit(false);
			
			Rol rol = rolDao.find(codigoRol, conn); // levantar el rol desde la base de datos
			if (rol == null) {
				throw new ObjetoNuloException("Rol no encontrado");
			}
			
			Usuario usuario = new Usuario(username, password, nombre, email, rol, apellido, dni, direccion); // nuevo constructor
			usuarioDao.create(usuario, conn); // crear el usuario
			
			conn.commit();
		} catch (SQLException e) { // captura errores SQL
			try {
				if (conn != null) conn.rollback();
			} catch (SQLException e2) {
				e2.printStackTrace(); // log error
			}
			throw new RuntimeException("Error registrando usuario", e); 
		} catch (Exception e) { // captura otros errores
			try {
				if (conn != null) conn.rollback();
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
			throw e;
		} finally { // asegura restaurar auto-commit y desconectar
			if (conn != null) {
				try {
					conn.setAutoCommit(true);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			ConnectionManager.disconnect();
		}
	}

	@Override
	public List<UsuarioDTO> obtenerUsuarios() { // listado completo
		Connection conn = null;
		List<UsuarioDTO> dtos = new ArrayList<>();
		try { // obtener todos los usuarios
			conn = ConnectionManager.getConnection();
			List<Usuario> usuarios = usuarioDao.findAll(conn);
			for (Usuario u : usuarios) {
				dtos.add(new UsuarioDTO(u.getUsuario(), u.getContrasena(), u.getNombre(), u.getEmail(),
						u.getRol().getNombre(), u.isActivo(), u.obtenerEstado()));
			}
		} catch (SQLException e) { // captura errores SQL
			e.printStackTrace();
		} finally {
			ConnectionManager.disconnect();
		}
		return dtos;
	}

	@Override
	public UsuarioDTO obtenerUsuario(String username) { // obtener por username
		Connection conn = null;
		try {
			conn = ConnectionManager.getConnection();
			Usuario u = usuarioDao.find(username, conn);
			if (u != null) {
				return new UsuarioDTO(u.getUsuario(), u.getContrasena(), u.getNombre(), u.getEmail(),
						u.getRol().getNombre(), u.isActivo(), u.obtenerEstado());
			}
		} catch (SQLException e) { // captura errores SQL
			e.printStackTrace();
		} finally {
			ConnectionManager.disconnect();
		}
		return null;
	}

	@Override
	public void eliminarUsuario(String username) {
		// TODO Auto-generated method stub
	}

	@Override
	public List<RolDTO> obtenerRoles() { // listado completo
		Connection conn = null;
		List<RolDTO> rolesDTO = new ArrayList<>();
		try {
			conn = ConnectionManager.getConnection();
			List<Rol> roles = rolDao.findAll(conn); // obtener todos los roles
			for (Rol rol : roles) {
				rolesDTO.add(new RolDTO(rol.getCodigo(), rol.getNombre(), rol.isActivo()));
			}
		} catch (SQLException e) {
			e.printStackTrace(); // captura errores SQL
		} finally {
			ConnectionManager.disconnect();
		}
		return rolesDTO;
	}

	@Override
	public List<RolDTO> obtenerRolesActivos() { // listado de roles activos
		Connection conn = null;
		List<RolDTO> rolesDTO = new ArrayList<>();
		try {
			conn = ConnectionManager.getConnection();
			List<Rol> roles = rolDao.findAll(conn);
			for (Rol rol : roles) {
				if (rol.isActivo()) {
					rolesDTO.add(new RolDTO(rol.getCodigo(), rol.getNombre(), rol.isActivo())); // solo activos
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionManager.disconnect();
		}
		return rolesDTO;
	}

	@Override
	public void guardarRol(Integer codigo, String descripcion, boolean estado) throws CampoVacioException {
		// TODO Auto-generated method stub
	}

	@Override
	public RolDTO obtenerRolPorCodigo(Integer codigo) { // obtener rol por codigo
		Connection conn = null;
		try {
			conn = ConnectionManager.getConnection();
			Rol rol = rolDao.find(codigo, conn);
			if (rol != null) {
				return new RolDTO(rol.getCodigo(), rol.getNombre(), rol.isActivo());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionManager.disconnect();
		}
		return null;
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
	public void activarUsuario(String username) { // activar solo un usuario
		Connection conn = null;
		try {
			conn = ConnectionManager.getConnection();
			conn.setAutoCommit(false);
			
			Usuario usuario = usuarioDao.find(username, conn); // buscar usuario
			if (usuario != null) { // si existe, activarlo
				usuario.activar();
				usuarioDao.update(usuario, conn);
			}
			
			conn.commit();
		} catch (SQLException e) { // captura errores SQL
			try {
				if (conn != null) conn.rollback(); // rollback si hay error
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.setAutoCommit(true);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			ConnectionManager.disconnect();
		}
	}

	@Override
	public void desactivarUsuario(String username) { // desactivar solo un usuario
		Connection conn = null;
		try {
			conn = ConnectionManager.getConnection();
			conn.setAutoCommit(false);
			
			Usuario usuario = usuarioDao.find(username, conn);
			if (usuario != null) {
				usuario.desactivar();
				usuarioDao.update(usuario, conn);
			}
			
			conn.commit();
		} catch (SQLException e) {
			try {
				if (conn != null) conn.rollback(); // rollback si hay error
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.setAutoCommit(true); // restaurar auto-commit
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			ConnectionManager.disconnect();
		}
	}

	@Override
	public void registrarPedidoDonacion(PedidoDonacionDTO pedidoDTO) throws CampoVacioException, ObjetoNuloException { // registrar nuevo pedido
		Connection conn = null;
		try {
			conn = ConnectionManager.getConnection();
			conn.setAutoCommit(false);

			// encontrar donante por DNI
			List<Usuario> donantes = usuarioDao.findByRol(3, conn);
			Usuario donante = null;
			for (Usuario u : donantes) {
				if (u.getDni() == pedidoDTO.getDonanteId()) {
					donante = u;
					break;
				}
			}
			
			if (donante == null) { // no se encontro donante
				throw new ObjetoNuloException("Donante no encontrado");
			}
			
			// convertir BienDTO a Bien
			List<Bien> bienes = new ArrayList<>();
			for (BienDTO bienDTO : pedidoDTO.getBienes()) {
				Bien bien = new Bien(bienDTO.getTipo(), bienDTO.getCantidad(), bienDTO.getCategoria());
				bienes.add(bien);
			}
			
			// crear PedidoDonacion
			PedidosDonacion pedido = new PedidosDonacion(
					LocalDateTime.now(),
					bienes,
					pedidoDTO.getTipoVehiculo(),
					donante);
			
			// persistir pedido
			int idPedido = pedidoDao.create(pedido, conn);
			
			bienDao.createBatch(bienes, idPedido, conn); // persistir bienes
			
			conn.commit();
		} catch (SQLException e) {
			try {
				if (conn != null) conn.rollback(); // rollback si hay error
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
			throw new RuntimeException("Error registrando pedido", e);
		} catch (Exception e) { // captura otros errores
			try {
				if (conn != null) conn.rollback();
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
			throw e;
		} finally {
			if (conn != null) {
				try {
					conn.setAutoCommit(true);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			ConnectionManager.disconnect();
		}
	}

	@Override
	public List<DonanteDTO> obtenerDonantes() { // listado de donantes
		Connection conn = null;
		List<DonanteDTO> dtos = new ArrayList<>();
		try {
			conn = ConnectionManager.getConnection();
			List<Usuario> donantes = usuarioDao.findByRol(3, conn); // rol donante = 3
			for (Usuario d : donantes) {
				dtos.add(new DonanteDTO(d.getDni(), d.getNombre() + " " + d.getApellido(), d.obtenerDireccion()));
			}
		} catch (SQLException e) { // captura errores de SQL
			e.printStackTrace();
		} finally {
			ConnectionManager.disconnect();
		}
		return dtos;
	}

	@Override
	public List<PedidoDonacionDTO> obtenerPedidosPendientes() { // pedidos con estado PENDIENTE
		Connection conn = null;
		List<PedidoDonacionDTO> dtos = new ArrayList<>();
		try {
			conn = ConnectionManager.getConnection(); 
			List<PedidosDonacion> pedidos = pedidoDao.findAllPendientes(conn); 
			for (PedidosDonacion p : pedidos) {
				String nombreCompletoDonante = p.getDonante().getNombre() + " " + p.getDonante().getApellido() + " " + p.getDonante().obtenerDireccion();
				PedidoDonacionDTO dto = new PedidoDonacionDTO(
						p.getId(),
						p.obtenerFecha().toString(),
						p.describirTipoVehiculo(),
						p.getDonante().getDni(),
						nombreCompletoDonante);
				dtos.add(dto);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionManager.disconnect();
		}
		return dtos;
	}

	@Override
	public List<PedidoDonacionDTO> obtenerTodosPedidos() { // todos los pedidos sin filtro
		Connection conn = null;
		List<PedidoDonacionDTO> dtos = new ArrayList<>();
		try {
			conn = ConnectionManager.getConnection(); 
			List<PedidosDonacion> pedidos = pedidoDao.findAll(conn); 
			for (PedidosDonacion p : pedidos) {
				String nombreCompletoDonante = p.getDonante().getNombre() + " " + p.getDonante().getApellido() + " " + p.getDonante().obtenerDireccion();
				PedidoDonacionDTO dto = new PedidoDonacionDTO(
						p.getId(),
						p.obtenerFecha().toString(),
						p.describirTipoVehiculo(),
						p.getDonante().getDni(),
						nombreCompletoDonante,
						p.obtenerEstado());
				dtos.add(dto);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionManager.disconnect();
		}
		return dtos;
	}

	@Override
	public List<PedidoDonacionDTO> obtenerPedidosDeOrden(int idOrden) { // pedidos asociados a una orden
		Connection conn = null;
		List<PedidoDonacionDTO> dtos = new ArrayList<>();
		try {
			conn = ConnectionManager.getConnection();
			List<PedidosDonacion> pedidos = pedidoDao.findByOrden(idOrden, conn); // obtener pedidos por orden
			for (PedidosDonacion p : pedidos) {
				String nombreDonante = p.getDonante().getNombre() + " " + p.getDonante().getApellido();
				String direccion = p.obtenerDireccion();
				String estado = p.obtenerEstado();
				dtos.add(new PedidoDonacionDTO( // mapear a DTO con direccion y estado
						p.getId(),
						nombreDonante,
						direccion,
						estado));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionManager.disconnect();
		}
		return dtos;
	}

	@Override
	public List<OrdenRetiroDTO> obtenerOrdenesDeRetiro(String estado) {
		Connection conn = null;
		List<OrdenRetiroDTO> dtos = new ArrayList<>();
		try {
			conn = ConnectionManager.getConnection();
			List<OrdenRetiro> ordenes = ordenDao.findByEstado(estado, conn);
			for (OrdenRetiro o : ordenes) { // para cada orden de retiro, recorrer y mapear a DTO
				String nombreVoluntario = "";
				if (o.obtenerVoluntarioPrincipal() != null) {
					Usuario vol = o.obtenerVoluntarioPrincipal();
					nombreVoluntario = vol.getNombre() + " " + vol.getApellido();
				}
				
				String nombreDonante = "";
				if (o.obtenerDonante() != null) {
					Usuario don = o.obtenerDonante();
					nombreDonante = don.getNombre() + " " + don.getApellido();
				}
				
				String descripcionVehiculo = "";
				if (o.obtenerVehiculo() != null) {
					descripcionVehiculo = o.obtenerVehiculo().getDescripcion();
				}
				
				OrdenRetiroDTO dto = new OrdenRetiroDTO(
						o.getId(),
						o.obtenerNombreEstado(),
						o.obtenerFechaCreacion(),
						new ArrayList<VisitaDTO>(),
						nombreDonante,
						descripcionVehiculo,
						nombreVoluntario);
				dtos.add(dto);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionManager.disconnect();
		}
		return dtos;
	}

	@Override
	public List<OrdenRetiroDTO> obtenerTodasOrdenesRetiro() { // todas las ordenes sin filtro
		Connection conn = null;
		List<OrdenRetiroDTO> dtos = new ArrayList<>();
		try {
			conn = ConnectionManager.getConnection();
			List<OrdenRetiro> ordenes = ordenDao.findAll(conn);
			for (OrdenRetiro o : ordenes) {
				String nombreVoluntario = "";
				if (o.obtenerVoluntarioPrincipal() != null) {
					Usuario vol = o.obtenerVoluntarioPrincipal();
					nombreVoluntario = vol.getNombre() + " " + vol.getApellido();
				}
				
				String nombreDonante = "";
				if (o.obtenerDonante() != null) {
					Usuario don = o.obtenerDonante();
					nombreDonante = don.getNombre() + " " + don.getApellido();
				}
				
				String descripcionVehiculo = "";
				if (o.obtenerVehiculo() != null) {
					descripcionVehiculo = o.obtenerVehiculo().getDescripcion();
				}
				
				OrdenRetiroDTO dto = new OrdenRetiroDTO(
						o.getId(),
						o.obtenerNombreEstado(),
						o.obtenerFechaCreacion(),
						new ArrayList<VisitaDTO>(),
						nombreDonante,
						descripcionVehiculo,
						nombreVoluntario);
				dtos.add(dto);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionManager.disconnect();
		}
		return dtos;
	}

	@Override
	public void registrarVisita(int idOrdenRetiro, int idPedido, VisitaDTO visitaDTO) // registrar visita y actualizar estados
			throws ObjetoNuloException, CampoVacioException, ReglaNegocioException {
		Connection conn = null;
		try {
			conn = ConnectionManager.getConnection();
			conn.setAutoCommit(false);
			
			// cargar OrdenRetiro y PedidoDonacion
			OrdenRetiro orden = ordenDao.findById(idOrdenRetiro, conn);
			PedidosDonacion pedido = pedidoDao.findById(idPedido, conn);
			
			if (orden == null) { // si no se encuentra orden o pedido, lanzar excepcion
				throw new ObjetoNuloException("Orden no encontrada");
			}
			if (pedido == null) {
				throw new ObjetoNuloException("Pedido no encontrado");
			}
			
			// asignar la orden completa al pedido para que las validaciones funcionen correctamente
			pedido.asignarOrden(orden);
			
			// cargar datos de visita desde DTO
			ResultadoVisita resultado = ResultadoVisita.valueOf(visitaDTO.getResultado().replace(" ", "_").toUpperCase());
			Visita visita = new Visita(LocalDateTime.now(), resultado, visitaDTO.getObservacion());
			
			// persistir visita
			visitaDao.create(visita, idOrdenRetiro, idPedido, conn);
			
			// actualizar estado pedido segun resultado visita
			// usar metodos con validaciones para evitar cambios invalidos de estado
			if (resultado == ResultadoVisita.RECOLECCION_EXITOSA || resultado == ResultadoVisita.CANCELADO) {
				pedido.marcarCompletado(); // lanza excepcion si ya esta completado
			} else if (resultado == ResultadoVisita.RECOLECCION_PARCIAL || resultado == ResultadoVisita.DONANTE_AUSENTE) {
				pedido.marcarEnEjecucion(); // lanza excepcion si ya esta completado
			}
			pedidoDao.update(pedido, conn);
			
			// actualizar estado orden automaticamente (ya fue llamado por marcarCompletado/marcarEnEjecucion)
			ordenDao.update(orden, conn);
			
			conn.commit();
		} catch (SQLException e) { // captura errores SQL
			try {
				if (conn != null) conn.rollback();
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
			throw new RuntimeException("Error registrando visita", e);
		} catch (Exception e) {
			try {
				if (conn != null) conn.rollback();
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
			throw e;
		} finally {
			if (conn != null) {
				try {
					conn.setAutoCommit(true);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			ConnectionManager.disconnect();
		}
	}

	@Override
	public List<VoluntarioDTO> obtenerVoluntarios() { // listado de voluntarios
		Connection conn = null;
		List<VoluntarioDTO> dtos = new ArrayList<>();
		try {
			conn = ConnectionManager.getConnection();
			List<Usuario> voluntarios = usuarioDao.findByRol(2, conn);
			for (Usuario v : voluntarios) {
				dtos.add(new VoluntarioDTO(v.getDni(), v.getNombre(), v.getApellido(), v.getUsuario()));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionManager.disconnect();
		}
		return dtos;
	}
/*
Este metodo al fin anda, ya no carga nulls.
El problema era que no estaba manejando bien las transacciones 
Cargaba como null el vehiculo y el voluntario porque no estaba buscando bien en la base de datos.
*/ 
	@Override
	public void crearOrdenRetiro(List<Integer> idsPedidos, int idVoluntario, String tipoVehiculo)
			throws ReglaNegocioException, ObjetoNuloException {
		Connection conn = null;
		try {
			conn = ConnectionManager.getConnection();
			conn.setAutoCommit(false);
			
			List<PedidosDonacion> pedidos = new ArrayList<>(); // cargar pedidos por ids
			for (Integer idPedido : idsPedidos) {
				PedidosDonacion p = pedidoDao.findById(idPedido, conn);
				if (p != null) {
					pedidos.add(p);
				}
			}
			
			if (pedidos.isEmpty()) {
				throw new ObjetoNuloException("No se encontraron pedidos");
			}
			
			Vehiculo vehiculo = vehiculoDao.findDisponible(tipoVehiculo, conn); // buscar vehiculo disponible
			if (vehiculo == null) {
				throw new ReglaNegocioException("No hay vehiculos disponibles del tipo: " + tipoVehiculo);
			}
			
			List<Usuario> voluntarios = usuarioDao.findByRol(2, conn); // rol voluntario = 2
			Usuario voluntario = null;
			for (Usuario v : voluntarios) { // buscar voluntario por dni
				if (v.getDni() == idVoluntario) {
					voluntario = v;
					break;
				}
			}
			
			if (voluntario == null) {
				throw new ObjetoNuloException("Voluntario no encontrado");
			}
			
			OrdenRetiro orden = new OrdenRetiro(pedidos, null); // crear nueva orden
			orden.asignarVehiculo(vehiculo);
			orden.asignarVoluntario(voluntario);
			
			int idOrdenGenerado = ordenDao.create(orden, conn);
			orden.setId(idOrdenGenerado);
			
			for (PedidosDonacion pedido : pedidos) { // actualizar cada pedido con la orden asignada
				pedido.asignarOrden(orden);
				pedidoDao.update(pedido, conn);
			}
			
			conn.commit();
		} catch (SQLException e) {
			try {
				if (conn != null) conn.rollback();
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
			throw new RuntimeException("Error creando orden de retiro", e); 
		} catch (Exception e) {
			try {
				if (conn != null) conn.rollback();
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
			throw e;
		} finally {
			if (conn != null) {
				try {
					conn.setAutoCommit(true);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			ConnectionManager.disconnect();
		}
	}

	@Override
	public List<OrdenRetiroDTO> obtenerOrdenesAsignadas(String voluntario) { // ordenes asignadas a un voluntario, filtrado por estado y voluntario
		Connection conn = null;
		List<OrdenRetiroDTO> dtos = new ArrayList<>();
		try {
			conn = ConnectionManager.getConnection();
			List<OrdenRetiro> ordenes = ordenDao.findByVoluntario(voluntario, conn);
			for (OrdenRetiro o : ordenes) {
				String nombreVoluntario = "";
				if (o.obtenerVoluntarioPrincipal() != null) {
					Usuario vol = o.obtenerVoluntarioPrincipal();
					nombreVoluntario = vol.getNombre() + " " + vol.getApellido();
				}
				
				String nombreDonante = "";
				if (o.obtenerDonante() != null) {
					Usuario don = o.obtenerDonante();
					nombreDonante = don.getNombre() + " " + don.getApellido();
				}
				
				String descripcionVehiculo = "";
				if (o.obtenerVehiculo() != null) {
					descripcionVehiculo = o.obtenerVehiculo().getDescripcion();
				}
				
				OrdenRetiroDTO dto = new OrdenRetiroDTO(
						o.getId(),
						o.obtenerNombreEstado(),
						o.obtenerFechaCreacion(),
						new ArrayList<VisitaDTO>(),
						nombreDonante,
						descripcionVehiculo,
						nombreVoluntario);
				dtos.add(dto);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionManager.disconnect();
		}
		return dtos;
	}

	@Override
	public List<VisitaDTO> obtenerVisitasPorVoluntario(VoluntarioDTO voluntario) { // visitas realizadas por un voluntario
		Connection conn = null;
		List<VisitaDTO> dtos = new ArrayList<>();
		try {
			conn = ConnectionManager.getConnection();
			
			// encontrar voluntario por DNI
			List<Usuario> voluntarios = usuarioDao.findByRol(2, conn);
			Usuario vol = null;
			for (Usuario u : voluntarios) {
				if (u.getDni() == voluntario.getId()) {
					vol = u;
					break;
				}
			}
			
			if (vol == null) {
				return dtos;
			}
			
			// obtener visitas con sus pedidos asociados
			List<Visita> visitas = visitaDao.findByVoluntario(vol, conn);
			for (Visita v : visitas) {
				String nombreDonante = "";
				
				// obtener donante desde el pedido relacionado
				if (v.getPedidoRelacionado() != null) {
					Usuario donante = v.getPedidoRelacionado().getDonante();
					if (donante != null) {
						nombreDonante = donante.getNombre() + " " + donante.getApellido();
					}
				}
				
				dtos.add(new VisitaDTO(
						v.obtenerFechaFormateada(),
						v.obtenerObservacion(),
						v.obtenerResultado().toString(),
						nombreDonante));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionManager.disconnect();
		}
		return dtos;
	}

	@Override
	public String obtenerNombreDonantePorId(int idPedido) { // obtener nombre completo del donante por id de pedido
		Connection conn = null;
		try {
			conn = ConnectionManager.getConnection();
			PedidosDonacion pedido = pedidoDao.findById(idPedido, conn);
			if (pedido != null && pedido.getDonante() != null) {
				Usuario donante = pedido.getDonante();
				return donante.getNombre() + " " + donante.getApellido();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionManager.disconnect();
		}
		return "";
	}

}
