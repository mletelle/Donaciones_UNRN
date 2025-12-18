package ar.edu.unrn.seminario.api;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ar.edu.unrn.seminario.accesos.*;
import ar.edu.unrn.seminario.dto.*;
import ar.edu.unrn.seminario.exception.*;
import ar.edu.unrn.seminario.modelo.*;
import ar.edu.unrn.seminario.servicios.BienMapper;

public class PersistenceApi implements IApi {

    private final RolDao rolDao;
    private final UsuarioDao usuarioDao;
    private final PedidosDonacionDao pedidoDao;
    private final BienDao bienDao;
    private final OrdenRetiroDao ordenDao;
    private final OrdenEntregaDao ordenEntregaDao;
    private final VehiculoDao vehiculoDao;
    private final VisitaDao visitaDao;

    public PersistenceApi() {
        this.rolDao = new RolDAOJDBC();
        this.usuarioDao = new UsuarioDAOJDBC();
        this.pedidoDao = new PedidosDonacionDAOJDBC();
        this.bienDao = new BienDAOJDBC();
        this.ordenDao = new OrdenRetiroDAOJDBC();
        this.ordenEntregaDao = new OrdenEntregaDAOJDBC();
        this.vehiculoDao = new VehiculoDAOJDBC();
        this.visitaDao = new VisitaDAOJDBC();
    }

    public PersistenceApi(RolDao rolDao, UsuarioDao usuarioDao, PedidosDonacionDao pedidoDao,
                          BienDao bienDao, OrdenRetiroDao ordenDao, OrdenEntregaDao ordenEntregaDao,
                          VehiculoDao vehiculoDao, VisitaDao visitaDao) {
        this.rolDao = rolDao;
        this.usuarioDao = usuarioDao;
        this.pedidoDao = pedidoDao;
        this.bienDao = bienDao;
        this.ordenDao = ordenDao;
        this.ordenEntregaDao = ordenEntregaDao;
        this.vehiculoDao = vehiculoDao;
        this.visitaDao = visitaDao;
    }

    @Override
    public List<BienDTO> obtenerInventario() {
        try {
            List<Bien> bienesEnStock = bienDao.findByEstadoInventario(EstadoBien.EN_STOCK.name());
            return bienesEnStock.stream()
                    .map(BienMapper::entidadADTOVisual)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("error al obtener inventario: " + e.getMessage(), e);
        }
    }

    @Override
    public void actualizarBienInventario(BienDTO bienDTO)
            throws ObjetoNuloException, CampoVacioException, ReglaNegocioException {
        try {
            if (bienDTO.getId() <= 0) throw new ObjetoNuloException("id invalido");

            Bien bienDb = bienDao.findById(bienDTO.getId());
            if (bienDb == null) throw new ObjetoNuloException("el bien no existe");
            
            bienDb.actualizarDatos(bienDTO.getCantidad(), bienDTO.getDescripcion(), bienDTO.getFechaVencimiento());
            bienDao.update(bienDb);
        } catch (Exception e) {
            throw new RuntimeException("error en la carga del bien: " + e.getMessage(), e);
        }
    }

