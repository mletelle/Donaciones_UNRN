package ar.edu.unrn.seminario.api;

import ar.edu.unrn.seminario.accesos.*;
import ar.edu.unrn.seminario.dto.*;
import ar.edu.unrn.seminario.exception.*;
import ar.edu.unrn.seminario.modelo.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class PersistenceApi implements IApi {

    private RolDao rolDao;
    private UsuarioDao usuarioDao;
    private PedidosDonacionDao pedidoDao;
    private BienDao bienDao;
    private OrdenRetiroDao ordenDao;
    private OrdenEntregaDao ordenEntregaDao;
    private VehiculoDao vehiculoDao;
    private VisitaDao visitaDao;

    public PersistenceApi() {
        rolDao = new RolDAOJDBC();
        usuarioDao = new UsuarioDAOJDBC();
        pedidoDao = new PedidosDonacionDAOJDBC();
        bienDao = new BienDAOJDBC();
        ordenDao = new OrdenRetiroDAOJDBC();
        ordenEntregaDao = new OrdenEntregaDAOJDBC();
        vehiculoDao = new VehiculoDAOJDBC();
        visitaDao = new VisitaDAOJDBC();
    }

    @Override
    public List<BienDTO> obtenerInventario() {
        try {
            List<Bien> bienesEnStock = bienDao.findByEstadoInventario(EstadoBien.EN_STOCK.name());
            return bienesEnStock.stream()
                    .map(this::convertirEntidadADTOVisual)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private BienDTO convertirEntidadADTOVisual(Bien bien) {
        String categoriaStr = mapCategoriaToString(bien.obtenerCategoria());
        String estadoStr = (bien.obtenerTipo() == TipoBien.ALIMENTO) ? "Nuevo" : "Usado";
        
        String vencimientoStr = "-";
        LocalDate fechaLocalDate = null; 

        if (bien.getFecVec() != null) {
            fechaLocalDate = bien.getFecVec().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

            vencimientoStr = fechaLocalDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        BienDTO dto = new BienDTO();
        dto.setId(bien.getId());
        dto.setDescripcion(bien.getDescripcion());
        dto.setCantidad(bien.obtenerCantidad());
        dto.setCategoria(mapEnumCategoriaToDTO(bien.obtenerCategoria()));
        dto.setTipo(mapEnumTipoToDTO(bien.obtenerTipo()));
        
        dto.setCategoriaTexto(categoriaStr);
        dto.setEstadoTexto(estadoStr);
        dto.setFechaVencimiento(fechaLocalDate); 
        dto.setVencimientoTexto(vencimientoStr); 
        
        return dto;
    }
    
    @Override
    public void actualizarBienInventario(BienDTO bienDTO) throws ObjetoNuloException, CampoVacioException, ReglaNegocioException {
        try {
            Bien bienDb = bienDao.findById(bienDTO.getId());
            if (bienDb == null) throw new ObjetoNuloException("El bien no existe.");

            Date fechaVenc = (bienDTO.getFechaVencimiento() != null) 
                ? Date.from(bienDTO.getFechaVencimiento().atStartOfDay(ZoneId.systemDefault()).toInstant()) 
                : null;

            bienDb.actualizarDatos(bienDTO.getCantidad(), bienDTO.getDescripcion(), fechaVenc);
            
            bienDao.update(bienDb);
        } catch (PersistenceException e) {
            throw new RuntimeException("Error en la base de datos: " + e.getMessage());
        }
    }

    @Override
    public void darDeBajaBien(int idBien, String motivo) throws ObjetoNuloException, ReglaNegocioException {
        try {
            Bien bienDb = bienDao.findById(idBien);
            if (bienDb == null) throw new ObjetoNuloException("El bien no existe");
            bienDb.darDeBaja(motivo);
            bienDao.update(bienDb);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

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
            return usuarioDao.findByRol(3).stream()
                    .map(u -> new UsuarioDTO(u.getUsuario(), u.getNombre(), u.getApellido(), u.getDni(), u.obtenerDireccion() != null ? u.obtenerDireccion() : "", 3))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
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
            // en caso de error, devolvemos lista vacia para que la UI no falle
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public void registrarPedidoDonacion(PedidoDonacionDTO pedidoDTO) throws CampoVacioException, ObjetoNuloException {
        try {
            Usuario donante = usuarioDao.findByDni(pedidoDTO.getDonanteId());
            if (donante == null) throw new ObjetoNuloException("donante no encontrado");

            List<Bien> bienes = new ArrayList<>();
            for (ar.edu.unrn.seminario.dto.BienDTO dto : pedidoDTO.getBienes()) {
                TipoBien tipo = mapDTOTipoToEnum(dto.getTipo());
                CategoriaBien categoria = mapDTOCategoriaToEnum(dto.getCategoria());
                Bien bien = new Bien(tipo, dto.getCantidad(), categoria);
                if (dto.getDescripcion() != null) bien.setDescripcion(dto.getDescripcion());
                if (dto.getFechaVencimiento() != null) {
                    java.time.ZoneId zoneId = java.time.ZoneId.systemDefault();
                    bien.setFecVec(java.util.Date.from(dto.getFechaVencimiento().atStartOfDay(zoneId).toInstant()));
                }
                bienes.add(bien);
            }

            // las fechas vienen como string dd/MM/yyyy desde la gui
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDateTime fecha = java.time.LocalDate.parse(pedidoDTO.getFecha(), formatter).atStartOfDay();

            TipoVehiculo tipoVehiculo = TipoVehiculo.valueOf(pedidoDTO.getTipoVehiculo().toUpperCase());

            PedidosDonacion pedido = new PedidosDonacion(
                fecha, 
                (ArrayList<Bien>) bienes, 
                tipoVehiculo, 
                donante
            );

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
            e.printStackTrace();
            return new ArrayList<>();
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
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public String obtenerNombreDonantePorId(int idPedido) {
        try {
            PedidosDonacion pedido = pedidoDao.findById(idPedido);
            if (pedido != null && pedido.getDonante() != null) {
                return pedido.getDonante().getNombre() + " " + pedido.getDonante().getApellido();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void crearOrdenRetiro(List<Integer> idsPedidos, int dniVoluntario, String tipoVehiculo)
            throws ReglaNegocioException, ObjetoNuloException {
        try {
            Usuario voluntario = usuarioDao.findByDni(dniVoluntario);
            if (voluntario == null || !"VOLUNTARIO".equalsIgnoreCase(voluntario.getRol().getNombre())) {
                throw new ObjetoNuloException("El usuario ingresado no es un Voluntario vAlido");
            }
            Vehiculo vehiculo = vehiculoDao.findDisponible(tipoVehiculo);
            if (vehiculo == null) {
                throw new ReglaNegocioException("No hay vehiculos disponibles del tipo: " + tipoVehiculo);
            }
            List<PedidosDonacion> pedidos = pedidoDao.findByIds(idsPedidos);
            if (pedidos == null || pedidos.size() != idsPedidos.size()) {
                throw new ObjetoNuloException("Algunos pedidos no fueron encontrados");
            }
            for (PedidosDonacion p : pedidos) {
                if (p.obtenerEstadoPedido() != EstadoPedido.PENDIENTE || p.obtenerOrden() != null) {
                    throw new ReglaNegocioException("El pedido " + p.getId() + " no esta disponible para retiro");
                }
            }
            OrdenRetiro orden = new OrdenRetiro(pedidos, null);
            orden.asignarVehiculo(vehiculo);
            orden.asignarVoluntario(voluntario);
            ordenDao.crearOrdenConPedidos(orden, idsPedidos);
        } catch (PersistenceException e) {
            throw new RuntimeException("Error de base de datos al crear Orden de Retiro: " + e.getMessage());
        }
    }

    @Override
    public List<OrdenRetiroDTO> obtenerOrdenesDeRetiro(String estado) {
        try {
            return ordenDao.findByEstado(estado).stream()
                    .map(this::mapearOrdenADTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<OrdenRetiroDTO> obtenerTodasOrdenesRetiro() {
        try {
            return ordenDao.findAll().stream()
                    .map(this::mapearOrdenADTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<OrdenRetiroDTO> obtenerOrdenesAsignadas(String voluntarioUser) {
        try {
            return ordenDao.findByVoluntario(voluntarioUser).stream()
                    .map(this::mapearOrdenADTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private OrdenRetiroDTO mapearOrdenADTO(OrdenRetiro orden) {
        Usuario voluntario = orden.obtenerVoluntarioPrincipal();
        String nombreVol = (voluntario != null) ? voluntario.getNombre() + " " + voluntario.getApellido() : "Sin asignar";
        
        String estado = orden.obtenerNombreEstado();
        String fecha = orden.obtenerFechaCreacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        int cantidadPedidos = orden.obtenerPedidos().size();
        
        return new OrdenRetiroDTO(orden.obtenerId(), fecha, estado, nombreVol, cantidadPedidos);
    }

    @Override
    public List<UsuarioDTO> obtenerVoluntarios() {
        try {
            return usuarioDao.findByRol(2).stream()
                    .map(v -> new UsuarioDTO(v.getUsuario(), v.getNombre(), v.getApellido(), v.getDni(), v.obtenerDireccion(), 2))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<UsuarioDTO> obtenerBeneficiarios() {
        try {
            return usuarioDao.findByRol(4).stream()
                    .map(b -> new UsuarioDTO(b.getUsuario(), b.getNombre(), b.getApellido(), b.getDni(), b.obtenerDireccion(), 4))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dtos;
    }

    // Ordenes de entrega

    @Override
    public void crearOrdenEntrega(String userBeneficiario, Map<Integer, Integer> bienesYCantidades, String userVoluntario)
            throws ObjetoNuloException, ReglaNegocioException, CampoVacioException {
        try {
            Usuario beneficiario = usuarioDao.find(userBeneficiario);
            if (beneficiario == null) throw new ObjetoNuloException("Beneficiario no encontrado");
            Usuario voluntario = null;
            if (userVoluntario != null && !userVoluntario.isEmpty()) {
                voluntario = usuarioDao.find(userVoluntario);
            }
            List<Bien> bienesParaOrden = new ArrayList<>();
            for (Map.Entry<Integer, Integer> entry : bienesYCantidades.entrySet()) {
                int idBienOriginal = entry.getKey();
                int cantidadSolicitada = entry.getValue();
                Bien bienOriginal = bienDao.findById(idBienOriginal);
                if (bienOriginal == null) throw new ObjetoNuloException("Bien ID " + idBienOriginal + " no existe.");
                if (cantidadSolicitada == bienOriginal.getCantidad()) {
                    bienOriginal.entregar(); 
                    bienDao.update(bienOriginal);
                    bienesParaOrden.add(bienOriginal);
                } else {
                    int idPedidoPadre = bienDao.obtenerIdPedidoDeBien(idBienOriginal);
                    Bien bienNuevo = bienOriginal.fraccionarParaEntrega(cantidadSolicitada);
                    int nuevoId = bienDao.create(bienNuevo, idPedidoPadre); 
                    bienNuevo.setId(nuevoId);
                    bienDao.update(bienOriginal);                  
                    bienesParaOrden.add(bienNuevo);
                }
            }

            OrdenEntrega orden = new OrdenEntrega(beneficiario, bienesParaOrden);
            if (voluntario != null) {
                orden.setVoluntario(voluntario);
            }
            ordenEntregaDao.crearOrdenConBienes(orden, bienesYCantidades);
            
        } catch (ObjetoNuloException | ReglaNegocioException | CampoVacioException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("error al procesar la entrega: " + e.getMessage(), e);
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
            e.printStackTrace();
            return new ArrayList<>();
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
            e.printStackTrace();
            return new ArrayList<>();
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
            e.printStackTrace();
            return new ArrayList<>();
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
            e.printStackTrace();
            return new ArrayList<>();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
            e.printStackTrace();
            return new ArrayList<>();
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
            e.printStackTrace();
            return new ArrayList<>();
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
            e.printStackTrace();
        }
    }
    
    private TipoBien mapDTOTipoToEnum(int categoriaDto) {
        switch (categoriaDto) {
            case BienDTO.CATEGORIA_ALIMENTOS:
            case BienDTO.CATEGORIA_MEDICAMENTOS:
                return TipoBien.ALIMENTO;
            case BienDTO.CATEGORIA_ROPA:
                return TipoBien.ROPA;
            case BienDTO.CATEGORIA_MUEBLES:
                return TipoBien.MOBILIARIO;
            case BienDTO.CATEGORIA_HIGIENE:
                return TipoBien.HIGIENE;
            default:
                return TipoBien.ALIMENTO; // tippo por defecto
        }
    }
    
    private CategoriaBien mapDTOCategoriaToEnum(int categoria) {
        switch (categoria) {
            case BienDTO.CATEGORIA_ROPA: return CategoriaBien.ROPA;
            case BienDTO.CATEGORIA_MUEBLES: return CategoriaBien.MUEBLES;
            case BienDTO.CATEGORIA_ALIMENTOS: return CategoriaBien.ALIMENTOS;
            case BienDTO.CATEGORIA_ELECTRODOMESTICOS: return CategoriaBien.ELECTRODOMESTICOS;
            case BienDTO.CATEGORIA_HERRAMIENTAS: return CategoriaBien.HERRAMIENTAS;
            case BienDTO.CATEGORIA_JUGUETES: return CategoriaBien.JUGUETES;
            case BienDTO.CATEGORIA_LIBROS: return CategoriaBien.LIBROS;
            case BienDTO.CATEGORIA_MEDICAMENTOS: return CategoriaBien.MEDICAMENTOS;
            case BienDTO.CATEGORIA_HIGIENE: return CategoriaBien.HIGIENE;
            default: return CategoriaBien.OTROS;
        }
    }
    
    private int mapEnumCategoriaToDTO(CategoriaBien categoria) {
        switch (categoria) {
            case ROPA: return BienDTO.CATEGORIA_ROPA;
            case MUEBLES: return BienDTO.CATEGORIA_MUEBLES;
            case ALIMENTOS: return BienDTO.CATEGORIA_ALIMENTOS;
            case ELECTRODOMESTICOS: return BienDTO.CATEGORIA_ELECTRODOMESTICOS;
            case HERRAMIENTAS: return BienDTO.CATEGORIA_HERRAMIENTAS;
            case JUGUETES: return BienDTO.CATEGORIA_JUGUETES;
            case LIBROS: return BienDTO.CATEGORIA_LIBROS;
            case MEDICAMENTOS: return BienDTO.CATEGORIA_MEDICAMENTOS;
            case HIGIENE: return BienDTO.CATEGORIA_HIGIENE;
            default: return BienDTO.CATEGORIA_OTROS;
        }
    }
    
    private int mapEnumTipoToDTO(TipoBien tipo) {
        return BienDTO.TIPO_NUEVO;
    }

    private String mapCategoriaToString(CategoriaBien categoria) {
        return categoria.toString();
    }

}

