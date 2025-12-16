package ar.edu.unrn.seminario.api;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId; //que dependan de esto las fechas
import java.time.format.DateTimeFormatter;//por lo del vencimiento
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ar.edu.unrn.seminario.dto.*;
import ar.edu.unrn.seminario.exception.*;
import ar.edu.unrn.seminario.modelo.*;

public class MemoryApi implements IApi {
    private List<Rol> roles;
    private List<Usuario> usuarios;
    private List<PedidosDonacion> pedidos;
    private List<OrdenRetiro> ordenes;
    private List<OrdenEntrega> ordenesEntrega;
    private List<Vehiculo> vehiculosDisponibles;

    private static int secuenciaBien = 0;

    public MemoryApi() throws CampoVacioException, ObjetoNuloException {
        this.roles = new ArrayList<>();
        this.usuarios = new ArrayList<>();
        this.pedidos = new ArrayList<>();
        this.ordenes = new ArrayList<>();
        this.ordenesEntrega = new ArrayList<>();
        this.vehiculosDisponibles = new ArrayList<>();

        inicializarDatos();
    }

    private void inicializarDatos() {
        try {
            roles.add(new Rol(1, "ADMIN"));
            roles.add(new Rol(2, "VOLUNTARIO"));
            roles.add(new Rol(3, "DONANTE"));
            roles.add(new Rol(4, "BENEFICIARIO"));

            vehiculosDisponibles.add(new Vehiculo("AE 123 CD", "Disponible", "Auto", 500));
            vehiculosDisponibles.add(new Vehiculo("AD 456 EF", "Disponible", "Camioneta", 1500));
            vehiculosDisponibles.add(new Vehiculo("AA 789 GH", "Disponible", "Camion", 4000));

            registrarUsuario("admin", "1234", "admin@unrn.edu.ar", "Admin", 1, "Sistema", 11111111, null, null, 0, null);
            registrarUsuario("clopez", "pass", "clopez@unrn.edu.ar", "Carlos", 2, "Lopez", 22222222, null, null, 0, null);
            registrarUsuario("jperez", "pass", "jperez@unrn.edu.ar", "Juan", 3, "Perez", 55555555, "Calle Falsa 123", null, 0, null);
            registrarUsuario("inst_comedor", "1234", "comedor@mail.com", "Comedor", 4, "", 99999999, "Calle Solidaria 10", "Alimentos", 100, "ALTA");

            Usuario donantePrueba = usuarios.stream().filter(u -> u.getUsuario().equals("jperez")).findFirst().orElse(null);
            if (donantePrueba != null) {
                java.time.LocalDateTime ahora = java.time.LocalDateTime.now();
                PedidosDonacion pd = new PedidosDonacion(pedidos.size() + 1, ahora, 1, donantePrueba);
                try {
                    Bien b1 = new Bien(BienDTO.TIPO_NUEVO, 10, BienDTO.CATEGORIA_ALIMENTOS);
                    b1.setDescripcion("Paquete de arroz 1kg");
                    b1.setEstadoInventario(Bien.ESTADO_EN_STOCK);
                    b1.setId(++secuenciaBien);

                    Bien b2 = new Bien(BienDTO.TIPO_NUEVO, 5, BienDTO.CATEGORIA_ROPA);
                    b2.setDescripcion("Camisas talla M");
                    b2.setEstadoInventario(Bien.ESTADO_EN_STOCK);
                    b2.setId(++secuenciaBien);

                    pd.obtenerBienes().add(b1);
                    pd.obtenerBienes().add(b2);
                    pedidos.add(pd);
                } catch (CampoVacioException e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<BienDTO> obtenerInventario() {
        return pedidos.stream().flatMap(p -> p.obtenerBienes().stream())
                .filter(b -> Bien.ESTADO_EN_STOCK.equals(b.getEstadoInventario()))
                .map(this::convertirEntidadADTOVisual).collect(Collectors.toList());
    }

    @Override
    public void actualizarBienInventario(BienDTO bienDTO) throws ObjetoNuloException, CampoVacioException, ReglaNegocioException {
        if (bienDTO.getId() <= 0) throw new ObjetoNuloException("ID inválido");

        Bien bienEncontrado = pedidos.stream()
                .flatMap(p -> p.obtenerBienes().stream())
                .filter(b -> b.getId() == bienDTO.getId())
                .findFirst().orElse(null);
                
        if (bienEncontrado == null) throw new ObjetoNuloException("El bien no existe");
        if (bienDTO.getCantidad() < 0) throw new ReglaNegocioException("La cantidad no puede ser negativa");

        if (bienDTO.getFechaVencimiento() != null) {
            if (bienDTO.getCategoria() != BienDTO.CATEGORIA_ALIMENTOS) {
                throw new ReglaNegocioException("Solo alimentos pueden tener vencimiento");
            }
            if (bienDTO.getFechaVencimiento().isBefore(java.time.LocalDate.now())) {
                throw new ReglaNegocioException("Esta vencido, necesariamente tiene que ser posterior a hoy.");
            }
        }
        bienEncontrado.setCantidad(bienDTO.getCantidad());
        bienEncontrado.setDescripcion(bienDTO.getDescripcion());
        
  
        if (bienDTO.getFechaVencimiento() != null) {
            java.util.Date fechaConvertida = java.util.Date.from(
                bienDTO.getFechaVencimiento().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()
            );
            bienEncontrado.setFecVec(fechaConvertida); 
        } else {
            bienEncontrado.setFecVec(null);
        }
    }

    private BienDTO convertirEntidadADTOVisual(Bien bien) {
        String categoriaStr = mapCategoriaToString(bien.obtenerCategoria());
        String estadoStr = (bien.obtenerTipo() == BienDTO.TIPO_NUEVO) ? "Nuevo" : "Usado";
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
        dto.setCategoria(bien.obtenerCategoria());
        dto.setTipo(bien.obtenerTipo());
        
        dto.setCategoriaTexto(categoriaStr);
        dto.setEstadoTexto(estadoStr);
        dto.setFechaVencimiento(fechaLocalDate); 
        dto.setVencimientoTexto(vencimientoStr);
        
        return dto;
    }

    private String mapCategoriaToString(int idCategoria) {
        switch (idCategoria) {
            case BienDTO.CATEGORIA_ROPA: return "Ropa";
            case BienDTO.CATEGORIA_MUEBLES: return "Muebles";
            case BienDTO.CATEGORIA_ALIMENTOS: return "Alimentos";
            case BienDTO.CATEGORIA_ELECTRODOMESTICOS: return "Electrodomésticos";
            case BienDTO.CATEGORIA_HERRAMIENTAS: return "Herramientas";
            case BienDTO.CATEGORIA_JUGUETES: return "Juguetes";
            case BienDTO.CATEGORIA_LIBROS: return "Libros";
            case BienDTO.CATEGORIA_MEDICAMENTOS: return "Medicamentos";
            case BienDTO.CATEGORIA_HIGIENE: return "Higiene";
            default: return "Otros";
        }
    }
    
    @Override
    public void darDeBajaBien(int idBien, String motivo) throws ObjetoNuloException, ReglaNegocioException {
        Bien bienEncontrado = pedidos.stream().flatMap(p -> p.obtenerBienes().stream()).filter(b -> b.getId() == idBien).findFirst().orElse(null);
        if (bienEncontrado == null) throw new ObjetoNuloException("El bien no existe.");
        bienEncontrado.setEstadoInventario(Bien.ESTADO_BAJA);
        bienEncontrado.setDescripcion(bienEncontrado.getDescripcion() + " [BAJA: " + motivo + "]");
    }

    @Override
    public void registrarUsuario(String username, String password, String email, String nombre, Integer codigoRol,
            String apellido, int dni, String direccion, String necesidad, int personasCargo, String prioridad)
            throws CampoVacioException, ObjetoNuloException, UsuarioInvalidoException {
        if (usuarios.stream().anyMatch(u -> u.getUsuario().equalsIgnoreCase(username))) {
            throw new UsuarioInvalidoException("El nombre de usuario ya existe.");
        }
        if (usuarios.stream().anyMatch(u -> u.getDni() == dni)) {
            throw new UsuarioInvalidoException("Ya existe un usuario con el DNI " + dni);
        }
        Rol rol = roles.stream().filter(r -> r.getCodigo().equals(codigoRol)).findFirst().orElse(null);
        if (rol == null) throw new ObjetoNuloException("Rol no encontrado.");
        Usuario nuevoUsuario = new Usuario(username, password, nombre, email, rol, apellido, dni, direccion, 
                                           necesidad, personasCargo, prioridad);
        usuarios.add(nuevoUsuario);
    }

    @Override
    public List<UsuarioDTO> obtenerUsuarios() {
        return usuarios.stream()
                .map(u -> new UsuarioDTO(u.getUsuario(), u.getContrasena(), u.getNombre(), u.getEmail(),
                        u.getRol().getNombre(), u.isActivo(), u.obtenerEstado()))
                .collect(Collectors.toList());
    }

    @Override
    public UsuarioDTO obtenerUsuario(String username) {
        return usuarios.stream().filter(u -> u.getUsuario().equals(username))
                .findFirst()
                .map(u -> new UsuarioDTO(u.getUsuario(), u.getContrasena(), u.getNombre(), u.getEmail(),
                        u.getRol().getNombre(), u.isActivo(), u.obtenerEstado()))
                .orElse(null);
    }

    @Override
    public void eliminarUsuario(String username) {
        usuarios.removeIf(u -> u.getUsuario().equals(username));
    }

    @Override
    public void activarUsuario(String username) {
        usuarios.stream().filter(u -> u.getUsuario().equals(username)).findFirst().ifPresent(Usuario::activar);
    }

    @Override
    public void desactivarUsuario(String username) {
        usuarios.stream().filter(u -> u.getUsuario().equals(username)).findFirst().ifPresent(Usuario::desactivar);
    }

    @Override
    public List<UsuarioDTO> obtenerDonantes() {
        return usuarios.stream().filter(u -> u.getRol().getCodigo() == 3 && u.isActivo())
                .map(u -> new UsuarioDTO(u.getUsuario(), u.getNombre(), u.getApellido(), u.getDni(), u.obtenerDireccion(), 3))
                .collect(Collectors.toList());
    }

    @Override
    public List<UsuarioDTO> obtenerVoluntarios() {
        return usuarios.stream().filter(u -> u.getRol().getCodigo() == 2 && u.isActivo())
                .map(u -> new UsuarioDTO(u.getUsuario(), u.getNombre(), u.getApellido(), u.getDni(), u.obtenerDireccion(), 2))
                .collect(Collectors.toList());
    }

    @Override
    public List<UsuarioDTO> obtenerBeneficiarios() {
        return usuarios.stream().filter(u -> u.getRol().getCodigo() == 4 && u.isActivo())
                .map(u -> new UsuarioDTO(u.getUsuario(), u.getNombre(), u.getApellido(), u.getDni(), u.obtenerDireccion(), 4))
                .collect(Collectors.toList());
    }

    private Usuario buscarUsuarioPorDni(int dni) {
        return usuarios.stream().filter(u -> u.getDni() == dni).findFirst().orElse(null);
    }

    @Override
    public List<RolDTO> obtenerRoles() {
        return roles.stream().map(r -> new RolDTO(r.getCodigo(), r.getNombre(), r.isActivo())).collect(Collectors.toList());
    }

    @Override
    public List<RolDTO> obtenerRolesActivos() {
        return roles.stream().filter(Rol::isActivo).map(r -> new RolDTO(r.getCodigo(), r.getNombre(), r.isActivo())).collect(Collectors.toList());
    }

    @Override
    public void guardarRol(Integer codigo, String descripcion, boolean estado) throws CampoVacioException {
        Rol rolExistente = roles.stream().filter(r -> r.getCodigo().equals(codigo)).findFirst().orElse(null);
        if (rolExistente != null) {
            rolExistente.setNombre(descripcion);
            rolExistente.setActivo(estado);
        } else {
            Rol nuevoRol = new Rol(codigo, descripcion);
            nuevoRol.setActivo(estado);
            roles.add(nuevoRol);
        }
    }

    @Override
    public RolDTO obtenerRolPorCodigo(Integer codigo) {
        return roles.stream().filter(r -> r.getCodigo().equals(codigo)).findFirst()
                .map(r -> new RolDTO(r.getCodigo(), r.getNombre(), r.isActivo())).orElse(null);
    }

    @Override
    public void activarRol(Integer codigo) {
        roles.stream().filter(r -> r.getCodigo().equals(codigo)).findFirst().ifPresent(Rol::activar);
    }

    @Override
    public void desactivarRol(Integer codigo) {
        roles.stream().filter(r -> r.getCodigo().equals(codigo)).findFirst().ifPresent(Rol::desactivar);
    }

    @Override
    public void registrarPedidoDonacion(PedidoDonacionDTO pedidoDTO) throws CampoVacioException, ObjetoNuloException {
        Usuario donante = buscarUsuarioPorDni(pedidoDTO.getDonanteId());
        if (donante == null) throw new ObjetoNuloException("Donante no encontrado.");

        List<Bien> bienes = new ArrayList<>();
        for (BienDTO dto : pedidoDTO.getBienes()) {
            Bien bien = new Bien(dto.getTipo(), dto.getCantidad(), dto.getCategoria());
            bien.setId(++secuenciaBien);
            if (dto.getDescripcion() != null) bien.setDescripcion(dto.getDescripcion());
            if (dto.getFechaVencimiento() != null) {
                java.util.Date fecha = java.util.Date.from(dto.getFechaVencimiento().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
                bien.setFecVec(fecha);
            }
            bienes.add(bien);
        }

        LocalDateTime fecha = LocalDate.parse(pedidoDTO.getFecha(), DateTimeFormatter.ofPattern("dd/MM/yyyy")).atStartOfDay();
        
        PedidosDonacion pedido = new PedidosDonacion(
            fecha, 
            (ArrayList<Bien>) bienes, 
            PedidosDonacion.convertirVehiculoAInt(pedidoDTO.getTipoVehiculo()), 
            donante
        );

        pedidos.add(pedido);
    }

    @Override
    public List<PedidoDonacionDTO> obtenerTodosPedidos() {
        return pedidos.stream().sorted(Comparator.comparing(PedidosDonacion::obtenerFecha).reversed())
                .map(this::convertirPedidoADTO).collect(Collectors.toList());
    }

    @Override
    public List<PedidoDonacionDTO> obtenerPedidosPendientes() {
        return pedidos.stream().filter(p -> p.obtenerOrden() == null).map(this::convertirPedidoADTO).collect(Collectors.toList());
    }

    @Override
    public List<PedidoDonacionDTO> obtenerPedidosDeOrden(int idOrden) {
        OrdenRetiro o = buscarOrdenPorId(idOrden);
        if (o == null) return new ArrayList<>();
        return o.obtenerPedidos().stream().map(this::convertirPedidoADTO).collect(Collectors.toList());
    }

    @Override
    public String obtenerNombreDonantePorId(int idPedido) {
        PedidosDonacion p = buscarPedidoPorId(idPedido);
        return p != null ? p.getDonante().getNombre() + " " + p.getDonante().getApellido() : "";
    }

    private PedidoDonacionDTO convertirPedidoADTO(PedidosDonacion p) {
        String nombre = p.getDonante().getNombre() + " " + p.getDonante().getApellido();
        String fecha = p.obtenerFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        return new PedidoDonacionDTO(p.getId(), fecha, p.describirTipoVehiculo(), p.getDonante().getDni(), nombre, p.obtenerEstado());
    }

    private PedidosDonacion buscarPedidoPorId(int id) {
        return pedidos.stream().filter(p -> p.getId() == id).findFirst().orElse(null);
    }

    @Override
    public void crearOrdenRetiro(List<Integer> idsPedidos, int idVoluntario, String tipoVehiculo)
            throws ReglaNegocioException, ObjetoNuloException {

        Usuario voluntario = buscarUsuarioPorDni(idVoluntario);
        if (voluntario == null || voluntario.getRol().getCodigo() != 2) throw new ObjetoNuloException("Voluntario inválido.");

        Vehiculo vehiculo = vehiculosDisponibles.stream()
                .filter(v -> v.getTipoVeh().equalsIgnoreCase(tipoVehiculo) && "Disponible".equals(v.getEstado()))
                .findFirst().orElse(null);

        if (vehiculo == null) throw new ReglaNegocioException("No hay vehículos disponibles de tipo " + tipoVehiculo);

        List<PedidosDonacion> pedidosParaOrden = new ArrayList<>();
        for (Integer id : idsPedidos) {
            PedidosDonacion p = buscarPedidoPorId(id);
            if (p == null) throw new ObjetoNuloException("Pedido ID " + id + " no encontrado.");
            if (p.obtenerOrden() != null) throw new ReglaNegocioException("El pedido ID " + id + " ya tiene una orden asignada.");
            pedidosParaOrden.add(p);
        }

        OrdenRetiro orden = new OrdenRetiro(pedidosParaOrden, null);
        orden.asignarVoluntario(voluntario);
        orden.asignarVehiculo(vehiculo);
        ordenes.add(orden);
    }

    @Override
    public List<OrdenRetiroDTO> obtenerOrdenesDeRetiro(String estado) {
        return ordenes.stream().filter(o -> o.obtenerNombreEstado().equalsIgnoreCase(estado))
                .map(this::mapearOrdenADTO).collect(Collectors.toList());
    }

    @Override
    public List<OrdenRetiroDTO> obtenerTodasOrdenesRetiro() {
        return ordenes.stream().map(this::mapearOrdenADTO).collect(Collectors.toList());
    }

    @Override
    public List<OrdenRetiroDTO> obtenerOrdenesAsignadas(String voluntarioUser) {
        return ordenes.stream()
                .filter(o -> o.obtenerVoluntarioPrincipal() != null && o.obtenerVoluntarioPrincipal().getUsuario().equals(voluntarioUser))
                .map(this::mapearOrdenADTO).collect(Collectors.toList());
    }

    private OrdenRetiro buscarOrdenPorId(int id) {
        return ordenes.stream().filter(o -> o.getId() == id).findFirst().orElse(null);
    }

    private OrdenRetiroDTO mapearOrdenADTO(OrdenRetiro o) {
        String vol = o.obtenerVoluntarioPrincipal() != null ? o.obtenerVoluntarioPrincipal().getNombre() + " " + o.obtenerVoluntarioPrincipal().getApellido() : "Sin Voluntario";
        String don = o.obtenerDonante() != null ? o.obtenerDonante().getNombre() + " " + o.obtenerDonante().getApellido() : "Sin Donante";
        String veh = o.obtenerVehiculo() != null ? o.obtenerVehiculo().getDescripcion() : "Sin Vehículo";
        return new OrdenRetiroDTO(o.getId(), o.obtenerNombreEstado(), o.obtenerFechaCreacion(), new ArrayList<>(), don, veh, vol);
    }

    @Override
    public void crearOrdenEntrega(String userBeneficiario, Map<Integer, Integer> bienesYCantidades, String userVoluntario)
            throws ObjetoNuloException, ReglaNegocioException, CampoVacioException {
        
        Usuario beneficiario = usuarios.stream().filter(u -> u.getUsuario().equals(userBeneficiario)).findFirst().orElse(null);
        if (beneficiario == null) throw new ObjetoNuloException("Beneficiario no encontrado.");

        Usuario voluntario = null;
        if (userVoluntario != null) {
            voluntario = usuarios.stream().filter(u -> u.getUsuario().equals(userVoluntario)).findFirst().orElse(null);
        }

        List<Bien> bienesFinalesParaOrden = new ArrayList<>();

        for (Map.Entry<Integer, Integer> entry : bienesYCantidades.entrySet()) {
            int idBienOriginal = entry.getKey();
            int cantidadSolicitada = entry.getValue();

            Bien bienOriginal = pedidos.stream().flatMap(p -> p.obtenerBienes().stream()).filter(b -> b.getId() == idBienOriginal).findFirst().orElse(null);
            
            if (bienOriginal == null) throw new ObjetoNuloException("Bien no encontrado.");
            if (!Bien.ESTADO_EN_STOCK.equals(bienOriginal.getEstadoInventario())) throw new ReglaNegocioException("Bien no disponible.");
            if (cantidadSolicitada > bienOriginal.getCantidad()) throw new ReglaNegocioException("Stock insuficiente.");

            if (cantidadSolicitada == bienOriginal.getCantidad()) {
                bienOriginal.setEstadoInventario(Bien.ESTADO_ENTREGADO);
                bienesFinalesParaOrden.add(bienOriginal);
            } else {
                bienOriginal.setCantidad(bienOriginal.getCantidad() - cantidadSolicitada);
                Bien bienNuevo = new Bien(bienOriginal.obtenerTipo(), cantidadSolicitada, bienOriginal.obtenerCategoria());
                bienNuevo.setId(++secuenciaBien);
                bienNuevo.setDescripcion(bienOriginal.getDescripcion());
                bienNuevo.setEstadoInventario(Bien.ESTADO_ENTREGADO);
                bienesFinalesParaOrden.add(bienNuevo);
            }
        }

        OrdenEntrega nuevaOrden = new OrdenEntrega(beneficiario, bienesFinalesParaOrden);
        if (voluntario != null) nuevaOrden.setVoluntario(voluntario);
        nuevaOrden.setId(ordenesEntrega.size() + 1);
        
        ordenesEntrega.add(nuevaOrden);
    }

    @Override
    public List<OrdenEntregaDTO> obtenerEntregasPorBeneficiario(String username) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        return ordenesEntrega.stream()
                .filter(o -> o.getBeneficiario().getUsuario().equals(username))
                .map(o -> {
                    String fechaStr = o.getFechaGeneracion() != null ? sdf.format(o.getFechaGeneracion()) : "-";
                    String resumen = (o.getBienes() != null && !o.getBienes().isEmpty()) 
                            ? o.getBienes().stream().map(Object::toString).collect(Collectors.joining(", ")) 
                            : "Sin detalle";
                    return new OrdenEntregaDTO(o.getId(), fechaStr, o.obtenerEstadoString(), resumen);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<OrdenEntregaDTO> obtenerEntregasPendientes() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        return ordenesEntrega.stream()
                .filter(o -> o.getEstado() == OrdenEntrega.ESTADO_PENDIENTE)
                .map(o -> {
                    String resumen = "Sin detalle";
                    if (o.getBienes() != null && !o.getBienes().isEmpty()) {
                        resumen = o.getBienes().stream().map(Object::toString).collect(Collectors.joining(", "));
                    }
                    return new OrdenEntregaDTO(o.getId(), sdf.format(o.getFechaGeneracion()), o.obtenerEstadoString(), resumen);
                })
                .collect(Collectors.toList());
    }

    @Override
    public void completarEntrega(int idOrden, String usuarioVoluntario) throws ObjetoNuloException {
        OrdenEntrega orden = ordenesEntrega.stream().filter(o -> o.getId() == idOrden).findFirst().orElse(null);
        if (orden == null) throw new ObjetoNuloException("Orden no encontrada.");
        
        Usuario vol = usuarios.stream().filter(u -> u.getUsuario().equals(usuarioVoluntario)).findFirst().orElse(null);
        orden.setVoluntario(vol);
        orden.setEstado(OrdenEntrega.ESTADO_COMPLETADO);
    }

    @Override
    public void registrarVisita(int idOrdenRetiro, int idPedido, LocalDateTime fechaHora, String resultado, String observacion)
            throws ObjetoNuloException, CampoVacioException, ReglaNegocioException {
        OrdenRetiro orden = buscarOrdenPorId(idOrdenRetiro);
        if (orden == null) throw new ObjetoNuloException("Orden no encontrada.");
        PedidosDonacion pedido = orden.obtenerPedidoPorId(idPedido);
        if (pedido == null) throw new ObjetoNuloException("El pedido no pertenece a esta orden.");

        ResultadoVisita resEnum = ResultadoVisita.fromString(resultado);
        Visita visita = new Visita(fechaHora, resEnum, observacion);
        visita.setPedidoRelacionado(pedido);
        orden.agregarVisita(visita);

        if (resEnum == ResultadoVisita.RECOLECCION_EXITOSA) {
            pedido.marcarCompletado();
            if (pedido.obtenerBienes() != null) {
                pedido.obtenerBienes().forEach(b -> b.setEstadoInventario(Bien.ESTADO_EN_STOCK));
            }
        } else if (resEnum == ResultadoVisita.CANCELADO) {
            pedido.marcarCompletado();
        } else {
            pedido.marcarEnEjecucion();
        }
        orden.actualizarEstadoAutomatico();
    }

    @Override
    public List<VisitaDTO> obtenerVisitasPorVoluntario(UsuarioDTO voluntarioDTO) {
        List<VisitaDTO> visitasDTO = new ArrayList<>();
        ordenes.stream()
                .filter(o -> o.obtenerVoluntarioPrincipal() != null && o.obtenerVoluntarioPrincipal().getDni() == voluntarioDTO.getDni())
                .forEach(o -> {
                    o.obtenerVisitas().forEach(v -> {
                        String donante = "Desconocido";
                        if (v.getPedidoRelacionado() != null && v.getPedidoRelacionado().getDonante() != null) {
                            Usuario d = v.getPedidoRelacionado().getDonante();
                            donante = d.getNombre() + " " + d.getApellido();
                        }
                        visitasDTO.add(new VisitaDTO(v.obtenerFechaFormateada(), v.obtenerObservacion(), v.obtenerResultado().toString(), donante));
                    });
                });
        return visitasDTO;
    }
}