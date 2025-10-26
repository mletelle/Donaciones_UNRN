package ar.edu.unrn.seminario.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;

import ar.edu.unrn.seminario.dto.RolDTO;
import ar.edu.unrn.seminario.dto.UsuarioDTO;
import ar.edu.unrn.seminario.dto.PedidoDonacionDTO;
import ar.edu.unrn.seminario.dto.DonanteDTO;
import ar.edu.unrn.seminario.dto.OrdenRetiroDTO;
import ar.edu.unrn.seminario.dto.VisitaDTO;
import ar.edu.unrn.seminario.dto.BienDTO;
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
import ar.edu.unrn.seminario.modelo.Ubicacion;
import ar.edu.unrn.seminario.modelo.Voluntario;

public class MemoryApi implements IApi {

	private ArrayList<Rol> roles = new ArrayList<>();
	private ArrayList<Usuario> usuarios = new ArrayList<>();
	private ArrayList<Donante> donantes = new ArrayList<>();
	private List<PedidosDonacion> pedidos = new ArrayList<>();
	private List<OrdenRetiro> ordenes = new ArrayList<>();
	private ArrayList<Voluntario> voluntarios = new ArrayList<>();

	public MemoryApi() throws CampoVacioException {
		this.roles.add(new Rol(1, "ADMIN"));
		this.roles.add(new Rol(2, "ESTUDIANTE"));
		this.roles.add(new Rol(3, "INVITADO"));
		inicializarUsuarios();
		inicializarDonantes();
		inicializarPedidos();
		inicializarVoluntarios();
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
			Donante donante1 = this.donantes.get(0);
			List<Bien> bienes1 = new ArrayList<>();
			bienes1.add(new Bien(1, 10, 2)); 
			this.pedidos.add(new PedidosDonacion(new Date(), new ArrayList<>(bienes1), PedidosDonacion.getVehiculoCamioneta(), "Sin observaciones", donante1));

			if (this.donantes.size() > 1) {
				Donante donante2 = this.donantes.get(1);
				List<Bien> bienes2 = new ArrayList<>();
				bienes2.add(new Bien(2, 5, 1)); 
				bienes2.add(new Bien(3, 1, 3));
				Date fecha2 = new Date(System.currentTimeMillis() - 86400000); 
				this.pedidos.add(new PedidosDonacion(fecha2, new ArrayList<>(bienes2), PedidosDonacion.getVehiculoCamion(), "Mueble grande", donante2));
			}

			List<Bien> bienes3 = new ArrayList<>();
			bienes3.add(new Bien(4, 15, 2)); 
			Date fecha3 = new Date(System.currentTimeMillis() - 172800000); 
			this.pedidos.add(new PedidosDonacion(fecha3, new ArrayList<>(bienes3), PedidosDonacion.getVehiculoAuto(), "Cajas pequeñas", donante1));
		} catch (CampoVacioException | ObjetoNuloException e) {
			e.printStackTrace();
		}
	}

	private void inicializarVoluntarios() {
			this.voluntarios.add(new Voluntario("Carlos", "Lopez", "Zona Norte"));
			this.voluntarios.add(new Voluntario("Ana", "Martinez", "Zona Sur"));
			this.voluntarios.add(new Voluntario("Luis", "Gomez", "Zona Centro"));
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
			throw new ObjetoNuloException("El donante no existe.");
		}

		ArrayList<Bien> bienes = new ArrayList<>();
		for (BienDTO bienDTO : pedidoDTO.getBienes()) {
			bienes.add(new Bien(bienDTO.getTipo(), bienDTO.getCantidad(), bienDTO.getCategoria()));
		}

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDateTime fechaLocalDateTime = LocalDate.parse(pedidoDTO.getFecha(), formatter).atStartOfDay();
        Date fecha = Date.from(fechaLocalDateTime.atZone(ZoneId.systemDefault()).toInstant());
		PedidosDonacion pedido = donante.crearPedido(fecha, bienes, pedidoDTO.getTipoVehiculo(), pedidoDTO.getObservaciones());
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
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		for (PedidosDonacion pedido : pedidos) {
			if (pedido.obtenerOrden() == null) {
				String formattedDate = formatter.format(pedido.obtenerFecha());

				List<String> bienes = new ArrayList<>();
				for (Bien bien : pedido.obtenerBienes()) {
					bienes.add(bien.toString()); // Assuming Bien has a meaningful toString method
				}

				pendientes.add(new PedidoDonacionDTO(
					pedido.obtenerId(),
					formattedDate, // Use the formatted date
					pedido.describirTipoVehiculo(),
					pedido.obtenerObservaciones(),
					pedido.obtenerDonante().obtenerDni()
				));
			}
		}
		return pendientes; // Ensure the method always returns a list
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

		OrdenRetiro orden = new OrdenRetiro(pedido, pedido.obtenerUbicacion());
		pedido.asignarOrden(orden);
		this.ordenes.add(orden);

		List<VisitaDTO> visitasDTO = new ArrayList<>();
		for (Visita visita : orden.obtenerVisitas()) {
			visitasDTO.add(new VisitaDTO(visita.obtenerFechaFormateada(), visita.obtenerObservacion(), visita.obtenerBienes()));
		}

		OrdenRetiroDTO ordenDTO = new OrdenRetiroDTO(orden.obtenerId(), orden.obtenerEstado(), orden.obtenerFechaCreacion(), new ArrayList<VisitaDTO>());
	}

	@Override
	public List<OrdenRetiroDTO> obtenerOrdenesDeRetiro(String estado) {
		List<OrdenRetiroDTO> ordenesFiltradas = new ArrayList<>();
		for (OrdenRetiro orden : ordenes) {
			if (orden.describirEstado().equalsIgnoreCase(estado)) {
				List<VisitaDTO> visitasDTO = new ArrayList<>();
				for (Visita visita : orden.obtenerVisitas()) {
					visitasDTO.add(new VisitaDTO(visita.obtenerFechaFormateada(), visita.obtenerObservacion(), visita.obtenerBienes(), true));
				}
				ordenesFiltradas.add(new OrdenRetiroDTO(orden.obtenerId(), orden.obtenerEstado(), orden.obtenerFechaCreacion(), visitasDTO));
			}
		}
		return ordenesFiltradas;
	}

	@Override
	public OrdenRetiroDTO obtenerOrdenDeRetiroDetalle(int idOrden) {
		for (OrdenRetiro orden : ordenes) {
			if (orden.obtenerId() == idOrden) {
				return new OrdenRetiroDTO(orden.obtenerId(), orden.obtenerEstado(), orden.obtenerFechaCreacion(), orden.obtenerVisitas());
			}
		}
		return null;
	}

	@Override
	public void registrarVisita(int idOrdenRetiro, VisitaDTO visitaDTO) throws ObjetoNuloException, CampoVacioException {
		OrdenRetiro orden = buscarOrdenPorId(idOrdenRetiro);
		if (orden == null) {
			throw new ObjetoNuloException("La orden de retiro no existe.");
		}
		Visita visita = new Visita(visitaDTO.getFechaDeVisita(), visitaDTO.getObservacion(), convertirBienes(visitaDTO.getBienesRetirados()));
		orden.agregarVisita(visita);
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
		for (String bienStr : bienesStr) {
			String[] parts = bienStr.split(","); 
			if (parts.length != 3) {
				throw new CampoVacioException("Formato de bien inválido: " + bienStr);
			}
			int tipo = mapTipo(parts[0]);
			int cantidad = Integer.parseInt(parts[1]);
			int categoria = Integer.parseInt(parts[2]);
			bienes.add(new Bien(tipo, cantidad, categoria));
		}
		return bienes;
	}

	private int mapTipo(String tipoStr) throws CampoVacioException {
		switch (tipoStr.toLowerCase()) {
			case "alimento":
				return 1; // TIPO_ALIMENTO
			case "ropa":
				return 2; // TIPO_ROPA
			case "mobiliario":
				return 3; // TIPO_MOBILIARIO
			case "higiene":
				return 4; // TIPO_HIGIENE
			default:
				throw new CampoVacioException("Tipo desconocido: " + tipoStr);
		}
	}

	@Override
	public List<PedidoDonacionDTO> obtenerPedidos() {
		List<PedidoDonacionDTO> pedidosDTO = new ArrayList<>();

		for (PedidosDonacion pedido : pedidos) {
			List<BienDTO> bienesDTO = new ArrayList<>();
			for (Bien bien : pedido.obtenerBienes()) {
				bienesDTO.add(new BienDTO(bien.getTipo(), bien.getCantidad(), bien.getCategoria()));
			}

			pedidosDTO.add(new PedidoDonacionDTO(
				pedido.obtenerId(),
				pedido.obtenerFecha().toString(),
				pedido.describirTipoVehiculo(),
				pedido.obtenerObservaciones(),
				pedido.obtenerDonante().obtenerDni(),
				bienesDTO
			));
		}

		return pedidosDTO;
	}
}