    @Override
    public void darDeBajaBien(int idBien, String motivo) throws ObjetoNuloException, ReglaNegocioException {
        try {
            Bien bienDb = bienDao.findById(idBien);
            if (bienDb == null) throw new ObjetoNuloException("el bien no existe");
            
            bienDb.darDeBaja(motivo);
            bienDao.update(bienDb);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void registrarVisita(int idOrdenRetiro, int idPedido, LocalDateTime fechaHora, String resultado, String observacion)
            throws ObjetoNuloException, CampoVacioException, ReglaNegocioException {
        try {
            ResultadoVisita resEnum = ResultadoVisita.fromString(resultado);
            visitaDao.registrarVisitaCompleta(idOrdenRetiro, idPedido, resEnum.name(), observacion);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<UsuarioDTO> obtenerDonantes() {
        try {
            return usuarioDao.findByRol(Rol.ROL_DONANTE).stream()
                    .map(u -> new UsuarioDTO(u.getUsuario(), u.getNombre(), u.getApellido(), u.getDni(), u.obtenerDireccion() != null ? u.obtenerDireccion() : "", 3))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("error al obtener donantes: " + e.getMessage(), e);
        }
    }

    @Override
    public List<PedidoDonacionDTO> obtenerTodosPedidos() {
        try {
            return pedidoDao.findAll().stream()
                    .map(p -> new PedidoDonacionDTO(
                            p.getId(),
                            p.obtenerFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                            p.describirTipoVehiculo(),
                            p.getDonante().getDni(),
                            p.getDonante().getNombre() + " " + p.getDonante().getApellido(),
                            p.obtenerEstado()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("error al obtener pedidos: " + e.getMessage(), e);
        }
    }

    @Override
    public void registrarPedidoDonacion(PedidoDonacionDTO pedidoDTO) throws CampoVacioException, ObjetoNuloException {
        try {
            Usuario donante = usuarioDao.findByDni(pedidoDTO.getDonanteId());
            if (donante == null) throw new ObjetoNuloException("donante no encontrado");

            List<Bien> bienes = pedidoDTO.getBienes().stream()
                    .map(dto -> {
                        try {
                            return BienMapper.toEntity(dto);
                        } catch (Exception e) {
                            throw new RuntimeException("error al convertir bien: " + e.getMessage(), e);
                        }
                    })
                    .collect(Collectors.toList());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDateTime fecha = LocalDate.parse(pedidoDTO.getFecha(), formatter).atStartOfDay();
            TipoVehiculo tipoVehiculo = TipoVehiculo.valueOf(pedidoDTO.getTipoVehiculo().toUpperCase());

            PedidosDonacion pedido = new PedidosDonacion(fecha, (ArrayList<Bien>) bienes, tipoVehiculo, donante);
            pedidoDao.create(pedido);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<PedidoDonacionDTO> obtenerPedidosPendientes() {
        try {
            return pedidoDao.findAllPendientes().stream()
                    .map(p -> new PedidoDonacionDTO(
                            p.getId(),
                            p.obtenerFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                            p.describirTipoVehiculo(),
                            p.getDonante().getDni(),
                            p.getDonante().getNombre() + " " + p.getDonante().getApellido()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("error al obtener pedidos pendientes: " + e.getMessage(), e);
        }
    }

    @Override
    public List<PedidoDonacionDTO> obtenerPedidosDeOrden(int idOrden) {
        try {
            return pedidoDao.findByOrden(idOrden).stream()
                    .map(p -> new PedidoDonacionDTO(
                            p.getId(),
                            p.getDonante().getNombre() + " " + p.getDonante().getApellido(),
                            p.getDonante().obtenerDireccion(), 
                            p.obtenerEstado()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("error al obtener pedidos de orden: " + e.getMessage(), e);
        }
    }

    @Override
    public String obtenerNombreDonantePorId(int idPedido) {
        try {
            PedidosDonacion pedido = pedidoDao.findById(idPedido);
            if (pedido != null && pedido.getDonante() != null) {
                return pedido.getDonante().getNombre() + " " + pedido.getDonante().getApellido();
            }
            return "";
        } catch (Exception e) {
            throw new RuntimeException("error al obtener nombre de donante: " + e.getMessage(), e);
        }
    }

    @Override
    public void crearOrdenRetiro(List<Integer> idsPedidos, int idVoluntario, String tipoVehiculo)
            throws ReglaNegocioException, ObjetoNuloException {
        try {
            List<PedidosDonacion> pedidos = pedidoDao.findByIds(idsPedidos);
            if (pedidos == null || pedidos.size() != idsPedidos.size()) {
                throw new ObjetoNuloException("no se encontraron todos los pedidos");
            }
            
            for (PedidosDonacion p : pedidos) {
                if (p.obtenerEstadoPedido() != EstadoPedido.PENDIENTE) {
                    throw new ReglaNegocioException("pedido " + p.getId() + " no esta pendiente");
                }
                if (p.obtenerOrden() != null) {
                    throw new ReglaNegocioException("pedido " + p.getId() + " ya tiene orden");
                }
            }

            Usuario voluntario = usuarioDao.findByDni(idVoluntario);
            if (voluntario == null || voluntario.getRol().getCodigo() != 2) {
                throw new ObjetoNuloException("voluntario invalido");
            }

            Vehiculo vehiculo = vehiculoDao.findDisponible(tipoVehiculo);
            if (vehiculo == null) throw new ReglaNegocioException("no hay vehiculos disponibles");

            OrdenRetiro orden = new OrdenRetiro(pedidos, null);
            orden.asignarVehiculo(vehiculo);
            orden.asignarVoluntario(voluntario);

            ordenDao.crearOrdenConPedidos(orden, idsPedidos);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<OrdenRetiroDTO> obtenerOrdenesDeRetiro(String estado) {
        try {
            return ordenDao.findByEstado(estado).stream()
                    .map(this::mapearOrdenADTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("error al obtener ordenes de retiro: " + e.getMessage(), e);
        }
    }

    @Override
    public List<OrdenRetiroDTO> obtenerTodasOrdenesRetiro() {
        try {
            return ordenDao.findAll().stream()
                    .map(this::mapearOrdenADTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("error al obtener todas las ordenes de retiro: " + e.getMessage(), e);
        }
    }

    @Override
    public List<OrdenRetiroDTO> obtenerOrdenesAsignadas(String voluntarioUser) {
        try {
            return ordenDao.findByVoluntario(voluntarioUser).stream()
                    .map(this::mapearOrdenADTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("error al obtener ordenes asignadas: " + e.getMessage(), e);
        }
    }
    
    private OrdenRetiroDTO mapearOrdenADTO(OrdenRetiro orden) {
        Usuario voluntario = orden.obtenerVoluntarioPrincipal();
        String nombreVol = (voluntario != null) ? voluntario.getNombre() + " " + voluntario.getApellido() : "Sin asignar";
        
        String estado = orden.obtenerNombreEstado();
        int cantidadPedidos = (orden.obtenerPedidos() != null) ? orden.obtenerPedidos().size() : 0;

        java.time.LocalDateTime ldt = orden.obtenerFechaCreacion();
        
        java.sql.Timestamp fechaReal = java.sql.Timestamp.valueOf(ldt);
        

        String fechaVisual = ldt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        String vehiculoStr = "Sin Vehículo";
        if (orden.obtenerVehiculo() != null) {
            vehiculoStr = orden.obtenerVehiculo().getPatente() + " (" + orden.obtenerVehiculo().getTipoVeh() + ")";
        }

        String donanteStr = "Sin Donante";
        if (orden.obtenerPedidos() != null && !orden.obtenerPedidos().isEmpty()) {
            donanteStr = orden.obtenerPedidos().stream()
                .map(p -> p.getDonante().getNombre() + " " + p.getDonante().getApellido())
                .distinct()
                .collect(Collectors.joining(", "));
        }
        
        return new OrdenRetiroDTO(
            orden.obtenerId(), 
            fechaReal,      
            fechaVisual,    
            estado, 
            nombreVol, 
            cantidadPedidos, 
            donanteStr, 
            vehiculoStr
        );
    }
    
    @Override
    public List<UsuarioDTO> obtenerVoluntarios() {
        try {
            return usuarioDao.findByRol(Rol.ROL_VOLUNTARIO).stream()
                    .map(v -> new UsuarioDTO(v.getUsuario(), v.getNombre(), v.getApellido(), v.getDni(), v.obtenerDireccion(), 2))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("error al obtener voluntarios: " + e.getMessage(), e);
        }
    }

    @Override
    public List<UsuarioDTO> obtenerBeneficiarios() {
        try {
            return usuarioDao.findByRol(Rol.ROL_BENEFICIARIO).stream()
                    .map(b -> new UsuarioDTO(b.getUsuario(), b.getNombre(), b.getApellido(), b.getDni(), b.obtenerDireccion(), 4))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("error al obtener beneficiarios: " + e.getMessage(), e);
        }
    }

    @Override
    public List<VisitaDTO> obtenerVisitasPorVoluntario(UsuarioDTO voluntario) {
        List<VisitaDTO> dtos = new ArrayList<>();
        try {
            Usuario vol = usuarioDao.findByDni(voluntario.getId());
            if (vol != null) {
                List<Visita> visitas = visitaDao.findByVoluntario(vol);
                for (Visita v : visitas) {
                    String donante = (v.getPedidoRelacionado() != null && v.getPedidoRelacionado().getDonante() != null)
                            ? v.getPedidoRelacionado().getDonante().getNombre() + " " + v.getPedidoRelacionado().getDonante().getApellido()
                            : "Desconocido";
                    dtos.add(new VisitaDTO(v.obtenerFechaFormateada(), v.obtenerObservacion(), v.obtenerResultado().toString(), donante));
                }
            }
            return dtos;
        } catch (Exception e) {
            throw new RuntimeException("error al obtener visitas: " + e.getMessage(), e);
        }
    }

    // Ordenes de entrega

    @Override
    public void crearOrdenEntrega(String userBeneficiario, Map<Integer, Integer> bienesYCantidades, String userVoluntario)
            throws ObjetoNuloException, ReglaNegocioException, CampoVacioException {
        try {
            Usuario beneficiario = usuarioDao.find(userBeneficiario);
            if (beneficiario == null) throw new ObjetoNuloException("beneficiario no existe");

            Usuario voluntario = null;
            if (userVoluntario != null && !userVoluntario.isEmpty()) {
                voluntario = usuarioDao.find(userVoluntario);
                if (voluntario == null) throw new ObjetoNuloException("voluntario no encontrado");
            }

            for (Map.Entry<Integer, Integer> entry : bienesYCantidades.entrySet()) {
                Bien bienOriginal = bienDao.findById(entry.getKey());
                if (bienOriginal == null) throw new ObjetoNuloException("bien id " + entry.getKey() + " no existe");
                if (bienOriginal.getCantidad() < entry.getValue()) {
                    throw new ReglaNegocioException("stock insuficiente para el bien " + entry.getKey());
                }
            }

            OrdenEntrega orden = new OrdenEntrega(beneficiario, new ArrayList<>());
            if (voluntario != null) {
                orden.setVoluntario(voluntario);
            }
            
            ordenEntregaDao.crearOrdenConBienes(orden, bienesYCantidades);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public List<OrdenEntregaDTO> obtenerEntregasPorBeneficiario(String username) {
        try {
            List<OrdenEntrega> ordenes = ordenEntregaDao.findByBeneficiario(username);
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
            
            return ordenes.stream().map(o -> {
                String resumenBienes = "Sin bienes detallados";
                if (o.getBienes() != null && !o.getBienes().isEmpty()) {
                    resumenBienes = o.getBienes().stream().map(Object::toString).collect(Collectors.joining(", "));
                }
                String fechaStr = o.getFechaGeneracion() != null ? sdf.format(o.getFechaGeneracion()) : "-";
                return new OrdenEntregaDTO(o.getId(), fechaStr, o.getEstado().toString(), resumenBienes);
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("error al obtener entregas de beneficiario: " + e.getMessage(), e);
        }
    }

    @Override
    public List<OrdenEntregaDTO> obtenerEntregasPendientes() {
        try {
            return ordenEntregaDao.findAllPendientes().stream().map(o -> {
                 String resumen = (o.getBienes() != null && !o.getBienes().isEmpty()) 
                         ? o.getBienes().stream().map(Object::toString).collect(Collectors.joining(", ")) 
                         : "Sin detalle";
                 return new OrdenEntregaDTO(o.getId(), o.getFechaGeneracion().toString(), o.getEstado().toString(), resumen);
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("error al obtener entregas pendientes: " + e.getMessage(), e);
        }
    }

    @Override
    public List<OrdenEntregaDTO> obtenerTodasOrdenesEntrega() {
        try {
            return ordenEntregaDao.findAll().stream()
                    .map(o -> new OrdenEntregaDTO(
                            o.getId(),
                            o.getFechaGeneracion().toString(),
                            o.getEstado().toString(),
                            o.getBeneficiario() != null ? o.getBeneficiario().getNombre() + " " + o.getBeneficiario().getApellido() : "-",
                            o.getVoluntario() != null ? o.getVoluntario().getNombre() + " " + o.getVoluntario().getApellido() : "-"
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("error al obtener todas las ordenes de entrega: " + e.getMessage(), e);
        }
    }

    @Override
    public void completarEntrega(int idOrden, String usuarioVoluntario) throws ObjetoNuloException {
        try {
            OrdenEntrega orden = ordenEntregaDao.findById(idOrden);
            if (orden == null) throw new ObjetoNuloException("orden no encontrada");
            
            Usuario voluntario = usuarioDao.find(usuarioVoluntario);
            orden.setVoluntario(voluntario);
            orden.marcarComoCompletada();
            
            ordenEntregaDao.update(orden);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Metodos de usuario genericos

    @Override
    public void registrarUsuario(String username, String password, String email, String nombre, Integer codigoRol,
            String apellido, int dni, String direccion, String necesidad, int personasCargo, String prioridad)
            throws CampoVacioException, ObjetoNuloException, UsuarioInvalidoException {
        try {
            Rol rol = rolDao.findById(codigoRol);
            if (rol == null) throw new ObjetoNuloException("rol no encontrado");
            Usuario usuario = new Usuario(username, password, nombre, email, rol, apellido, dni, direccion, necesidad, personasCargo, prioridad);
            usuarioDao.create(usuario);
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("dni_UNIQUE") || e.getMessage().contains("Duplicate"))) {
                throw new UsuarioInvalidoException("ya existe un usuario con el dni " + dni);
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<UsuarioDTO> obtenerUsuarios() {
        try {
            List<Usuario> usuarios = usuarioDao.findAll();
            return usuarios.stream().map(u -> new UsuarioDTO(
                u.getUsuario(), u.getContrasena(), u.getNombre(), u.getEmail(), 
                u.getRol().getNombre(), u.isActivo(), u.obtenerEstado()
            )).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("error al obtener usuarios: " + e.getMessage(), e);
        }
    }

    @Override
    public UsuarioDTO obtenerUsuario(String username) {
        try {
            Usuario u = usuarioDao.find(username);
            if (u != null) {
                return new UsuarioDTO(u.getUsuario(), u.getContrasena(), u.getNombre(), 
                    u.getEmail(), u.getRol().getNombre(), u.isActivo(), u.obtenerEstado());
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("error al obtener usuario: " + e.getMessage(), e);
        }
    }

    @Override 
    public void eliminarUsuario(String username) {}
    
    @Override 
    public List<RolDTO> obtenerRoles() {
        try {
            return rolDao.findAll().stream()
                .map(r -> new RolDTO(r.getCodigo(), r.getNombre(), r.isActivo()))
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("error al obtener roles: " + e.getMessage(), e);
        }
    }
    
    @Override 
    public List<RolDTO> obtenerRolesActivos() {
        try {
            return rolDao.findAll().stream()
                .filter(Rol::isActivo)
                .map(r -> new RolDTO(r.getCodigo(), r.getNombre(), r.isActivo()))
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("error al obtener roles activos: " + e.getMessage(), e);
        }
    }
    
    @Override 
    public void guardarRol(Integer codigo, String descripcion, boolean estado) throws CampoVacioException {}
    @Override public RolDTO obtenerRolPorCodigo(Integer codigo) { return null; }
    @Override public void activarRol(Integer codigo) {}
    @Override public void desactivarRol(Integer codigo) {}
    @Override public void activarUsuario(String username) { cambiarEstadoUsuario(username, true); }
    @Override public void desactivarUsuario(String username) { cambiarEstadoUsuario(username, false); }

    private void cambiarEstadoUsuario(String username, boolean activar) {
        try {
            Usuario usuario = usuarioDao.find(username);
            if (usuario != null) {
                if (activar) usuario.activar(); 
                else usuario.desactivar();
                usuarioDao.update(usuario);
            }
        } catch (Exception e) {
            throw new RuntimeException("error al cambiar estado de usuario: " + e.getMessage(), e);
        }
    }
    
}