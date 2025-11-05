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
import ar.edu.unrn.seminario.modelo.Donante;
import ar.edu.unrn.seminario.modelo.OrdenRetiro;
import ar.edu.unrn.seminario.modelo.PedidosDonacion;
import ar.edu.unrn.seminario.modelo.Rol;
import ar.edu.unrn.seminario.modelo.Usuario;
import ar.edu.unrn.seminario.modelo.Visita;
import ar.edu.unrn.seminario.modelo.Voluntario;
import ar.edu.unrn.seminario.modelo.Ubicacion;
import ar.edu.unrn.seminario.modelo.Vehiculo;

public class MemoryApi implements IApi {

	private ArrayList<Rol> roles = new ArrayList<>();
	private ArrayList<Usuario> usuarios = new ArrayList<>();
	private ArrayList<Donante> donantes = new ArrayList<>();
	private List<PedidosDonacion> pedidos = new ArrayList<>();
	private List<OrdenRetiro> ordenes = new ArrayList<>();
	private ArrayList<Voluntario> voluntarios = new ArrayList<>();
	private ArrayList<Vehiculo> vehiculosDisponibles = new ArrayList<>(); // lista para autos fijos

	public MemoryApi() throws CampoVacioException {
		this.roles.add(new Rol(1, "ADMIN"));
		this.roles.add(new Rol(2, "ESTUDIANTE"));
		this.roles.add(new Rol(3, "INVITADO"));
		
		// inicio en orden
		inicializarUsuarios();
		inicializarDonantes();
		inicializarVoluntarios();
		inicializarVehiculos(); 
		inicializarPedidos(); // crea pedidos, sin asignar ordenes
	}

	private void inicializarUsuarios() {
		try {
			registrarUsuario("admin", "1234", "admin@unrn.edu.ar", "Admin", 1);
			registrarUsuario("ml", "4", "ml@unrn.edu.ar", "Mauro", 2);
			registrarUsuario("ra", "1234", "ra@unrn.edu.ar", "Ramiro", 3);
		} catch (CampoVacioException | ObjetoNuloException e) {
			e.printStackTrace();
		}
	}

	private void inicializarDonantes() {
		try {
			this.donantes.add(new Donante("Juan", "Perez", 12345678, "Calle Falsa 123", "Centro", "Barrio Norte", -40.0, -65.0));
			this.donantes.add(new Donante("Maria", "Gomez", 87654321, "Avenida Siempre Viva 742", "Sur", "Barrio Sur", -41.0, -66.0));
		} catch (CampoVacioException | ObjetoNuloException e) {
			e.printStackTrace();
		}
	}
	private void inicializarPedidos() {
		try {
			Donante donante1 = this.donantes.get(0); // Juan Perez
			Donante donante2 = this.donantes.get(1); // Maria Gomez

			Vehiculo vehiculoCamioneta = new Vehiculo("TEMP-CAMIONETA", "Disponible", "Camioneta", 1000);
			Vehiculo vehiculoCamion = new Vehiculo("TEMP-CAMION", "Disponible", "Camión", 5000);
			Vehiculo vehiculoAuto = new Vehiculo("TEMP-AUTO", "Disponible", "Auto", 500);

			// PEDIDO 1 (Donante Juan Perez)
			List<Bien> bienes1 = new ArrayList<>();
			bienes1.add(new Bien(1, 10, 2, vehiculoCamioneta)); 
			this.pedidos.add(new PedidosDonacion(LocalDateTime.now(), new ArrayList<>(bienes1), "Camioneta", "Pedido 1: Sin observaciones", donante1));

			// PEDIDO 2 (Donante Maria Gomez)
			if (this.donantes.size() > 1) {
				List<Bien> bienes2 = new ArrayList<>();
				bienes2.add(new Bien(2, 5, 1, vehiculoCamion)); 
				bienes2.add(new Bien(3, 1, 3, vehiculoCamion));
				LocalDateTime fecha2 = LocalDateTime.now().minusDays(1); 
				this.pedidos.add(new PedidosDonacion(fecha2, new ArrayList<>(bienes2), "Camión", "Pedido 2: Mueble grande", donante2));
			}

			// PEDIDO 3 (Donante Juan Perez)
			List<Bien> bienes3 = new ArrayList<>();
			bienes3.add(new Bien(4, 15, 2, vehiculoAuto)); 
			LocalDateTime fecha3 = LocalDateTime.now().minusDays(2); 
			this.pedidos.add(new PedidosDonacion(fecha3, new ArrayList<>(bienes3), "Auto", "Pedido 3: Cajas pequeñas", donante1));
			
			
		} catch (CampoVacioException | ObjetoNuloException e) {
			e.printStackTrace();
		}
	}

