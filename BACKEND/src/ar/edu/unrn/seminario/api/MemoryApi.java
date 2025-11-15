package ar.edu.unrn.seminario.api;

import java.util.ArrayList;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.LocalDateTime;

import ar.edu.unrn.seminario.dto.RolDTO;
import ar.edu.unrn.seminario.dto.UsuarioDTO;
import ar.edu.unrn.seminario.dto.PedidoDonacionDTO;
import ar.edu.unrn.seminario.dto.DonanteDTO;
import ar.edu.unrn.seminario.dto.OrdenRetiroDTO;
import ar.edu.unrn.seminario.dto.VisitaDTO;
import ar.edu.unrn.seminario.dto.BienDTO;
import ar.edu.unrn.seminario.dto.VoluntarioDTO;
import ar.edu.unrn.seminario.exception.CampoVacioException;
import ar.edu.unrn.seminario.exception.ObjetoNuloException;
import ar.edu.unrn.seminario.exception.ReglaNegocioException;
import ar.edu.unrn.seminario.modelo.Bien;
import ar.edu.unrn.seminario.modelo.OrdenRetiro;
import ar.edu.unrn.seminario.modelo.PedidosDonacion;
import ar.edu.unrn.seminario.modelo.Rol;
import ar.edu.unrn.seminario.modelo.Usuario;
import ar.edu.unrn.seminario.modelo.Visita;
import ar.edu.unrn.seminario.modelo.Vehiculo;
import ar.edu.unrn.seminario.modelo.ResultadoVisita;

public class MemoryApi implements IApi {

	// Atributos
	private ArrayList<Rol> roles = new ArrayList<>();
	private ArrayList<Usuario> usuarios = new ArrayList<>();
	private List<PedidosDonacion> pedidos = new ArrayList<>();
	private List<OrdenRetiro> ordenes = new ArrayList<>();
	private ArrayList<Vehiculo> vehiculosDisponibles = new ArrayList<>(); // lista para autos fijos

	// Constructores
	public MemoryApi() throws CampoVacioException {
		this.roles.add(new Rol(1, "ADMIN"));
		this.roles.add(new Rol(2, "VOLUNTARIO"));
		this.roles.add(new Rol(3, "DONANTE"));
		
		// inicio en orden
		inicializarUsuarios(); //  incluye todos los usuarios (admin, donantes, voluntarios)
		inicializarVehiculos(); 
		inicializarPedidos(); // crea pedidos, sin asignar ordenes
	}

	// Metodos
	private void inicializarUsuarios() {
		try {
			// ADMIN (sin direccinn)
			registrarUsuario("admin", "1234", "admin@unrn.edu.ar", "Admin", 1, "Sistema", 11111111, null);
			
			// VOLUNTARIOS (sin dirección - no la necesitan)
			registrarUsuario("clopez", "pass", "clopez@unrn.edu.ar", "Carlos", 2, "Lopez", 22222222, null);
			registrarUsuario("amartinez", "pass", "amartinez@unrn.edu.ar", "Ana", 2, "Martinez", 33333333, null);
			registrarUsuario("lgomez", "pass", "lgomez@unrn.edu.ar", "Luis", 2, "Gomez", 44444444, null);
			
			// DONANTES (CON direccion, la necesitan, sale en tablas)
			registrarUsuario("jperez", "pass", "jperez@unrn.edu.ar", "Juan", 3, "Perez", 55555555, "Calle Falsa 123, Centro");
			registrarUsuario("mgomez", "pass", "mgomez@unrn.edu.ar", "Maria", 3, "Gomez", 66666666, "Avenida Siempre Viva 742, Sur");
		} catch (CampoVacioException | ObjetoNuloException e) {
			e.printStackTrace();
		}
	}

