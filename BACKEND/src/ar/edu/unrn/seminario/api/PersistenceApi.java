package ar.edu.unrn.seminario.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
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
import ar.edu.unrn.seminario.exception.UsuarioInvalidoException;
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
			String apellido, int dni, String direccion) throws CampoVacioException, ObjetoNuloException, UsuarioInvalidoException {
		
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
		} catch (SQLException e) { 
			try {
				if (conn != null) conn.rollback();
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
			
			// si el error es por dni duplicado, lanzamos nuestra excepcion de negocio
			if (e.getMessage().contains("dni_UNIQUE") || e.getMessage().contains("Duplicate entry")) {
                throw new UsuarioInvalidoException("Ya existe un usuario con el DNI " + dni);
			}

			throw new RuntimeException("Error SQL registrando usuario", e);
			
		} catch (CampoVacioException | ObjetoNuloException e) {
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
	}

	@Override
	public void desactivarRol(Integer codigo) {
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
			// Parsear la fecha que VIENE DEL DTO
            java.time.format.DateTimeFormatter formatter = 
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            java.time.LocalDate fechaParsed = 
                java.time.LocalDate.parse(pedidoDTO.getFecha(), formatter);
            
            LocalDateTime fechaDelDTO = fechaParsed.atStartOfDay();
            
            PedidosDonacion pedido = new PedidosDonacion(
					fechaDelDTO, // <-- AHORA USA LA FECHA DEL DTO
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
			
		} catch (CampoVacioException | ObjetoNuloException e) {
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
	public void registrarVisita(int idOrdenRetiro, int idPedido, java.time.LocalDateTime fechaHora, String resultado, String observacion)
	        throws ObjetoNuloException, CampoVacioException, ReglaNegocioException {

	    Connection conn = null;
	    try {
	        conn = ConnectionManager.getConnection();
	        conn.setAutoCommit(false);

	        // traer pedido y validar pertenencia a la orden en una sola consulta
	        PedidosDonacion pedido = pedidoDao.findByIdAndOrden(idPedido, idOrdenRetiro, conn);
	        if (pedido == null) {
	            throw new ObjetoNuloException("Pedido " + idPedido + " no pertenece a la orden " + idOrdenRetiro);
	        }

	        // la orden ya esta referenciada en el pedido (con su ID)
	        OrdenRetiro orden = pedido.obtenerOrden();
	        if (orden == null) {
	            throw new ObjetoNuloException("Orden no encontrada con ID: " + idOrdenRetiro);
	        }

	        // logica de negocio - construccion de la entidad desde datos primitivos
	        ResultadoVisita resultadoEnum = ResultadoVisita.fromString(resultado);
	        Visita visita = new Visita(fechaHora, resultadoEnum, observacion);
	        visita.setPedidoRelacionado(pedido);

	        // escritura
	        visitaDao.create(visita, idOrdenRetiro, idPedido, conn);

	        // actualizar estado del pedido segun resultado
	        if (resultadoEnum == ResultadoVisita.RECOLECCION_EXITOSA || resultadoEnum == ResultadoVisita.CANCELADO) {
	            pedido.marcarCompletado();
	        } else if (resultadoEnum == ResultadoVisita.RECOLECCION_PARCIAL || resultadoEnum == ResultadoVisita.DONANTE_AUSENTE) {
	            pedido.marcarEnEjecucion();
	        }
	        pedidoDao.update(pedido, conn);

	        // actualizar estado de la orden
	        ordenDao.update(orden, conn);

	        conn.commit();

	    } catch (SQLException e) {
	        if (conn != null) {
	            try {
	                conn.rollback();
	            } catch (SQLException e2) {
	                e2.printStackTrace();
	            }
	        }
	        throw new RuntimeException("Error de base de datos al registrar la visita: " + e.getMessage(), e);

	    } catch (ReglaNegocioException | ObjetoNuloException | CampoVacioException e) {
	        if (conn != null) {
	            try {
	                conn.rollback();
	            } catch (SQLException e2) {
	                e2.printStackTrace();
	            }
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
	public void crearOrdenRetiro(List<Integer> idsPedidos, int idVoluntario, String tipoVehiculo)
	        throws ReglaNegocioException, ObjetoNuloException {
	    
	    Connection conn = null;
	    try {
	        conn = ConnectionManager.getConnection();
	        conn.setAutoCommit(false); // Inicia la transacción

	        // Un solo SELECT para todos los pedidos
	        List<PedidosDonacion> pedidos = pedidoDao.findByIds(idsPedidos, conn);
	        
	        if (pedidos == null || pedidos.isEmpty() || pedidos.size() != idsPedidos.size()) {
	            throw new ObjetoNuloException("No se encontraron todos los pedidos solicitados. Verifique los IDs.");
	        }
	        
	        // Validar que los pedidos estén pendientes
	        for (PedidosDonacion p : pedidos) {
	            if (p.obtenerOrden() != null) {
	                throw new ReglaNegocioException("El pedido " + p.getId() + " ya pertenece a otra orden.");
	            }
	        }

	        Vehiculo vehiculo = vehiculoDao.findDisponible(tipoVehiculo, conn);
	        if (vehiculo == null) {
	            throw new ReglaNegocioException("No hay vehiculos disponibles del tipo: " + tipoVehiculo);
	        }

	        // Un solo SELECT para el voluntario específico
	        Usuario voluntario = usuarioDao.findByDni(idVoluntario, conn);

	        if (voluntario == null) {
	            throw new ObjetoNuloException("Voluntario no encontrado con DNI: " + idVoluntario);
	        }
	        
	        if (voluntario.getRol().getCodigo() != 2) { 
	            throw new ReglaNegocioException("El DNI " + idVoluntario + " no pertenece a un voluntario.");
	        }

	        // Crear la entidad OrdenRetiro
	        OrdenRetiro orden = new OrdenRetiro(pedidos, null);
	        orden.asignarVehiculo(vehiculo);
	        orden.asignarVoluntario(voluntario);

	        // Crear la orden en la BD para obtener su ID
	        int idOrdenGenerado = ordenDao.create(orden, conn);
	        orden.setId(idOrdenGenerado);

	        // Un solo BATCH UPDATE para todos los pedidos
	        // Usamos try para asegurar que el PreparedStatement se cierre
	        String sqlUpdate = "UPDATE pedidos_donacion SET estado = ?, id_orden_retiro = ? WHERE id = ?";
	        try (PreparedStatement updateStmt = conn.prepareStatement(sqlUpdate)) {
	            
	            for (PedidosDonacion pedido : pedidos) {
	                pedido.asignarOrden(orden); // Asigna la orden en el objeto Java
	                
	                // Setea los parámetros para el batch
	                updateStmt.setString(1, pedido.obtenerEstado()); // Sigue "PENDIENTE"
	                updateStmt.setInt(2, idOrdenGenerado);
	                updateStmt.setInt(3, pedido.getId());
	                
	                // Agrega la operación al lote
	                updateStmt.addBatch();
	            }
	            
	            // Ejecuta todas las operaciones de UPDATE en una sola llamada
	            updateStmt.executeBatch();
	        }

	        conn.commit();
	        
	    } catch (SQLException e) {
	        try {
	            if (conn != null) conn.rollback();
	        } catch (SQLException e2) {
	            e2.printStackTrace();
	        }
	        throw new RuntimeException("Error SQL al crear orden de retiro", e);
	    } catch (ReglaNegocioException | ObjetoNuloException e) {
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
