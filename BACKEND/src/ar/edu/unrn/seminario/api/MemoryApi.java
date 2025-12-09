package ar.edu.unrn.seminario.api;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
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

public class MemoryApi implements IApi {
    private List<Rol> roles;
    private List<Usuario> usuarios;
    private List<PedidosDonacion> pedidos;
    private List<OrdenRetiro> ordenes;
    private List<Vehiculo> vehiculosDisponibles;

 
    private static int secuenciaBien = 0;

    public MemoryApi() throws CampoVacioException, ObjetoNuloException {
        this.roles = new ArrayList<>();
        this.usuarios = new ArrayList<>();
        this.pedidos = new ArrayList<>();
        this.ordenes = new ArrayList<>();
        this.vehiculosDisponibles = new ArrayList<>();

        inicializarDatos();
    }

    private void inicializarDatos() throws CampoVacioException, ObjetoNuloException {
        // Roles
        roles.add(new Rol(1, "ADMIN"));
        roles.add(new Rol(2, "VOLUNTARIO"));
        roles.add(new Rol(3, "DONANTE"));

        // Vehículos
        vehiculosDisponibles.add(new Vehiculo("AE 123 CD", "Disponible", "Auto", 500));
        vehiculosDisponibles.add(new Vehiculo("AD 456 EF", "Disponible", "Camioneta", 1500));
        vehiculosDisponibles.add(new Vehiculo("AA 789 GH", "Disponible", "Camion", 4000));

        // Usuarios Base
        try {
            registrarUsuario("admin", "1234", "admin@unrn.edu.ar", "Admin", 1, "Sistema", 11111111, null);
            registrarUsuario("clopez", "pass", "clopez@unrn.edu.ar", "Carlos", 2, "Lopez", 22222222, null);
            registrarUsuario("jperez", "pass", "jperez@unrn.edu.ar", "Juan", 3, "Perez", 55555555, "Calle Falsa 123");
        } catch (UsuarioInvalidoException e) {
            e.printStackTrace();
        }
    }
    @Override
    public List<BienDTO> obtenerInventario() {
        // Buscamos en todos los pedidos, aquellos bienes que estén EN_STOCK
        return pedidos.stream()
                .flatMap(p -> p.obtenerBienes().stream()) // Aplanamos la estructura (Lista de Listas -> Stream único)
                .filter(b -> Bien.ESTADO_EN_STOCK.equals(b.getEstadoInventario()))
                .map(this::convertirEntidadADTOVisual)
                .collect(Collectors.toList());
    }

    @Override
    public void actualizarBienInventario(BienDTO bienDTO) throws ObjetoNuloException, CampoVacioException, ReglaNegocioException {
        // Validar ID
        if (bienDTO.getId() <= 0) throw new ObjetoNuloException("ID inválido.");

        // Buscar el bien en memoria 
        Bien bienEncontrado = pedidos.stream()
                .flatMap(p -> p.obtenerBienes().stream())
                .filter(b -> b.getId() == bienDTO.getId())
                .findFirst()
                .orElse(null);

        if (bienEncontrado == null) {
            throw new ObjetoNuloException("El bien no existe en el sistema.");
        }

        // Validaciones
        if (bienDTO.getCantidad() < 0) {
            throw new ReglaNegocioException("La cantidad no puede ser negativa.");
        }

        // Actualizar datos
        bienEncontrado.setCantidad(bienDTO.getCantidad());
        bienEncontrado.setDescripcion(bienDTO.getDescripcion());
        // El estado se mantiene igual (EN_STOCK)
    }