	private void inicializarPedidos() {
		try {
			//  donantes por DNI
			Usuario donante1 = buscarUsuarioPorDni(55555555); // Juan Perez
			Usuario donante2 = buscarUsuarioPorDni(66666666); // Maria Gomez

			if (donante1 == null || donante2 == null) {
				System.err.println("Error: No se encontraron los donantes para inicializar pedidos");
				return;
			}

			Vehiculo vehiculoCamioneta = new Vehiculo("TEMP-CAMIONETA", "Disponible", "Camioneta", 1000);
			Vehiculo vehiculoCamion = new Vehiculo("TEMP-CAMION", "Disponible", "Camion", 5000);
			Vehiculo vehiculoAuto = new Vehiculo("TEMP-AUTO", "Disponible", "Auto", 500);

			// PEDIDO 1 (Donante Juan Perez)
			List<Bien> bienes1 = new ArrayList<>();
			bienes1.add(new Bien(1, 10, 2, vehiculoCamioneta)); 
			this.pedidos.add(new PedidosDonacion(LocalDateTime.now(), new ArrayList<>(bienes1), "Camioneta", donante1));

			// PEDIDO 2 (Donante Maria Gomez)
			List<Bien> bienes2 = new ArrayList<>();
			bienes2.add(new Bien(2, 5, 1, vehiculoCamion)); 
			bienes2.add(new Bien(3, 1, 3, vehiculoCamion));
			LocalDateTime fecha2 = LocalDateTime.now().minusDays(1); 
			this.pedidos.add(new PedidosDonacion(fecha2, new ArrayList<>(bienes2), "Camion", donante2));

			// PEDIDO 3 (Donante Juan Perez)
			List<Bien> bienes3 = new ArrayList<>();
			bienes3.add(new Bien(4, 15, 2, vehiculoAuto)); 
			LocalDateTime fecha3 = LocalDateTime.now().minusDays(2); 
			this.pedidos.add(new PedidosDonacion(fecha3, new ArrayList<>(bienes3), "Auto", donante1));
			
		} catch (CampoVacioException | ObjetoNuloException e) {
			e.printStackTrace();
		}
	}

	private void inicializarVehiculos() {
	    // autos fijos 
	    this.vehiculosDisponibles.add(new Vehiculo("AE 123 CD", "Disponible", "Auto", 500)); // Auto
	    this.vehiculosDisponibles.add(new Vehiculo("AD 456 EF", "Disponible", "Camioneta", 1500)); // Camioneta
	    this.vehiculosDisponibles.add(new Vehiculo("AA 789 GH", "Disponible", "Camion", 4000)); // Camion
	}
	
	public void registrarUsuario(String username, String password, String email, String nombre, Integer rol, String apellido, int dni, String direccion) throws CampoVacioException, ObjetoNuloException {
		Rol role = this.buscarRol(rol);
		Usuario usuario = new Usuario(username, password, nombre, email, role, apellido, dni, direccion);
		this.usuarios.add(usuario);
	}

	public List<UsuarioDTO> obtenerUsuarios() {
		List<UsuarioDTO> dtos = new ArrayList<>();
		for (Usuario u : this.usuarios) {
			dtos.add(new UsuarioDTO(u.getUsuario(), u.getContrasena(), u.getNombre(), u.getEmail(),
					u.getRol().getNombre(), u.isActivo(), u.obtenerEstado()));
		}
		return dtos;
	}

	public UsuarioDTO obtenerUsuario(String username) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void eliminarUsuario(String username) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<RolDTO> obtenerRoles() {
		List<RolDTO> dtos = new ArrayList<>();
		for (Rol r : this.roles) {
			dtos.add(new RolDTO(r.getCodigo(), r.getNombre()));
		}
		return dtos;
	}

	@Override
	public List<RolDTO> obtenerRolesActivos() {
		List<RolDTO> dtos = new ArrayList<>();
		for (Rol r : this.roles) {
			if (r.isActivo())
				dtos.add(new RolDTO(r.getCodigo(), r.getNombre()));
		}
		return dtos;
	}