	private void inicializarVoluntarios() {
			try {
				// DNI 12345678
				this.voluntarios.add(new Voluntario("Carlos", "Lopez", 12345678, new Ubicacion("Calle Falsa 123", "Zona Norte", "Barrio Norte", 0.0, 0.0)));
				// DNI 87654321
				this.voluntarios.add(new Voluntario("Ana", "Martinez", 87654321, new Ubicacion("Calle Verdadera 456", "Zona Sur", "Barrio Sur", 0.0, 0.0)));
				// DNI 11223344
				this.voluntarios.add(new Voluntario("Luis", "Gomez", 11223344, new Ubicacion("Avenida Siempre Viva", "Zona Centro", "Barrio Centro", 0.0, 0.0)));
			} catch (CampoVacioException | ObjetoNuloException e) {
				e.printStackTrace();
			}
	}

	private void inicializarVehiculos() {
	    // autos fijos 
	    this.vehiculosDisponibles.add(new Vehiculo("AE 123 CD", "Disponible", "Auto", 500)); // Auto
	    this.vehiculosDisponibles.add(new Vehiculo("AD 456 EF", "Disponible", "Camioneta", 1500)); // Camioneta
	    this.vehiculosDisponibles.add(new Vehiculo("AA 789 GH", "Disponible", "Camión", 4000)); // Camión
	}
	public void registrarUsuario(String username, String password, String email, String nombre, Integer rol) throws CampoVacioException, ObjetoNuloException {

		Rol role = this.buscarRol(rol);
		Usuario usuario = new Usuario(username, password, nombre, email, role);
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
		// TODO Auto-generated method stub
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
	    Donante donante = buscarDonantePorId(pedidoDTO.getDonanteId());
	    if (donante == null) {
	        throw new ObjetoNuloException("Donante no encontrado.");
	    }
	    ArrayList<Bien> bienes = new ArrayList<>();
	    Vehiculo vehiculo = new Vehiculo("XYZ123", "Disponible", "Camioneta", 1000);
	    final int CATEGORIA_CALIDAD_POR_DEFECTO = 2; 
	    for (BienDTO bienDTO : pedidoDTO.getBienes()) {
	        bienes.add(new Bien(bienDTO.getId(), bienDTO.getCantidad(), CATEGORIA_CALIDAD_POR_DEFECTO, vehiculo));
	    }

	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	    LocalDateTime fechaLocalDateTime = LocalDate.parse(pedidoDTO.getFecha(), formatter).atStartOfDay();

	    PedidosDonacion pedido = donante.crearPedido(fechaLocalDateTime, bienes, pedidoDTO.getTipoVehiculo(), pedidoDTO.getObservaciones());
	    this.pedidos.add(pedido);
	}