    private BienDTO convertirEntidadADTOVisual(Bien bien) {
        String categoriaStr = mapCategoriaToString(bien.obtenerCategoria());
        String estadoStr = (bien.obtenerTipo() == BienDTO.TIPO_NUEVO) ? "Nuevo" : "Usado";
        
        String vencimientoStr = "-";
        if (bien.getFecVec() != null) {
            LocalDate localDate = new java.sql.Date(bien.getFecVec().getTime()).toLocalDate();
            vencimientoStr = localDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }

        BienDTO dto = new BienDTO(
            categoriaStr,
            bien.getDescripcion(),
            bien.obtenerCantidad(),
            estadoStr,
            vencimientoStr
        );
        
        // Asignar ID simulado
        dto.setId(bien.getId());
        dto.setCategoria(bien.obtenerCategoria());
        dto.setTipo(bien.obtenerTipo());
        
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

        // Lógica de Estados
        if (resEnum == ResultadoVisita.RECOLECCION_EXITOSA) {
            pedido.marcarCompletado();
            // Actualizar inventario en memoria
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
    public List<VisitaDTO> obtenerVisitasPorVoluntario(VoluntarioDTO voluntarioDTO) {
        List<VisitaDTO> visitasDTO = new ArrayList<>();
        // Recorrer todas las órdenes donde este voluntario sea el responsable
        ordenes.stream()
            .filter(o -> o.obtenerVoluntarioPrincipal() != null && 
                         o.obtenerVoluntarioPrincipal().getDni() == voluntarioDTO.getId())
            .forEach(o -> {
                o.obtenerVisitas().forEach(v -> {
                    String donante = "Desconocido";
                    if(v.getPedidoRelacionado() != null && v.getPedidoRelacionado().getDonante() != null) {
                        Usuario d = v.getPedidoRelacionado().getDonante();
                        donante = d.getNombre() + " " + d.getApellido();
                    }
                    visitasDTO.add(new VisitaDTO(
                        v.obtenerFechaFormateada(),
                        v.obtenerObservacion(),
                        v.obtenerResultado().toString(),
                        donante
                    ));
                });
            });
        return visitasDTO;
    }

    @Override
    public void registrarUsuario(String username, String password, String email, String nombre, Integer codigoRol,
            String apellido, int dni, String direccion) throws CampoVacioException, ObjetoNuloException, UsuarioInvalidoException {
        
        if (usuarios.stream().anyMatch(u -> u.getUsuario().equalsIgnoreCase(username))) {
            throw new UsuarioInvalidoException("El nombre de usuario ya existe.");
        }
        if (usuarios.stream().anyMatch(u -> u.getDni() == dni)) {
            throw new UsuarioInvalidoException("Ya existe un usuario con el DNI " + dni);
        }

        Rol rol = roles.stream().filter(r -> r.getCodigo().equals(codigoRol)).findFirst().orElse(null);
        if (rol == null) throw new ObjetoNuloException("Rol no encontrado.");

        Usuario nuevoUsuario = new Usuario(username, password, nombre, email, rol, apellido, dni, direccion);
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
        return usuarios.stream()
            .filter(u -> u.getUsuario().equals(username))
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
    public List<RolDTO> obtenerRoles() {
        return roles.stream()
                .map(r -> new RolDTO(r.getCodigo(), r.getNombre(), r.isActivo()))
                .collect(Collectors.toList());
    }

    @Override
    public List<RolDTO> obtenerRolesActivos() {
        return roles.stream()
                .filter(Rol::isActivo)
                .map(r -> new RolDTO(r.getCodigo(), r.getNombre(), r.isActivo()))
                .collect(Collectors.toList());
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
        return roles.stream()
                .filter(r -> r.getCodigo().equals(codigo))
                .findFirst()
                .map(r -> new RolDTO(r.getCodigo(), r.getNombre(), r.isActivo()))
                .orElse(null);
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
            
            // --- ASIGNACIÓN DE ID SIMULADO ---
            bien.setId(++secuenciaBien); 
            
            if (dto.getDescripcion() != null) bien.setDescripcion(dto.getDescripcion());
            if (dto.getFechaVencimiento() != null) {
                java.util.Date fecha = java.util.Date.from(dto.getFechaVencimiento().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
                bien.setFecVec(fecha);
            }
            bienes.add(bien);
        }

        LocalDateTime fecha = LocalDate.parse(pedidoDTO.getFecha(), DateTimeFormatter.ofPattern("dd/MM/yyyy")).atStartOfDay();
        PedidosDonacion pedido = new PedidosDonacion(fecha, bienes, pedidoDTO.getTipoVehiculo(), donante);
        
        pedidos.add(pedido);
    }

    @Override
    public List<DonanteDTO> obtenerDonantes() {
        return usuarios.stream()
                .filter(u -> u.getRol().getCodigo() == 3 && u.isActivo())
                .map(u -> new DonanteDTO(u.getDni(), u.getNombre() + " " + u.getApellido(), u.obtenerDireccion()))
                .collect(Collectors.toList());
    }

    @Override
    public List<PedidoDonacionDTO> obtenerTodosPedidos() {
        return pedidos.stream()
                .sorted(Comparator.comparing(PedidosDonacion::obtenerFecha).reversed())
                .map(this::convertirPedidoADTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PedidoDonacionDTO> obtenerPedidosPendientes() {
        return pedidos.stream()
                .filter(p -> p.obtenerOrden() == null)
                .map(this::convertirPedidoADTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PedidoDonacionDTO> obtenerPedidosDeOrden(int idOrden) {
        OrdenRetiro o = buscarOrdenPorId(idOrden);
        if (o == null) return new ArrayList<>();
        
        return o.obtenerPedidos().stream()
                .map(this::convertirPedidoADTO)
                .collect(Collectors.toList());
    }

    @Override
    public String obtenerNombreDonantePorId(int idPedido) {
        PedidosDonacion p = buscarPedidoPorId(idPedido);
        return p != null ? p.getDonante().getNombre() + " " + p.getDonante().getApellido() : "";
    }

    private PedidoDonacionDTO convertirPedidoADTO(PedidosDonacion p) {
        String nombre = p.getDonante().getNombre() + " " + p.getDonante().getApellido();
        String fecha = p.obtenerFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        return new PedidoDonacionDTO(
            p.getId(),
            fecha,
            p.describirTipoVehiculo(),
            p.getDonante().getDni(),
            nombre,
            p.obtenerEstado()
        );
    }

    @Override
    public void crearOrdenRetiro(List<Integer> idsPedidos, int idVoluntario, String tipoVehiculo)
            throws ReglaNegocioException, ObjetoNuloException {
        
        Usuario voluntario = buscarUsuarioPorDni(idVoluntario);
        if (voluntario == null || voluntario.getRol().getCodigo() != 2) {
            throw new ObjetoNuloException("El usuario seleccionado no es un voluntario válido.");
        }

        Vehiculo vehiculo = vehiculosDisponibles.stream()
                .filter(v -> v.getTipoVeh().equalsIgnoreCase(tipoVehiculo) && "Disponible".equals(v.getEstado()))
                .findFirst()
                .orElse(null);
        
        if (vehiculo == null) throw new ReglaNegocioException("No hay vehículos disponibles de tipo " + tipoVehiculo);

        List<PedidosDonacion> pedidosParaOrden = new ArrayList<>();
        for (Integer id : idsPedidos) {
            PedidosDonacion p = buscarPedidoPorId(id);
            if (p == null) throw new ObjetoNuloException("Pedido ID " + id + " no encontrado.");
            if (p.obtenerOrden() != null) throw new ReglaNegocioException("El pedido ID " + id + " ya tiene una orden asignada.");
            pedidosParaOrden.add(p);
        }

        OrdenRetiro orden = new OrdenRetiro(pedidosParaOrden, null); // Destino null por simplificación en memoria
        orden.asignarVoluntario(voluntario);
        orden.asignarVehiculo(vehiculo);
        
        ordenes.add(orden);
    }

    @Override
    public List<OrdenRetiroDTO> obtenerOrdenesDeRetiro(String estado) {
        return ordenes.stream()
                .filter(o -> o.obtenerNombreEstado().equalsIgnoreCase(estado))
                .map(this::mapearOrdenADTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrdenRetiroDTO> obtenerTodasOrdenesRetiro() {
        return ordenes.stream()
                .map(this::mapearOrdenADTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrdenRetiroDTO> obtenerOrdenesAsignadas(String voluntarioUser) {
        return ordenes.stream()
                .filter(o -> o.obtenerVoluntarioPrincipal() != null && 
                             o.obtenerVoluntarioPrincipal().getUsuario().equals(voluntarioUser))
                .map(this::mapearOrdenADTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<VoluntarioDTO> obtenerVoluntarios() {
        return usuarios.stream()
                .filter(u -> u.getRol().getCodigo() == 2 && u.isActivo())
                .map(u -> new VoluntarioDTO(u.getDni(), u.getNombre(), u.getApellido(), u.getUsuario()))
                .collect(Collectors.toList());
    }

    private Usuario buscarUsuarioPorDni(int dni) {
        return usuarios.stream().filter(u -> u.getDni() == dni).findFirst().orElse(null);
    }

    private PedidosDonacion buscarPedidoPorId(int id) {
        return pedidos.stream().filter(p -> p.getId() == id).findFirst().orElse(null);
    }

    private OrdenRetiro buscarOrdenPorId(int id) {
        return ordenes.stream().filter(o -> o.getId() == id).findFirst().orElse(null);
    }

    private OrdenRetiroDTO mapearOrdenADTO(OrdenRetiro o) {
        String vol = o.obtenerVoluntarioPrincipal() != null ? o.obtenerVoluntarioPrincipal().getNombre() + " " + o.obtenerVoluntarioPrincipal().getApellido() : "Sin Voluntario";
        String don = o.obtenerDonante() != null ? o.obtenerDonante().getNombre() + " " + o.obtenerDonante().getApellido() : "Sin Donante";
        String veh = o.obtenerVehiculo() != null ? o.obtenerVehiculo().getDescripcion() : "Sin Vehículo";
        
        return new OrdenRetiroDTO(
            o.getId(),
            o.obtenerNombreEstado(),
            o.obtenerFechaCreacion(),
            new ArrayList<>(), // Visitas vacías por defecto en el listado general
            don, veh, vol
        );
    }
}