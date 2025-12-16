package ar.edu.unrn.seminario.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ar.edu.unrn.seminario.accesos.*;
import ar.edu.unrn.seminario.dto.*;
import ar.edu.unrn.seminario.exception.*;
import ar.edu.unrn.seminario.modelo.*;

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

    // Inventario y bienes

    @Override
    public List<BienDTO> obtenerInventario() {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            List<Bien> bienesEnStock = bienDao.findByEstadoInventario(Bien.ESTADO_EN_STOCK, conn);
            return bienesEnStock.stream()
                    .map(this::convertirEntidadADTOVisual)
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            ConnectionManager.disconnect();
        }
    }

    private BienDTO convertirEntidadADTOVisual(Bien bien) {
        String categoriaStr = mapCategoriaToString(bien.obtenerCategoria());
        String estadoStr = (bien.obtenerTipo() == BienDTO.TIPO_NUEVO) ? "Nuevo" : "Usado";
        String vencimientoStr = "-";
        if (bien.getFecVec() != null) {
            java.time.LocalDate localDate = new java.sql.Date(bien.getFecVec().getTime()).toLocalDate();
            vencimientoStr = localDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        BienDTO dto = new BienDTO(
                categoriaStr,
                bien.getDescripcion(),
                bien.obtenerCantidad(),
                estadoStr,
                vencimientoStr);
        dto.setId(bien.getId());
        dto.setCategoria(bien.obtenerCategoria());
        dto.setTipo(bien.obtenerTipo());
        return dto;
    }

    public void actualizarBienInventario(BienDTO bienDTO)
            throws ObjetoNuloException, CampoVacioException, ReglaNegocioException {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);
            if (bienDTO.getId() <= 0) throw new ObjetoNuloException("ID inválido.");
            Bien bienDb = bienDao.findById(bienDTO.getId(), conn);
            if (bienDb == null) throw new ObjetoNuloException("El bien no existe.");
            if (bienDTO.getCantidad() < 0) throw new ReglaNegocioException("La cantidad no puede ser negativa.");
            
            bienDb.setCantidad(bienDTO.getCantidad());
            bienDb.setDescripcion(bienDTO.getDescripcion());
            bienDao.update(bienDb, conn);
            conn.commit();
        } catch (SQLException e) {
            rollback(conn);
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn);
        }
    }

    @Override
    public void darDeBajaBien(int idBien, String motivo) throws ObjetoNuloException, ReglaNegocioException {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);
            Bien bienDb = bienDao.findById(idBien, conn);
            if (bienDb == null) throw new ObjetoNuloException("El bien no existe.");
            
            bienDb.setEstadoInventario(Bien.ESTADO_BAJA);
            bienDb.setDescripcion(bienDb.getDescripcion() + " [BAJA: " + motivo + "]");
            bienDao.update(bienDb, conn);
            conn.commit();
        } catch (SQLException e) {
            rollback(conn);
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn);
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

    // Visitas

    @Override
    public void registrarVisita(int idOrdenRetiro, int idPedido, LocalDateTime fechaHora, String resultado, String observacion)
            throws ObjetoNuloException, CampoVacioException, ReglaNegocioException {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);
            
            PedidosDonacion pedido = pedidoDao.findByIdAndOrden(idPedido, idOrdenRetiro, conn);
            if (pedido == null) throw new ObjetoNuloException("Pedido no encontrado en esta orden.");
            
            OrdenRetiro orden = pedido.obtenerOrden();
            if (orden == null) {
                 orden = ordenDao.findById(idOrdenRetiro, conn);
                 pedido.asignarOrden(orden);
            }

            ResultadoVisita resEnum = ResultadoVisita.fromString(resultado);
            Visita visita = new Visita(fechaHora, resEnum, observacion);
            visita.setPedidoRelacionado(pedido);
            visitaDao.create(visita, idOrdenRetiro, idPedido, conn);

            if (resEnum == ResultadoVisita.RECOLECCION_EXITOSA) {
                pedido.marcarCompletado();
                bienDao.updateEstadoPorPedido(idPedido, Bien.ESTADO_EN_STOCK, conn);
            } else if (resEnum == ResultadoVisita.CANCELADO) {
                pedido.marcarCompletado();
            } else {
                pedido.marcarEnEjecucion();
            }

            pedidoDao.update(pedido, conn);
            if (orden != null) {
                orden.actualizarEstadoAutomatico();
                ordenDao.update(orden, conn);
            }

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

    // Usuarios

    @Override
    public List<UsuarioDTO> obtenerDonantes() {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            return usuarioDao.findByRol(Rol.CODIGO_DONANTE, conn).stream()
                    .map(u -> new UsuarioDTO(u.getUsuario(), u.getNombre(), u.getApellido(), u.getDni(), u.obtenerDireccion() != null ? u.obtenerDireccion() : "", Rol.CODIGO_DONANTE))
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            ConnectionManager.disconnect();
        }
    }

    @Override
    public List<PedidoDonacionDTO> obtenerTodosPedidos() {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            return pedidoDao.findAll(conn).stream()
                    .map(p -> new PedidoDonacionDTO(
                            p.getId(),
                            p.obtenerFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                            p.describirTipoVehiculo(),
                            p.getDonante().getDni(),
                            p.getDonante().getNombre() + " " + p.getDonante().getApellido(),
                            p.obtenerEstado()))
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            ConnectionManager.disconnect();
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

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDateTime fecha = java.time.LocalDate.parse(pedidoDTO.getFecha(), formatter).atStartOfDay();

            PedidosDonacion pedido = new PedidosDonacion(
                fecha, 
                (ArrayList<Bien>) bienes, 
                PedidosDonacion.convertirVehiculoAInt(pedidoDTO.getTipoVehiculo()), 
                donante
            );

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
                            p.getDonante().getNombre() + " " + p.getDonante().getApellido()))
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
                            p.getDonante().getNombre() + " " + p.getDonante().getApellido(),
                            p.getDonante().obtenerDireccion(), 
                            p.obtenerEstado()))
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

    // Ordenes de retiro

    @Override
    public void crearOrdenRetiro(List<Integer> idsPedidos, int idVoluntario, String tipoVehiculo)
            throws ReglaNegocioException, ObjetoNuloException {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);

            List<PedidosDonacion> pedidos = pedidoDao.findByIds(idsPedidos, conn);
            if (pedidos == null || pedidos.size() != idsPedidos.size()) {
                throw new ObjetoNuloException("No se encontraron todos los pedidos.");
            }
            for (PedidosDonacion p : pedidos) {
                if (p.obtenerOrden() != null) throw new ReglaNegocioException("Pedido " + p.getId() + " ya tiene orden.");
            }

            Usuario voluntario = usuarioDao.findByDni(idVoluntario, conn);
            if (voluntario == null || voluntario.getRol().getCodigo() != Rol.CODIGO_VOLUNTARIO) throw new ObjetoNuloException("Voluntario inválido.");

            Vehiculo vehiculo = vehiculoDao.findDisponible(tipoVehiculo, conn);
            if (vehiculo == null) throw new ReglaNegocioException("No hay vehículos disponibles.");

            OrdenRetiro orden = new OrdenRetiro(pedidos, null);
            orden.asignarVehiculo(vehiculo);
            orden.asignarVoluntario(voluntario);

            int idOrden = ordenDao.create(orden, conn);
            orden.setId(idOrden);

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
    public List<UsuarioDTO> obtenerVoluntarios() {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            return usuarioDao.findByRol(Rol.CODIGO_VOLUNTARIO, conn).stream()
                    .map(v -> new UsuarioDTO(v.getUsuario(), v.getNombre(), v.getApellido(), v.getDni(), v.obtenerDireccion(), Rol.CODIGO_VOLUNTARIO))
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            ConnectionManager.disconnect();
        }
    }

    @Override
    public List<UsuarioDTO> obtenerBeneficiarios() {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            return usuarioDao.findByRol(Rol.CODIGO_BENEFICIARIO, conn).stream()
                    .map(b -> new UsuarioDTO(b.getUsuario(), b.getNombre(), b.getApellido(), b.getDni(), b.obtenerDireccion(), Rol.CODIGO_BENEFICIARIO))
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            ConnectionManager.disconnect();
        }
    }

    @Override
    public List<VisitaDTO> obtenerVisitasPorVoluntario(UsuarioDTO voluntario) {
        Connection conn = null;
        List<VisitaDTO> dtos = new ArrayList<>();
        try {
            conn = ConnectionManager.getConnection();
            Usuario vol = usuarioDao.findByDni(voluntario.getId(), conn);
            if (vol != null) {
                List<Visita> visitas = visitaDao.findByVoluntario(vol, conn);
                for (Visita v : visitas) {
                    String donante = (v.getPedidoRelacionado() != null && v.getPedidoRelacionado().getDonante() != null)
                            ? v.getPedidoRelacionado().getDonante().getNombre() + " " + v.getPedidoRelacionado().getDonante().getApellido()
                            : "Desconocido";
                    dtos.add(new VisitaDTO(v.obtenerFechaFormateada(), v.obtenerObservacion(), v.obtenerResultado().toString(), donante));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.disconnect();
        }
        return dtos;
    }

    // Ordenes de entrega

    @Override
    public void crearOrdenEntrega(String userBeneficiario, Map<Integer, Integer> bienesYCantidades, String userVoluntario)
            throws ObjetoNuloException, ReglaNegocioException, CampoVacioException {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);

            Usuario beneficiario = usuarioDao.find(userBeneficiario, conn);
            if (beneficiario == null) throw new ObjetoNuloException("Beneficiario no existe");

            // Buscar Voluntario
            Usuario voluntario = null;
            if (userVoluntario != null && !userVoluntario.isEmpty()) {
                voluntario = usuarioDao.find(userVoluntario, conn);
                if (voluntario == null) throw new ObjetoNuloException("Voluntario no encontrado.");
            }

            List<Bien> bienesFinalesParaOrden = new ArrayList<>();

            // Procesar Split de Bienes
            for (Map.Entry<Integer, Integer> entry : bienesYCantidades.entrySet()) {
                int idBienOriginal = entry.getKey();
                int cantidadSolicitada = entry.getValue();

                Bien bienOriginal = bienDao.findById(idBienOriginal, conn);
                if (bienOriginal == null) throw new ObjetoNuloException("Bien ID " + idBienOriginal + " no existe.");
                
                if (!Bien.ESTADO_EN_STOCK.equals(bienOriginal.getEstadoInventario())) {
                    throw new ReglaNegocioException("El bien " + bienOriginal.getDescripcion() + " no está disponible.");
                }

                if (cantidadSolicitada > bienOriginal.getCantidad()) {
                    throw new ReglaNegocioException("Stock insuficiente para: " + bienOriginal.getDescripcion());
                }

                if (cantidadSolicitada == bienOriginal.getCantidad()) {
                    bienesFinalesParaOrden.add(bienOriginal);
                } else {
                    bienOriginal.setCantidad(bienOriginal.getCantidad() - cantidadSolicitada);
                    bienDao.update(bienOriginal, conn);

                    Bien bienParaEntregar = new Bien(bienOriginal.obtenerTipo(), cantidadSolicitada, bienOriginal.obtenerCategoria());
                    bienParaEntregar.setDescripcion(bienOriginal.getDescripcion());
                    bienParaEntregar.setFecVec(bienOriginal.getFecVec());
                    bienParaEntregar.setEstadoInventario(Bien.ESTADO_EN_STOCK);
                    
                    int idPedidoOrigen = obtenerIdPedidoDeBien(idBienOriginal, conn);
                    int idNuevoBien = bienDao.create(bienParaEntregar, idPedidoOrigen, conn);
                    bienParaEntregar.setId(idNuevoBien);
                    
                    bienesFinalesParaOrden.add(bienParaEntregar);
                }
            }

            OrdenEntrega orden = new OrdenEntrega(beneficiario, bienesFinalesParaOrden);
            
            if (voluntario != null) {
                orden.setVoluntario(voluntario);
            }

            int idOrden = ordenEntregaDao.create(orden, conn);

            for (Bien bien : bienesFinalesParaOrden) {
                bienDao.asociarAOrdenEntrega(bien.getId(), idOrden, Bien.ESTADO_ENTREGADO, conn);
            }

            conn.commit();
        } catch (SQLException e) {
            rollback(conn);
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn);
        }
    }
    
    private int obtenerIdPedidoDeBien(int idBien, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT id_pedido_donacion FROM bienes WHERE id = ?")) {
            stmt.setInt(1, idBien);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    @Override
    public List<OrdenEntregaDTO> obtenerEntregasPorBeneficiario(String username) {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            List<OrdenEntrega> ordenes = ordenEntregaDao.findByBeneficiario(username, conn);
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
            
            return ordenes.stream().map(o -> {
                String resumenBienes = "Sin bienes detallados";
                if (o.getBienes() != null && !o.getBienes().isEmpty()) {
                    resumenBienes = o.getBienes().stream().map(Object::toString).collect(Collectors.joining(", "));
                }
                String fechaStr = o.getFechaGeneracion() != null ? sdf.format(o.getFechaGeneracion()) : "-";
                return new OrdenEntregaDTO(o.getId(), fechaStr, o.obtenerEstadoString(), resumenBienes);
            }).collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            ConnectionManager.disconnect();
        }
    }

    @Override
    public List<OrdenEntregaDTO> obtenerEntregasPendientes() {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            return ordenEntregaDao.findAllPendientes(conn).stream().map(o -> {
                 String resumen = (o.getBienes() != null && !o.getBienes().isEmpty()) 
                         ? o.getBienes().stream().map(Object::toString).collect(Collectors.joining(", ")) 
                         : "Sin detalle";
                 return new OrdenEntregaDTO(o.getId(), o.getFechaGeneracion().toString(), o.obtenerEstadoString(), resumen);
            }).collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            ConnectionManager.disconnect();
        }
    }

    @Override
    public void completarEntrega(int idOrden, String usuarioVoluntario) throws ObjetoNuloException {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);
            
            OrdenEntrega orden = ordenEntregaDao.findById(idOrden, conn);
            if (orden == null) throw new ObjetoNuloException("Orden no encontrada.");
            
            Usuario voluntario = usuarioDao.find(usuarioVoluntario, conn);
            orden.setVoluntario(voluntario);
            orden.setEstado(OrdenEntrega.ESTADO_COMPLETADO);
            
            ordenEntregaDao.update(orden, conn);
            conn.commit();
        } catch (Exception e) {
            rollback(conn);
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn);
        }
    }

    // Metodos de usuario genericos

    @Override
    public void registrarUsuario(String username, String password, String email, String nombre, Integer codigoRol,
            String apellido, int dni, String direccion, String necesidad, int personasCargo, String prioridad)
            throws CampoVacioException, ObjetoNuloException, UsuarioInvalidoException {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);
            Rol rol = rolDao.find(codigoRol, conn);
            if (rol == null) throw new ObjetoNuloException("Rol no encontrado");
            Usuario usuario = new Usuario(username, password, nombre, email, rol, apellido, dni, direccion, necesidad, personasCargo, prioridad);
            usuarioDao.create(usuario, conn);
            conn.commit();
        } catch (SQLException e) {
            rollback(conn);
            if (e.getMessage().contains("dni_UNIQUE") || e.getMessage().contains("Duplicate")) {
                throw new UsuarioInvalidoException("Ya existe un usuario con el DNI " + dni);
            }
            throw new RuntimeException(e);
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
                dtos.add(new UsuarioDTO(u.getUsuario(), u.getContrasena(), u.getNombre(), u.getEmail(), u.getRol().getNombre(), u.isActivo(), u.obtenerEstado()));
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
                return new UsuarioDTO(u.getUsuario(), u.getContrasena(), u.getNombre(), u.getEmail(), u.getRol().getNombre(), u.isActivo(), u.obtenerEstado());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.disconnect();
        }
        return null;
    }

    @Override public void eliminarUsuario(String username) {}
    @Override public List<RolDTO> obtenerRoles() {
        Connection conn = null;
        try { conn = ConnectionManager.getConnection(); return rolDao.findAll(conn).stream().map(r -> new RolDTO(r.getCodigo(), r.getNombre(), r.isActivo())).collect(Collectors.toList()); } catch (SQLException e) { e.printStackTrace(); return new ArrayList<>(); } finally { ConnectionManager.disconnect(); }
    }
    @Override public List<RolDTO> obtenerRolesActivos() {
        Connection conn = null;
        try { conn = ConnectionManager.getConnection(); return rolDao.findAll(conn).stream().filter(Rol::isActivo).map(r -> new RolDTO(r.getCodigo(), r.getNombre(), r.isActivo())).collect(Collectors.toList()); } catch (SQLException e) { e.printStackTrace(); return new ArrayList<>(); } finally { ConnectionManager.disconnect(); }
    }
    @Override public void guardarRol(Integer codigo, String descripcion, boolean estado) throws CampoVacioException {}
    @Override public RolDTO obtenerRolPorCodigo(Integer codigo) { return null; }
    @Override public void activarRol(Integer codigo) {}
    @Override public void desactivarRol(Integer codigo) {}
    @Override public void activarUsuario(String username) { cambiarEstadoUsuario(username, true); }
    @Override public void desactivarUsuario(String username) { cambiarEstadoUsuario(username, false); }

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

    // Helpers de conexión y mapeo
    private void rollback(Connection conn) { try { if (conn != null) conn.rollback(); } catch (SQLException e) { e.printStackTrace(); } }
    private void closeConnection(Connection conn) { try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); } ConnectionManager.disconnect(); }
    
    private OrdenRetiroDTO mapearOrdenADTO(OrdenRetiro o) {
        String vol = o.obtenerVoluntarioPrincipal() != null ? o.obtenerVoluntarioPrincipal().getNombre() + " " + o.obtenerVoluntarioPrincipal().getApellido() : "Sin Voluntario";
        String don = o.obtenerDonante() != null ? o.obtenerDonante().getNombre() + " " + o.obtenerDonante().getApellido() : "Sin Donante";
        String veh = o.obtenerVehiculo() != null ? o.obtenerVehiculo().getDescripcion() : "Sin Vehículo";
        return new OrdenRetiroDTO(o.getId(), o.obtenerNombreEstado(), o.obtenerFechaCreacion(), new ArrayList<>(), don, veh, vol);
    }
}