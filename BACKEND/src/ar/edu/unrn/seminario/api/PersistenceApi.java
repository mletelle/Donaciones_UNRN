package ar.edu.unrn.seminario.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ar.edu.unrn.seminario.accesos.BienDAOJDBC;
import ar.edu.unrn.seminario.accesos.BienDao;
import ar.edu.unrn.seminario.accesos.ConnectionManager;
import ar.edu.unrn.seminario.accesos.OrdenRetiroDAOJDBC;
import ar.edu.unrn.seminario.accesos.OrdenRetiroDao;
import ar.edu.unrn.seminario.accesos.PedidosDonacionDAOJDBC;
import ar.edu.unrn.seminario.accesos.PedidosDonacionDao;
import ar.edu.unrn.seminario.accesos.RolDAOJDBC;
import ar.edu.unrn.seminario.accesos.RolDao;
import ar.edu.unrn.seminario.accesos.UsuarioDAOJDBC;
import ar.edu.unrn.seminario.accesos.UsuarioDao;
import ar.edu.unrn.seminario.accesos.VehiculoDAOJDBC;
import ar.edu.unrn.seminario.accesos.VehiculoDao;
import ar.edu.unrn.seminario.accesos.VisitaDAOJDBC;
import ar.edu.unrn.seminario.accesos.VisitaDao;
import ar.edu.unrn.seminario.dto.BienDTO;
import ar.edu.unrn.seminario.dto.DonanteDTO;
import ar.edu.unrn.seminario.dto.OrdenRetiroDTO;
import ar.edu.unrn.seminario.dto.PedidoDonacionDTO;
import ar.edu.unrn.seminario.dto.RolDTO;
import ar.edu.unrn.seminario.dto.UsuarioDTO;
import ar.edu.unrn.seminario.dto.VisitaDTO;
import ar.edu.unrn.seminario.dto.VoluntarioDTO;
import ar.edu.unrn.seminario.exception.CampoVacioException;
import ar.edu.unrn.seminario.exception.ObjetoNuloException;
import ar.edu.unrn.seminario.exception.ReglaNegocioException;
import ar.edu.unrn.seminario.exception.UsuarioInvalidoException;
import ar.edu.unrn.seminario.modelo.Bien;
import ar.edu.unrn.seminario.modelo.OrdenRetiro;
import ar.edu.unrn.seminario.modelo.PedidosDonacion;
import ar.edu.unrn.seminario.modelo.ResultadoVisita;
import ar.edu.unrn.seminario.modelo.Rol;
import ar.edu.unrn.seminario.modelo.Usuario;
import ar.edu.unrn.seminario.modelo.Vehiculo;
import ar.edu.unrn.seminario.modelo.Visita;

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
    
    /// Se utilizó lambda
    @Override
    public List<BienDTO> obtenerInventario() {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            
            // Recuperar entidades desde la BD (filtradas por estado EN_STOCK)
            List<Bien> bienesEnStock = bienDao.findByEstadoInventario(Bien.ESTADO_EN_STOCK, conn);

            // Uso de STREAM para transformar Entidades -> DTOs visuales
            return bienesEnStock.stream()
                .map(this::convertirEntidadADTOVisual) // Referencia a método helper
                .collect(Collectors.toList());

        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            ConnectionManager.disconnect();
        }
    }

    // Helper para encapsular la lógica de presentación
    private BienDTO convertirEntidadADTOVisual(Bien bien) {
        String categoriaStr = mapCategoriaToString(bien.obtenerCategoria());
        String estadoStr = (bien.obtenerTipo() == BienDTO.TIPO_NUEVO) ? "Nuevo" : "Usado";
        
        String vencimientoStr = "-";
        if (bien.getFecVec() != null) {
            java.time.LocalDate localDate = new java.sql.Date(bien.getFecVec().getTime()).toLocalDate();
            vencimientoStr = localDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }

        // Usamos el constructor visual (Strings)
        BienDTO dto = new BienDTO(
            categoriaStr,
            bien.getDescripcion(),
            bien.obtenerCantidad(),
            estadoStr,
            vencimientoStr
        );
        
        // Setea el id real
        dto.setId(bien.getId());
        dto.setCategoria(bien.obtenerCategoria()); // Útil para lógica interna si hace falta
        dto.setTipo(bien.obtenerTipo());
        
        return dto;
    }

    
    public void actualizarBienInventario(BienDTO bienDTO) throws ObjetoNuloException, CampoVacioException, ReglaNegocioException {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);

            // Validar ID
            if (bienDTO.getId() <= 0) {
                throw new ObjetoNuloException("ID de bien inválido para actualización.");
            }

            // Validar existencia en BD
            Bien bienDb = bienDao.findById(bienDTO.getId(), conn);
            if (bienDb == null) {
                throw new ObjetoNuloException("El bien no existe en el inventario.");
            }

            // Validaciones de negocio
            if (bienDTO.getCantidad() < 0) {
                throw new ReglaNegocioException("La cantidad no puede ser negativa.");
            }

            // Actualizar objeto con datos del DTO
            bienDb.setCantidad(bienDTO.getCantidad());
            bienDb.setDescripcion(bienDTO.getDescripcion());
            // Podrías actualizar el estado si el DTO lo permite (ej: mover a DAÑADO)
            
            // Persistir
            bienDao.update(bienDb, conn);

            conn.commit();
        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            throw new RuntimeException("Error actualizando inventario", e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException e) {}
            }
            ConnectionManager.disconnect();
        }
    }
    
    
    
    
    
    
    
    
    
    // Helper simple para categorías (podría estar en un Enum o clase Utils)
    private String mapCategoriaToString(int idCategoria) {
        switch (idCategoria) {
            case BienDTO.CATEGORIA_ROPA: return "Ropa";
            case BienDTO.CATEGORIA_MUEBLES: return "Muebles";
            case BienDTO.CATEGORIA_ALIMENTOS: return "Alimentos";
            case BienDTO.CATEGORIA_ELECTRODOMESTICOS: return "Electrodomesticos";
            case BienDTO.CATEGORIA_HERRAMIENTAS: return "Herramientas";
            case BienDTO.CATEGORIA_JUGUETES: return "Juguetes";
            case BienDTO.CATEGORIA_LIBROS: return "Libros";
            case BienDTO.CATEGORIA_MEDICAMENTOS: return "Medicamentos";
            case BienDTO.CATEGORIA_HIGIENE: return "Higiene";
            
            default: return "Otros";
        }
    }

    @Override
    public void registrarVisita(int idOrdenRetiro, int idPedido, LocalDateTime fechaHora, String resultado, String observacion)
            throws ObjetoNuloException, CampoVacioException, ReglaNegocioException {

        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);

            PedidosDonacion pedido = pedidoDao.findByIdAndOrden(idPedido, idOrdenRetiro, conn);
            if (pedido == null) throw new ObjetoNuloException("Pedido inválido.");
            
            OrdenRetiro orden = pedido.obtenerOrden();
            if (orden == null) throw new ObjetoNuloException("Orden no encontrada.");

            ResultadoVisita resEnum = ResultadoVisita.fromString(resultado);
            Visita visita = new Visita(fechaHora, resEnum, observacion);
            visita.setPedidoRelacionado(pedido);
            visitaDao.create(visita, idOrdenRetiro, idPedido, conn);

            if (resEnum == ResultadoVisita.RECOLECCION_EXITOSA) {
                pedido.marcarCompletado();
                // --- INVENTARIO: PASAR A EN_STOCK ---
                bienDao.updateEstadoPorPedido(idPedido, Bien.ESTADO_EN_STOCK, conn);
            } else if (resEnum == ResultadoVisita.CANCELADO) {
                pedido.marcarCompletado(); 
            } else {
                pedido.marcarEnEjecucion();
            }

            pedidoDao.update(pedido, conn);
            orden.actualizarEstadoAutomatico();
            ordenDao.update(orden, conn);

            conn.commit();
        } catch (SQLException e) {
            rollback(conn);
            throw new RuntimeException(e);
        } catch (Exception e) {
            rollback(conn);
            throw e;
        } finally {
            closeConnection(conn);
        }
    }

    /// Se utilizo stream
    @Override
    public List<DonanteDTO> obtenerDonantes() {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            // Uso de Stream para mapear Usuario -> DonanteDTO
            return usuarioDao.findByRol(3, conn).stream()
                .map(u -> new DonanteDTO(
                    u.getDni(),
                    u.getNombre() + " " + u.getApellido(),
                    u.obtenerDireccion() != null ? u.obtenerDireccion() : ""
                ))
                .collect(Collectors.toList());
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            ConnectionManager.disconnect();
        }
    }
    
    /// Se utilizó stream
    @Override
    public List<PedidoDonacionDTO> obtenerTodosPedidos() {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            // Uso de Stream para mapear PedidoDonacion -> PedidoDonacionDTO
            return pedidoDao.findAll(conn).stream()
                .map(p -> new PedidoDonacionDTO(
                    p.getId(),
                    p.obtenerFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    p.describirTipoVehiculo(),
                    p.getDonante().getDni(),
                    p.getDonante().obtenerNombre() + " " + p.getDonante().obtenerApellido(),
                    p.obtenerEstado()
                ))
                .collect(Collectors.toList());
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            ConnectionManager.disconnect();
        }
    }

    @Override
    public void registrarUsuario(String username, String password, String email, String nombre, Integer codigoRol,
            String apellido, int dni, String direccion) throws CampoVacioException, ObjetoNuloException, UsuarioInvalidoException {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);
            
            Rol rol = rolDao.find(codigoRol, conn);
            if (rol == null) throw new ObjetoNuloException("Rol no encontrado");
            
            Usuario usuario = new Usuario(username, password, nombre, email, rol, apellido, dni, direccion);
            usuarioDao.create(usuario, conn);
            
            conn.commit();
        } catch (SQLException e) {
            rollback(conn);
            if (e.getMessage().contains("dni_UNIQUE") || e.getMessage().contains("Duplicate")) {
                throw new UsuarioInvalidoException("Ya existe un usuario con el DNI " + dni);
            }
            throw new RuntimeException("Error SQL registrando usuario", e);
        } finally {
            closeConnection(conn);
        }
    }

    @Override
    public List<UsuarioDTO> obtenerUsuarios() {
        Connection conn = null;
        List<UsuarioDTO> dtos = new ArrayList<>();
        try {
            conn = ConnectionManager.getConnection();
            List<Usuario> usuarios = usuarioDao.findAll(conn);
            for (Usuario u : usuarios) {
                dtos.add(new UsuarioDTO(u.getUsuario(), u.getContrasena(), u.getNombre(), u.getEmail(),
                        u.getRol().getNombre(), u.isActivo(), u.obtenerEstado()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.disconnect();
        }
        return dtos;
    }

    @Override
    public UsuarioDTO obtenerUsuario(String username) {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            Usuario u = usuarioDao.find(username, conn);
            if (u != null) {
                return new UsuarioDTO(u.getUsuario(), u.getContrasena(), u.getNombre(), u.getEmail(),
                        u.getRol().getNombre(), u.isActivo(), u.obtenerEstado());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.disconnect();
        }
        return null;
    }

    @Override
    public void eliminarUsuario(String username) {
        // Implementación pendiente o lógica de desactivación
    }

    @Override
    // Se utilizo stream
    public List<RolDTO> obtenerRoles() {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            return rolDao.findAll(conn).stream()
                    .map(r -> new RolDTO(r.getCodigo(), r.getNombre(), r.isActivo()))
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            ConnectionManager.disconnect();
        }
    }

    @Override
    public List<RolDTO> obtenerRolesActivos() {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            return rolDao.findAll(conn).stream()
                    .filter(Rol::isActivo)
                    .map(r -> new RolDTO(r.getCodigo(), r.getNombre(), r.isActivo()))
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            ConnectionManager.disconnect();
        }
    }

    @Override
    public void guardarRol(Integer codigo, String descripcion, boolean estado) throws CampoVacioException {
        // Lógica de guardado de rol
    }

    @Override
    public RolDTO obtenerRolPorCodigo(Integer codigo) {
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
    public void activarRol(Integer codigo) { }

    @Override
    public void desactivarRol(Integer codigo) { }

    @Override
    public void activarUsuario(String username) {
        cambiarEstadoUsuario(username, true);
    }

    @Override
    public void desactivarUsuario(String username) {
        cambiarEstadoUsuario(username, false);
    }

    private void cambiarEstadoUsuario(String username, boolean activar) {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);
            Usuario usuario = usuarioDao.find(username, conn);
            if (usuario != null) {
                if (activar) usuario.activar(); else usuario.desactivar();
                usuarioDao.update(usuario, conn);
            }
            conn.commit();
        } catch (SQLException e) {
            rollback(conn);
            e.printStackTrace();
        } finally {
            closeConnection(conn);
        }
    }

    @Override
    public void registrarPedidoDonacion(PedidoDonacionDTO pedidoDTO) throws CampoVacioException, ObjetoNuloException {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);

            Usuario donante = usuarioDao.findByDni(pedidoDTO.getDonanteId(), conn);
            if (donante == null) throw new ObjetoNuloException("Donante no encontrado");

            List<Bien> bienes = new ArrayList<>();
            for (ar.edu.unrn.seminario.dto.BienDTO dto : pedidoDTO.getBienes()) {
                Bien bien = new Bien(dto.getTipo(), dto.getCantidad(), dto.getCategoria());
                if (dto.getDescripcion() != null) bien.setDescripcion(dto.getDescripcion());
                if (dto.getFechaVencimiento() != null) {
                    java.time.ZoneId zoneId = java.time.ZoneId.systemDefault();
                    bien.setFecVec(java.util.Date.from(dto.getFechaVencimiento().atStartOfDay(zoneId).toInstant()));
                }
                bienes.add(bien);
            }
            
            // Parseo de DTO a fecha
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDateTime fecha = java.time.LocalDate.parse(pedidoDTO.getFecha(), formatter).atStartOfDay();
            
            PedidosDonacion pedido = new PedidosDonacion(fecha, bienes, pedidoDTO.getTipoVehiculo(), donante);
            
            int idPedido = pedidoDao.create(pedido, conn);
            bienDao.createBatch(bienes, idPedido, conn);
            
            conn.commit();
        } catch (SQLException e) {
            rollback(conn);
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn);
        }
    }

    @Override
    public List<PedidoDonacionDTO> obtenerPedidosPendientes() {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            return pedidoDao.findAllPendientes(conn).stream()
                .map(p -> new PedidoDonacionDTO(
                    p.getId(),
                    p.obtenerFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    p.describirTipoVehiculo(),
                    p.getDonante().getDni(),
                    p.getDonante().obtenerNombre() + " " + p.getDonante().obtenerApellido()
                ))
                .collect(Collectors.toList());
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            ConnectionManager.disconnect();
        }
    }

    @Override
    public List<PedidoDonacionDTO> obtenerPedidosDeOrden(int idOrden) {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            return pedidoDao.findByOrden(idOrden, conn).stream()
                .map(p -> new PedidoDonacionDTO(
                    p.getId(),
                    p.getDonante().obtenerNombre() + " " + p.getDonante().obtenerApellido(),
                    p.obtenerDireccion(),
                    p.obtenerEstado()
                ))
                .collect(Collectors.toList());
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            ConnectionManager.disconnect();
        }
    }

    @Override
    public String obtenerNombreDonantePorId(int idPedido) {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            PedidosDonacion pedido = pedidoDao.findById(idPedido, conn);
            if (pedido != null && pedido.getDonante() != null) {
                return pedido.getDonante().getNombre() + " " + pedido.getDonante().getApellido();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.disconnect();
        }
        return "";
    }

    @Override
    public void crearOrdenRetiro(List<Integer> idsPedidos, int idVoluntario, String tipoVehiculo)
            throws ReglaNegocioException, ObjetoNuloException {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);

            List<PedidosDonacion> pedidos = pedidoDao.findByIds(idsPedidos, conn);
            if (pedidos == null || pedidos.size() != idsPedidos.size()) {
                throw new ObjetoNuloException("No se encontraron todos los pedidos solicitados.");
            }
            
            for (PedidosDonacion p : pedidos) {
                if (p.obtenerOrden() != null) throw new ReglaNegocioException("El pedido " + p.getId() + " ya tiene orden.");
            }

            Usuario voluntario = usuarioDao.findByDni(idVoluntario, conn);
            if (voluntario == null || voluntario.getRol().getCodigo() != 2) {
                throw new ObjetoNuloException("Voluntario inválido.");
            }

            Vehiculo vehiculo = vehiculoDao.findDisponible(tipoVehiculo, conn);
            if (vehiculo == null) throw new ReglaNegocioException("No hay vehículos disponibles de tipo " + tipoVehiculo);

            OrdenRetiro orden = new OrdenRetiro(pedidos, null);
            orden.asignarVehiculo(vehiculo);
            orden.asignarVoluntario(voluntario);

            int idOrden = ordenDao.create(orden, conn);
            orden.setId(idOrden);

            // Actualizar pedidos
            try (PreparedStatement stmt = conn.prepareStatement("UPDATE pedidos_donacion SET estado = ?, id_orden_retiro = ? WHERE id = ?")) {
                for (PedidosDonacion p : pedidos) {
                    stmt.setString(1, p.obtenerEstado());
                    stmt.setInt(2, idOrden);
                    stmt.setInt(3, p.getId());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

            conn.commit();
        } catch (SQLException e) {
            rollback(conn);
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn);
        }
    }

    @Override
    public List<OrdenRetiroDTO> obtenerOrdenesDeRetiro(String estado) {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            return ordenDao.findByEstado(estado, conn).stream()
                .map(this::mapearOrdenADTO)
                .collect(Collectors.toList());
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            ConnectionManager.disconnect();
        }
    }

    @Override
    public List<OrdenRetiroDTO> obtenerTodasOrdenesRetiro() {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            return ordenDao.findAll(conn).stream()
                .map(this::mapearOrdenADTO)
                .collect(Collectors.toList());
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            ConnectionManager.disconnect();
        }
    }

    @Override
    public List<OrdenRetiroDTO> obtenerOrdenesAsignadas(String voluntarioUser) {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            return ordenDao.findByVoluntario(voluntarioUser, conn).stream()
                .map(this::mapearOrdenADTO)
                .collect(Collectors.toList());
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            ConnectionManager.disconnect();
        }
    }

    @Override
    public List<VoluntarioDTO> obtenerVoluntarios() {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            return usuarioDao.findByRol(2, conn).stream()
                .map(v -> new VoluntarioDTO(v.getDni(), v.getNombre(), v.getApellido(), v.getUsuario()))
                .collect(Collectors.toList());
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            ConnectionManager.disconnect();
        }
    }

    @Override
    public List<VisitaDTO> obtenerVisitasPorVoluntario(VoluntarioDTO voluntario) {
        Connection conn = null;
        List<VisitaDTO> dtos = new ArrayList<>();
        try {
            conn = ConnectionManager.getConnection();
            // Buscar usuario por DNI (el DTO tiene ID = DNI en este contexto)
            Usuario vol = usuarioDao.findByDni(voluntario.getId(), conn);
            if (vol != null) {
                List<Visita> visitas = visitaDao.findByVoluntario(vol, conn);
                for (Visita v : visitas) {
                    String donante = (v.getPedidoRelacionado() != null && v.getPedidoRelacionado().getDonante() != null)
                        ? v.getPedidoRelacionado().getDonante().obtenerNombre() + " " + v.getPedidoRelacionado().getDonante().obtenerApellido()
                        : "Desconocido";
                    
                    dtos.add(new VisitaDTO(
                        v.obtenerFechaFormateada(),
                        v.obtenerObservacion(),
                        v.obtenerResultado().toString(),
                        donante
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.disconnect();
        }
        return dtos;
    }

    // Helpers de conexión y mapeo
    private void rollback(Connection conn) {
        try { if (conn != null) conn.rollback(); } catch (SQLException e) { e.printStackTrace(); }
    }

    private void closeConnection(Connection conn) {
        try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        ConnectionManager.disconnect();
    }

    private OrdenRetiroDTO mapearOrdenADTO(OrdenRetiro o) {
        String vol = o.obtenerVoluntarioPrincipal() != null ? o.obtenerVoluntarioPrincipal().obtenerNombre() + " " + o.obtenerVoluntarioPrincipal().obtenerApellido() : "Sin Voluntario";
        String don = o.obtenerDonante() != null ? o.obtenerDonante().obtenerNombre() + " " + o.obtenerDonante().obtenerApellido() : "Sin Donante";
        String veh = o.obtenerVehiculo() != null ? o.obtenerVehiculo().getDescripcion() : "Sin Vehículo";
        
        return new OrdenRetiroDTO(
            o.getId(),
            o.obtenerNombreEstado(),
            o.obtenerFechaCreacion(),
            new ArrayList<>(), // Visitas se cargan aparte si es necesario
            don,
            veh,
            vol
        );
    }
/*
	@Override
	public void crearOrdenEntrega(int idBeneficiario, List<Integer> idsBienesAEntregar)
			throws ObjetoNuloException, ReglaNegocioException, CampoVacioException {
		// TODO Auto-generated method stub
		
	}
	*/
}