	@Override
	public List<DonanteDTO> obtenerDonantes() {
		List<DonanteDTO> donanteDTOs = new ArrayList<>();
		for (Donante donante : donantes) {
			donanteDTOs.add(new DonanteDTO(donante.obtenerDni(), donante.obtenerNombre(), donante.obtenerApellido()));
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
					bienes.add(bien.toString()); // 
				}

				pendientes.add(new PedidoDonacionDTO(
					pedido.obtenerId(),
					formattedDate, // usar la fecha formateada como string
					pedido.describirTipoVehiculo(),
					pedido.obtenerObservaciones(),
					pedido.obtenerDonante().obtenerDni()
				));
			}
		}
		return pendientes; // devuelve lista
	}

	@Override
	public void generarOrdenRetiro(int idPedidoDonacion) throws ObjetoNuloException, ReglaNegocioException {
		PedidosDonacion pedido = buscarPedidoPorId(idPedidoDonacion);
		if (pedido == null) {
			throw new ObjetoNuloException("El pedido de donación no existe.");
		}
		if (pedido.obtenerOrden() != null) {
			throw new ReglaNegocioException("El pedido ya tiene una orden de retiro asignada.");
		}
		if (pedido.obtenerDonante() == null) {
			throw new ObjetoNuloException("El pedido de donación no tiene un donante asignado.");
		}

		OrdenRetiro orden = new OrdenRetiro(pedido, pedido.obtenerUbicacion());
		pedido.asignarOrden(orden);
		this.ordenes.add(orden);

		List<VisitaDTO> visitasDTO = new ArrayList<>();
		for (Visita visita : orden.obtenerVisitas()) {
			visitasDTO.add(new VisitaDTO(
				visita.obtenerFechaFormateada(),
				visita.obtenerObservacion(),
				convertirBienesAStrings(visita.obtenerBienes())
			));
		}
	}

	@Override
	public List<OrdenRetiroDTO> obtenerOrdenesDeRetiro(String estado) {
		List<OrdenRetiroDTO> ordenesFiltradas = new ArrayList<>();
		for (OrdenRetiro orden : ordenes) {
			if (orden.describirEstado().equalsIgnoreCase(estado)) {
				List<VisitaDTO> visitasDTO = new ArrayList<>();
				for (Visita visita : orden.obtenerVisitas()) {
					visitasDTO.add(new VisitaDTO(visita.obtenerFechaFormateada(), visita.obtenerObservacion(), convertirBienesAStrings(visita.obtenerBienes())));
				}
				String donante = orden.obtenerDonante() != null ? orden.obtenerDonante().getNombre() : "Donante Desconocido";
				String vehiculo = orden.obtenerVehiculo() != null ? orden.obtenerVehiculo().getDescripcion() : "Vehículo Desconocido";
				String voluntario = orden.obtenerVoluntarioPrincipal() != null ? orden.obtenerVoluntarioPrincipal().obtenerNombre() : "Voluntario Desconocido";
				ordenesFiltradas.add(new OrdenRetiroDTO(orden.obtenerId(), orden.obtenerEstado(), orden.obtenerFechaCreacion(), visitasDTO, donante, vehiculo, voluntario));
			}
		}
		return ordenesFiltradas;
	}

	@Override
	public OrdenRetiroDTO obtenerOrdenDeRetiroDetalle(int idOrden) {
		for (OrdenRetiro orden : ordenes) {
			if (orden.obtenerId() == idOrden) {
				// ajustado
				return new OrdenRetiroDTO(orden.obtenerId(), orden.obtenerEstado(), orden.obtenerFechaCreacion(), new ArrayList<>(), orden.getDonante() != null ? orden.getDonante().getNombre() : "Sin Donante", orden.getVehiculo() != null ? orden.getVehiculo().getPatente() : "Sin Vehículo", orden.getVoluntario() != null ? orden.getVoluntario().getNombre() : "Sin Voluntario");
			}
		}
		return null;
	}

	@Override
	public void registrarVisita(int idOrdenRetiro, VisitaDTO visitaDTO) throws ObjetoNuloException, CampoVacioException {
		OrdenRetiro orden = buscarOrdenPorId(idOrdenRetiro);
		if (orden == null) {
			throw new ObjetoNuloException("Orden de retiro no encontrada.");
		}

		if (visitaDTO.getFechaHora() != null) {
			LocalDateTime fechaVisita = visitaDTO.getFechaHora();
			orden.registrarVisita(fechaVisita, visitaDTO.getObservacion());
		} else {
			throw new CampoVacioException("La fecha de la visita no puede ser nula.");
		}
	}


	@Override
	public void actualizarEstadoOrdenRetiro(int idOrdenRetiro, int nuevoEstado) throws ReglaNegocioException {
		OrdenRetiro orden = buscarOrdenPorId(idOrdenRetiro);
		if (orden == null) {
			throw new ReglaNegocioException("La orden de retiro no existe.");
		}
		orden.actualizarEstado(nuevoEstado);
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

	private Donante buscarDonantePorId(int id) {
		for (Donante donante : donantes) {
			if (donante.obtenerDni() == id) {
				return donante;
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

	private List<Bien> convertirBienes(List<String> bienesStr) throws CampoVacioException {
		List<Bien> bienes = new ArrayList<>();
        if (bienesStr != null) {
		    for (String nombreBien : bienesStr) {
			    bienes.add(new Bien(nombreBien.trim()));
		    }
        }
		return bienes;
	}

	@Override
	public List<VoluntarioDTO> obtenerVoluntarios() {
		List<VoluntarioDTO> voluntariosDTO = new ArrayList<>();
		for (Voluntario voluntario : this.voluntarios) {
			voluntariosDTO.add(new VoluntarioDTO(voluntario.obtenerId(), voluntario.obtenerNombre(), voluntario.obtenerApellido()));
		}
		return voluntariosDTO;
	}

	@Override
	public void crearOrdenRetiro(List<Integer> idsPedidos, int idVoluntario, String tipoVehiculo) throws ReglaNegocioException, ObjetoNuloException {
		Voluntario voluntario = buscarVoluntarioPorId(idVoluntario);
		if (voluntario == null) {
			throw new ObjetoNuloException("Voluntario no encontrado.");
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

		OrdenRetiro nuevaOrden = new OrdenRetiro(voluntario, tipoVehiculo);

		//  un tutu disponible del tipo requerido en la lista fija
		Vehiculo vehiculoAsignado = null;
		for (Vehiculo v : vehiculosDisponibles) {
			if (v.getTipoVeh().equalsIgnoreCase(tipoVehiculo) && v.getEstado().equals("Disponible")) {
				vehiculoAsignado = v;
				break; //  el primer autito disponible encontrado
			}
		}

		nuevaOrden.asignarVehiculo(vehiculoAsignado);

		for (PedidosDonacion pedido : pedidosAAsignar) {
			pedido.asignarOrden(nuevaOrden);
		}
		this.ordenes.add(nuevaOrden);
	}

	private Voluntario buscarVoluntarioPorId(int idVoluntario) {
		for (Voluntario voluntario : this.voluntarios) {
			if (voluntario.obtenerId() == idVoluntario) {
				return voluntario;
			}
		}
		return null;
	}

	private List<String> convertirBienesAStrings(List<Bien> bienes) {
        List<String> bienesStr = new ArrayList<>();
        for (Bien bien : bienes) {
            bienesStr.add(bien.toString()); 
        }
        return bienesStr;
    }

	@Override
	public List<OrdenRetiroDTO> obtenerOrdenesAsignadas(String voluntario) {
		// Confirmar que el ID se transmite correctamente
		List<OrdenRetiroDTO> ordenesAsignadas = new ArrayList<>();
		for (OrdenRetiro orden : this.ordenes) {
			int idOrden = orden.getId(); // Verificar el ID
			ordenesAsignadas.add(new OrdenRetiroDTO(
				idOrden,
				orden.getEstado(),
				orden.getFechaCreacion(),
				null, // Visitas no incluidas
				orden.getDonante() != null ? orden.getDonante().getNombre() : "Sin Donante",
				orden.getVehiculo() != null ? orden.getVehiculo().getPatente() : "Sin Vehículo",
				orden.getVoluntario() != null ? orden.getVoluntario().getNombre() : "Sin Voluntario"
			));
		}
		return ordenesAsignadas;
	}

	@Override
	public List<PedidoDonacionDTO> obtenerPedidosDeOrden(int idOrden) {
		List<PedidoDonacionDTO> pedidosDTO = new ArrayList<>();
		
		for (PedidosDonacion pedido : this.pedidos) { 
			
			OrdenRetiro orden = pedido.obtenerOrden(); 
			
			if (orden != null && orden.obtenerId() == idOrden) { 
				
				PedidoDonacionDTO dto = this.convertirPedidoADTO(pedido);
				
				if (dto != null) {
					pedidosDTO.add(dto); 
				}
			}
		}
		
		return pedidosDTO;
	}
	
	private PedidoDonacionDTO convertirPedidoADTO(PedidosDonacion pedido) {
	    if (pedido == null) {
	        return null;
	    }
	    Donante donante = pedido.obtenerDonante();
	    if (donante == null) {
	        System.out.println("Advertencia: El pedido " + pedido.obtenerId() + " no tiene un donante asociado.");
	        return null;
	    }

	    return new PedidoDonacionDTO(
	        pedido.obtenerId(),
	        donante.getNombre(),     
	        donante.getDireccion(),  
	        pedido.obtenerEstado()
	    );
	}
	

//	 mapea el estado de String  a int 
	private int mapearEstadoPedidoAInt(String estado) throws ReglaNegocioException {
        switch (estado.toUpperCase()) {
            case "PENDIENTE":
                return 1; // PENDIENTE
            case "EN_EJECUCION":
                return 2; // EN_EJECUCION
            case "COMPLETADO":
                return 3; // COMPLETADO
            default:
                throw new ReglaNegocioException("Estado de pedido inválido: " + estado);
        }
    }


	public void actualizarEstadoDelPedido(int idPedido, String nuevoEstado) throws ReglaNegocioException {
	    PedidosDonacion pedido = buscarPedidoPorId(idPedido);
	    if (pedido == null) {
	        throw new ReglaNegocioException("El pedido de donación con ID " + idPedido + " no existe.");
	    }

	    int nuevoEstadoInt = mapearEstadoPedidoAInt(nuevoEstado);
	    
	    pedido.actualizarEstado(nuevoEstadoInt); 
	    
	    System.out.println("Éxito: Estado del Pedido " + idPedido + " actualizado a " + nuevoEstado);
	}

	@Override
	public List<VisitaDTO> obtenerVisitasPorVoluntario(VoluntarioDTO voluntario) {
		List<VisitaDTO> visitas = new ArrayList<>();
		for (OrdenRetiro orden : this.ordenes) {
			if (orden.getVoluntario() != null && orden.getVoluntario().getNombre().equals(voluntario.getNombre())) {
				for (Visita visita : orden.obtenerVisitas()) {
					visitas.add(new VisitaDTO(visita.obtenerFechaFormateada(), visita.obtenerObservacion(), convertirBienesAStrings(visita.obtenerBienes())));
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
	
}