	@Override
	public void guardarRol(Integer codigo, String descripcion, boolean estado) throws CampoVacioException {
		Rol rol = new Rol(codigo, descripcion);
		this.roles.add(rol);
	}

	@Override
	public RolDTO obtenerRolPorCodigo(Integer codigo) {
		// TODO Auto-generated method stub
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
	public void activarUsuario(String usuario) {
		Usuario user = this.buscarUsuario(usuario);
		user.activar();
	}

	@Override
	public void desactivarUsuario(String usuario) {
		Usuario user = this.buscarUsuario(usuario);
		user.desactivar();
	}

	@Override
	public void registrarPedidoDonacion(PedidoDonacionDTO pedidoDTO) throws CampoVacioException, ObjetoNuloException {
	    // busacr usuario donante por DNI
		Usuario donante = buscarUsuarioPorDni(pedidoDTO.getDonanteId());
	    if (donante == null) {
	        throw new ObjetoNuloException("Donante no encontrado.");
	    }
		// revisar que el usuario tenga el rol de Donante
		if (donante.getRol().getCodigo() != 3) {
			throw new ObjetoNuloException("El usuario no tiene el rol de Donante.");
		}
		
	    ArrayList<Bien> bienes = new ArrayList<>();
	    Vehiculo vehiculo = new Vehiculo("XYZ123", "Disponible", "Camioneta", 1000);
	    final int CATEGORIA_CALIDAD_POR_DEFECTO = 2; 
	    for (BienDTO bienDTO : pedidoDTO.getBienes()) {
	        bienes.add(new Bien(bienDTO.getId(), bienDTO.getCantidad(), CATEGORIA_CALIDAD_POR_DEFECTO, vehiculo));
	    }

	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	    LocalDateTime fechaLocalDateTime = LocalDate.parse(pedidoDTO.getFecha(), formatter).atStartOfDay();

		// crea el pedido directamente 
	    PedidosDonacion pedido = new PedidosDonacion(fechaLocalDateTime, bienes, pedidoDTO.getTipoVehiculo(), donante);
	    this.pedidos.add(pedido);
	}

	// Getters
	@Override
	public List<DonanteDTO> obtenerDonantes() {
		List<DonanteDTO> donanteDTOs = new ArrayList<>();
		// filtra usuarios ACTIVOS con rol DONANTE ( 3)
		for (Usuario usuario : this.usuarios) {
			if (usuario.getRol().getCodigo() == 3 && usuario.isActivo()) {
				donanteDTOs.add(new DonanteDTO(usuario.obtenerDni(), usuario.obtenerNombre(), usuario.obtenerApellido()));
			}
		}
		return donanteDTOs;
	}

	@Override
	public List<PedidoDonacionDTO> obtenerPedidosPendientes() {
		List<PedidoDonacionDTO> pendientes = new ArrayList<>();

		for (PedidosDonacion pedido : pedidos) {
			if (pedido.obtenerOrden() == null) {
				DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("dd/MM/yyyy");
				String formattedDate = pedido.obtenerFecha().format(formatter2);

				List<String> bienes = new ArrayList<>();
				for (Bien bien : pedido.obtenerBienes()) {
					bienes.add(bien.toString()); // nomanejamos logica de bienes por ahora
				}

				// obtener nombre completo del donante
				String nombreDonante = pedido.obtenerDonante().obtenerNombre() + " " + pedido.obtenerDonante().obtenerApellido();

				pendientes.add(new PedidoDonacionDTO(
					pedido.obtenerId(),
					formattedDate, // usar la fecha formateada como string
					pedido.describirTipoVehiculo(),
					pedido.obtenerDonante().obtenerDni(),
					nombreDonante // nombre completo del donante
				));
			}
		}
		return pendientes; // devuelve lista
	}

	@Override
	public List<PedidoDonacionDTO> obtenerTodosPedidos() {
		List<PedidoDonacionDTO> todos = new ArrayList<>();

		for (PedidosDonacion pedido : pedidos) {
			DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("dd/MM/yyyy");
			String formattedDate = pedido.obtenerFecha().format(formatter2);

			String nombreDonante = pedido.obtenerDonante().obtenerNombre() + " " + pedido.obtenerDonante().obtenerApellido();

			todos.add(new PedidoDonacionDTO(
				pedido.obtenerId(),
				formattedDate,
				pedido.describirTipoVehiculo(),
				pedido.obtenerDonante().obtenerDni(),
				nombreDonante,
				pedido.obtenerEstado()
			));
		}
		return todos;
	}

	@Override
	public List<OrdenRetiroDTO> obtenerOrdenesDeRetiro(String estado) {
		List<OrdenRetiroDTO> ordenesFiltradas = new ArrayList<>();
		for (OrdenRetiro orden : ordenes) {
			if (orden.describirEstado().equalsIgnoreCase(estado)) {
				List<VisitaDTO> visitasDTO = new ArrayList<>();
				for (Visita visita : orden.obtenerVisitas()) {
				visitasDTO.add(new VisitaDTO(visita.obtenerFechaFormateada(), visita.obtenerObservacion()));
			}
			String donante = orden.obtenerDonante() != null ? orden.obtenerDonante().getNombre() : "Donante Desconocido";
			String vehiculo = orden.obtenerVehiculo() != null ? orden.obtenerVehiculo().getDescripcion() : "Vehiculo Desconocido";
			String voluntario = orden.obtenerVoluntarioPrincipal() != null ? orden.obtenerVoluntarioPrincipal().obtenerNombre() : "Voluntario Desconocido";
			ordenesFiltradas.add(new OrdenRetiroDTO(orden.obtenerId(), orden.obtenerEstadoOrden().toString(), orden.obtenerFechaCreacion(), visitasDTO, donante, vehiculo, voluntario));
		}
	}
	return ordenesFiltradas;
	}

	@Override
	public List<OrdenRetiroDTO> obtenerTodasOrdenesRetiro() {
		List<OrdenRetiroDTO> todasOrdenes = new ArrayList<>();
		for (OrdenRetiro orden : ordenes) {
			List<VisitaDTO> visitasDTO = new ArrayList<>();
			for (Visita visita : orden.obtenerVisitas()) {
				visitasDTO.add(new VisitaDTO(visita.obtenerFechaFormateada(), visita.obtenerObservacion()));
			}
			String donante = orden.obtenerDonante() != null ? orden.obtenerDonante().getNombre() : "Donante Desconocido";
			String vehiculo = orden.obtenerVehiculo() != null ? orden.obtenerVehiculo().getDescripcion() : "Vehiculo Desconocido";
			String voluntario = orden.obtenerVoluntarioPrincipal() != null ? orden.obtenerVoluntarioPrincipal().obtenerNombre() : "Voluntario Desconocido";
			todasOrdenes.add(new OrdenRetiroDTO(orden.obtenerId(), orden.obtenerEstadoOrden().toString(), orden.obtenerFechaCreacion(), visitasDTO, donante, vehiculo, voluntario));
		}
		return todasOrdenes;
	}
	
	@Override
	public List<VoluntarioDTO> obtenerVoluntarios() {
		List<VoluntarioDTO> voluntariosDTO = new ArrayList<>();
		// filtra usuarios ACTIVOS con rol VOLUNTARIO ( 2)
		for (Usuario usuario : this.usuarios) {
			if (usuario.getRol().getCodigo() == 2 && usuario.isActivo()) {
				voluntariosDTO.add(new VoluntarioDTO(usuario.obtenerDni(), usuario.obtenerNombre(), usuario.obtenerApellido(), usuario.getUsuario()));
			}
		}
		return voluntariosDTO;
	}

	@Override
	public List<OrdenRetiroDTO> obtenerOrdenesAsignadas(String nombreVoluntario) {
		List<OrdenRetiroDTO> ordenesAsignadas = new ArrayList<>();
		
		// el parametro es el username del voluntario (ej: "clopez", "bgoro")
		String usernameBuscado = nombreVoluntario != null ? nombreVoluntario.trim() : "";
		
		// FILTRAR SOLO las ordenes asignadas al voluntario especificado
		for (OrdenRetiro orden : this.ordenes) {
			Usuario voluntarioAsignado = orden.getVoluntario();
			
			// validar que la orden tenga un voluntario asignado
			if (voluntarioAsignado != null) {
				String usernameAsignado = voluntarioAsignado.getUsuario().trim();
				
				// comparar usernames (case-insensitive)
				if (usernameAsignado.equalsIgnoreCase(usernameBuscado)) {
					int idOrden = orden.getId();
					String nombreCompleto = voluntarioAsignado.getNombre() + " " + voluntarioAsignado.getApellido();
					
					ordenesAsignadas.add(new OrdenRetiroDTO(
						idOrden,
						orden.obtenerEstadoOrden().toString(), // Enum a String
						orden.getFechaCreacion(),
						null, // visitas no incluidas aqui
						orden.getDonante() != null ? orden.getDonante().getNombre() : "Sin Donante",
						orden.getVehiculo() != null ? orden.getVehiculo().getPatente() : "Sin Vehiculo",
						nombreCompleto
					));
				}
			}
		}
		
		return ordenesAsignadas;
	}
	
	@Override
	public List<PedidoDonacionDTO> obtenerPedidosDeOrden(int idOrden) {
		List<PedidoDonacionDTO> pedidosDTO = new ArrayList<>();
		
		OrdenRetiro orden = buscarOrdenPorId(idOrden);
		if (orden == null) {
			return pedidosDTO; // vacia si no existe la orden
		}
		
		for (PedidosDonacion pedido : orden.obtenerPedidos()) {
			PedidoDonacionDTO dto = this.convertirPedidoADTO(pedido);
			if (dto != null) {
				pedidosDTO.add(dto);
			}
		}
		
		return pedidosDTO;
	}
	
	@Override
	public List<VisitaDTO> obtenerVisitasPorVoluntario(VoluntarioDTO voluntario) {
		List<VisitaDTO> visitas = new ArrayList<>();
		for (OrdenRetiro orden : this.ordenes) {
			if (orden.getVoluntario() != null && orden.getVoluntario().obtenerNombre().equals(voluntario.getNombre())) { // MODIFICADO: usar obtenerNombre()
				
				for (Visita visita : orden.obtenerVisitas()) {
					// obtener el donante desde el pedido relacionado con la visita
					String nombreDonante = "Sin datos";
					if (visita.getPedidoRelacionado() != null && visita.getPedidoRelacionado().getDonante() != null) {
						Usuario donante = visita.getPedidoRelacionado().getDonante(); // MODIFICADO: ahora es Usuario
						nombreDonante = donante.obtenerNombre() + " " + donante.obtenerApellido();
						
						// DEBUG
						// System.out.println("DEBUG. Visita: " + visita.obtenerObservacion() +" Donante: " + nombreDonante);
					} else {
						System.out.println("DEBUG Visita SIN pedido relacionado: " + visita.obtenerObservacion());
					}
					
					// crear DTO con todos los datos relevantes
					VisitaDTO visitaDTO = new VisitaDTO(
						visita.obtenerFechaFormateada(),
						visita.obtenerObservacion(), visita.obtenerResultado().toString(), // Enum a String
						nombreDonante // nombre completo del donante especifico de esta visita
					);
					visitas.add(visitaDTO);
				}
			}
		}
		return visitas;
	}

	@Override
	public String obtenerNombreDonantePorId(int idPedido) {
        for (PedidosDonacion pedido : this.pedidos) {
            if (pedido.getId() == idPedido && pedido.getDonante() != null) {
                return pedido.getDonante().getNombre();
            }
        }
        return "Donante Desconocido";
    }

	public void registrarVisita(int idOrdenRetiro, int idPedido, VisitaDTO visitaDTO) throws ObjetoNuloException, CampoVacioException, ReglaNegocioException {
	    
	    // --- 1. Validaciones de Objeto Nulo y Existencia ---
	    OrdenRetiro orden = buscarOrdenPorId(idOrdenRetiro);
	    if (orden == null) {
	        throw new ObjetoNuloException("Orden de retiro no encontrada.");
	    }

	    PedidosDonacion pedido = orden.obtenerPedidoPorId(idPedido);
	    if (pedido == null) {
	        throw new ReglaNegocioException("El pedido con ID " + idPedido + " no pertenece a la orden " + idOrdenRetiro);
	    }
	    
	    // --- 2. Validaciones de DTO (Campos Vacíos y Nulos) ---
	    if (visitaDTO.getFechaHora() == null) {
	        throw new CampoVacioException("La fecha de la visita no puede ser nula.");
	    }
	    if (visitaDTO.getResultado() == null || visitaDTO.getResultado().isEmpty()) {
	        throw new CampoVacioException("El resultado de la visita no puede ser nulo o vacío.");
	    }
	    if (visitaDTO.getObservacion() == null || visitaDTO.getObservacion().trim().isEmpty()) {
	        throw new CampoVacioException("Las observaciones de la visita no pueden ser nulas o vacías.");
	    }
	    
	    // --- 3. Validaciones de Reglas de Negocio (Movidas al Back-end) ---
	    
	    // Regla 1: La visita no puede ser en el futuro
	    if (visitaDTO.getFechaHora().isAfter(LocalDateTime.now())) {
	        throw new ReglaNegocioException("La fecha y hora de la visita no pueden ser posteriores al momento actual.");
	    }
	    
	    // Conversión de String del DTO al Enum
	    ResultadoVisita resultado = ResultadoVisita.fromString(visitaDTO.getResultado());

	    // --- 4. Creación de la Entidad y Lógica de Dominio ---
	    
	    // Creación de la entidad Visita
	    LocalDateTime fechaVisita = visitaDTO.getFechaHora();
	    Visita visita = new Visita(fechaVisita, resultado, visitaDTO.getObservacion());

	    // Establecer la relación entre la visita y el pedido
	    visita.setPedidoRelacionado(pedido);

	    // Sumar la visita a la orden (el agregado de la visita debe ocurrir antes de actualizar el estado)
	    orden.agregarVisita(visita);

	    // Actualiza el estado del pedido según el resultado de visita
	    // Nota: Es mejor mover esta lógica de actualización del estado a un método dentro de la entidad 'PedidosDonacion'
	    // para que sea más coherente con el modelo de dominio.
	    if (resultado == ResultadoVisita.RECOLECCION_EXITOSA || resultado == ResultadoVisita.CANCELADO) {
	        pedido.marcarCompletado();
	    } else if (resultado == ResultadoVisita.RECOLECCION_PARCIAL || resultado == ResultadoVisita.DONANTE_AUSENTE) {
	        pedido.marcarEnEjecucion();
	    }

	    // Se asume que marcarCompletado() o marcarEnEjecucion() notifica automáticamente a la orden
	    // para que actualice su propio estado (ej. si todos los pedidos están completos, la orden se completa).
	    
	    // Si fuera necesario guardar los cambios en la base de datos (persistencia), la llamada al repositorio iría aquí.
	    // Ejemplo: ordenRetiroRepository.actualizar(orden);
	}
	
	private Rol buscarRol(Integer codigo) {
		for (Rol rol : roles) {
			if (rol.getCodigo().equals(codigo))
				return rol;
		}
		return null;
	}

	private Usuario buscarUsuario(String usuario) {
		for (Usuario user : usuarios) {
			if (user.getUsuario().equals(usuario))
				return user;
		}
		return null;
	}

	//  buscar usuarios por DNI (reemplaza metodos deprecated)
	private Usuario buscarUsuarioPorDni(int dni) {
		for (Usuario usuario : this.usuarios) {
			if (usuario.obtenerDni() == dni) {
				return usuario;
			}
		}
		return null;
	}

	private PedidosDonacion buscarPedidoPorId(int id) {
		for (PedidosDonacion pedido : pedidos) {
			if (pedido.obtenerId() == id) {
				return pedido;
			}
		}
		return null;
	}

	private OrdenRetiro buscarOrdenPorId(int id) {
		for (OrdenRetiro orden : ordenes) {
			if (orden.obtenerId() == id) {
				return orden;
			}
		}
		return null;
	}

	@Override
	public void crearOrdenRetiro(List<Integer> idsPedidos, int idVoluntario, String tipoVehiculo) throws ReglaNegocioException, ObjetoNuloException {
		// busca usuario voluntario por DNI
		Usuario voluntario = buscarUsuarioPorDni(idVoluntario);
		if (voluntario == null) {
			throw new ObjetoNuloException("Voluntario no encontrado.");
		}
		// revisa que el usuario tenga el rol de Voluntario
		if (voluntario.getRol().getCodigo() != 2) {
			throw new ObjetoNuloException("El usuario no tiene el rol de Voluntario.");
		}

		List<PedidosDonacion> pedidosAAsignar = new ArrayList<>();
		for (Integer idPedido : idsPedidos) {
			PedidosDonacion pedido = buscarPedidoPorId(idPedido);
			if (pedido == null) {
				throw new ObjetoNuloException("Pedido no encontrado.");
			}
			if (pedido.obtenerOrden() != null) {
				throw new ReglaNegocioException("El pedido ya tiene una orden asignada.");
			}
			pedidosAAsignar.add(pedido);
		}

		// orden con la lista de pedidos (destino ya no usa Ubicacion, simplificado)
		OrdenRetiro nuevaOrden = new OrdenRetiro(pedidosAAsignar, null);

		// asignar el voluntario
		nuevaOrden.asignarVoluntario(voluntario);

		// busca auto del tipo de la lista fija
		Vehiculo vehiculoAsignado = null;
		for (Vehiculo v : vehiculosDisponibles) {
			if (v.getTipoVeh().equalsIgnoreCase(tipoVehiculo) && v.getEstado().equals("Disponible")) {
				vehiculoAsignado = v;
				break; // el primer vehiculo disponible encontrado
			}
		}

		nuevaOrden.asignarVehiculo(vehiculoAsignado);

		// agrega la orden a la lista
		this.ordenes.add(nuevaOrden);
	}

	private List<String> convertirBienesAStrings(List<Bien> bienes) {//no manejamos logica de bienes, quedo de antes
        List<String> bienesStr = new ArrayList<>();
        for (Bien bien : bienes) {
            bienesStr.add(bien.toString()); 
        }
        return bienesStr;
    }
	
	private PedidoDonacionDTO convertirPedidoADTO(PedidosDonacion pedido) {
	    if (pedido == null) {
	        return null;
	    }
	    Usuario donante = pedido.obtenerDonante(); // ahora es Usuario
	    if (donante == null) {
	        System.out.println("Advertencia: El pedido " + pedido.obtenerId() + " no tiene un donante asociado.");
	        return null;
	    }

	    return new PedidoDonacionDTO(
	        pedido.obtenerId(),
	        donante.obtenerNombre(),
	        donante.obtenerDireccion() != null ? donante.obtenerDireccion() : "Sin direccion",
	        pedido.obtenerEstado()
	    );
	}
		
}