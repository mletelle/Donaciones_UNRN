package ar.edu.unrn.seminario.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.border.EmptyBorder;

import ar.edu.unrn.seminario.api.IApi;
import ar.edu.unrn.seminario.dto.UsuarioDTO;
import ar.edu.unrn.seminario.dto.VisitaDTO;

public class VentanaPrincipal extends JFrame {

    private JPanel contentPane;
    private String rolActual;
    private IApi api;
    
    // Menús
    private JMenu usuarioMenu;
    private JMenu mnDonaciones;
    private JMenu voluntarioMenu;
    private JMenu beneficiarioMenu;
    private JMenu configuracionMenu;
    
    private JMenuItem salirMenuItem;
    private int donanteIdActual = -1;
    private JMenuItem crearOrdenMenuItem;
    private JMenuItem listadoOrdenesMenuItem;
    private JMenuItem listadoPedidosMenuItem;
    
    // Selectores
    private JComboBox<UsuarioDTO> voluntarioSelectorComboBox;
    private JLabel voluntarioLabel;
    
    private JComboBox<UsuarioDTO> beneficiarioSelectorComboBox;
    private JLabel beneficiarioLabel;

    public VentanaPrincipal(IApi api) {
        this.api = api;
        setTitle("Ventana Principal - Sistema Donaciones");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 700, 450);
        setLayout(new BorderLayout());
        
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout());
        setContentPane(contentPane);

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        // Usuarios
        usuarioMenu = new JMenu("Usuarios");
        menuBar.add(usuarioMenu);

        JMenuItem altaUsuarioMenuItem = new JMenuItem("Alta/Modificacion");
        altaUsuarioMenuItem.addActionListener(e -> {
            AltaUsuario alta = new AltaUsuario(api);
            alta.setLocationRelativeTo(null);
            alta.setVisible(true);
        });
        usuarioMenu.add(altaUsuarioMenuItem);

        JMenuItem listadoUsuarioMenuItem = new JMenuItem("Listado");
        listadoUsuarioMenuItem.addActionListener(e -> {
            ListadoUsuario listado = new ListadoUsuario(api);
            listado.setLocationRelativeTo(null);
            listado.setVisible(true);
        });
        usuarioMenu.add(listadoUsuarioMenuItem);

        //  Donaciones
        mnDonaciones = new JMenu("Donaciones");
        menuBar.add(mnDonaciones);

        JMenuItem mntmRegistrarPedido = new JMenuItem("Generar Pedido de Donación");
        mntmRegistrarPedido.addActionListener(e -> {
            if ("DONANTE".equals(rolActual)) {
                List<UsuarioDTO> donantes = api.obtenerDonantes();
                if (!donantes.isEmpty()) {
                    donanteIdActual = donantes.get(0).getId();
                } else {
                    JOptionPane.showMessageDialog(VentanaPrincipal.this, "No hay donantes registrados.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                donanteIdActual = -1;
            }
            RegistrarPedidoDonacion dialog = new RegistrarPedidoDonacion(api, donanteIdActual);
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
        });

        JMenuItem listadoInventarioItem = new JMenuItem("Gestión de Inventario");
        listadoInventarioItem.addActionListener(e -> {
            ListadoInventario listado = new ListadoInventario(api, rolActual);
            listado.setLocationRelativeTo(null);
            listado.setVisible(true);
        });
        mnDonaciones.add(listadoInventarioItem);
        mnDonaciones.add(mntmRegistrarPedido);

        listadoOrdenesMenuItem = new JMenuItem("Listado de Órdenes de Retiro");
        listadoOrdenesMenuItem.addActionListener(e -> {
            ListadoOrdenesRetiro listadoOrdenes = new ListadoOrdenesRetiro(api);
            listadoOrdenes.setLocationRelativeTo(null);
            listadoOrdenes.setVisible(true);
        });
        mnDonaciones.add(listadoOrdenesMenuItem);

        listadoPedidosMenuItem = new JMenuItem("Listado de Pedidos de Donación");
        listadoPedidosMenuItem.addActionListener(e -> {
            ListadoPedidosDonacion listadoPedidos = new ListadoPedidosDonacion(api);
            listadoPedidos.setLocationRelativeTo(null);
            listadoPedidos.setVisible(true);
        });
        mnDonaciones.add(listadoPedidosMenuItem);

        crearOrdenMenuItem = new JMenuItem("Generar Orden de Retiro");
        crearOrdenMenuItem.addActionListener(e -> {
            CrearOrdenRetiro dialog = new CrearOrdenRetiro(VentanaPrincipal.this, api);
            dialog.setVisible(true);
        });
        mnDonaciones.add(crearOrdenMenuItem);
        
        JMenuItem generarEntregaItem = new JMenuItem("Generar Orden de Entrega");
        generarEntregaItem.addActionListener(e -> {
            CrearOrdenEntrega dialog = new CrearOrdenEntrega(VentanaPrincipal.this, api);
            dialog.setVisible(true);
        });
        mnDonaciones.add(generarEntregaItem);

        // Voluntarios
        voluntarioMenu = new JMenu("Voluntario");
        menuBar.add(voluntarioMenu);

        //  retiros 
        JMenuItem gestionarRetirosItem = new JMenuItem("Gestionar Órdenes de Retiro");
        gestionarRetirosItem.addActionListener(e -> {
            UsuarioDTO voluntarioSeleccionado = (UsuarioDTO) voluntarioSelectorComboBox.getSelectedItem();
            if (voluntarioSeleccionado == null) {
                JOptionPane.showMessageDialog(VentanaPrincipal.this, "Debe seleccionar un voluntario.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            ListadoOrdenesAsignadasVoluntario listado = new ListadoOrdenesAsignadasVoluntario(api, voluntarioSeleccionado);
            listado.setLocationRelativeTo(null);
            listado.setVisible(true);
        });
        voluntarioMenu.add(gestionarRetirosItem);

        // entregas
        JMenuItem gestionarEntregasItem = new JMenuItem("Gestionar Órdenes de Entrega");
        gestionarEntregasItem.addActionListener(e -> {
            UsuarioDTO voluntarioSeleccionado = (UsuarioDTO) voluntarioSelectorComboBox.getSelectedItem();
            if (voluntarioSeleccionado == null) {
                JOptionPane.showMessageDialog(VentanaPrincipal.this, "Debe seleccionar un voluntario.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            GestionarEntregas gui = new GestionarEntregas(api, voluntarioSeleccionado);
            gui.setVisible(true);
        });
        voluntarioMenu.add(gestionarEntregasItem);

        // historial
        JMenuItem listadoVisitasMenuItem = new JMenuItem("Historial de Visitas");
        listadoVisitasMenuItem.addActionListener(e -> {
            UsuarioDTO voluntarioSeleccionado = (UsuarioDTO) voluntarioSelectorComboBox.getSelectedItem();
            if (voluntarioSeleccionado == null) {
                JOptionPane.showMessageDialog(VentanaPrincipal.this, "Debe seleccionar un voluntario.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            List<VisitaDTO> visitas = api.obtenerVisitasPorVoluntario(voluntarioSeleccionado);
            if (visitas == null || visitas.isEmpty()) {
                 JOptionPane.showMessageDialog(VentanaPrincipal.this, "No hay visitas registradas.", "Info", JOptionPane.INFORMATION_MESSAGE);
                 return;
            }
            ListadoVisitasDialog dialog = new ListadoVisitasDialog(api, voluntarioSeleccionado);
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
        });
        voluntarioMenu.add(listadoVisitasMenuItem);

        // beneficiario
        beneficiarioMenu = new JMenu("Mis Solicitudes");
        menuBar.add(beneficiarioMenu);
        
        JMenuItem misEntregasItem = new JMenuItem("Consultar Estado de Pedidos");
        misEntregasItem.addActionListener(e -> {
            UsuarioDTO beneficiarioSeleccionado = (UsuarioDTO) beneficiarioSelectorComboBox.getSelectedItem();
            if (beneficiarioSeleccionado == null) {
                JOptionPane.showMessageDialog(VentanaPrincipal.this, "Debe seleccionar un beneficiario.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }
            ListadoEntregasBeneficiario listado = new ListadoEntregasBeneficiario(api, beneficiarioSeleccionado.getUsuario());
            listado.setVisible(true);
        });
        beneficiarioMenu.add(misEntregasItem);

        // Config
        configuracionMenu = new JMenu("Configuracion");
        menuBar.add(configuracionMenu);

        salirMenuItem = new JMenuItem("Salir");
        salirMenuItem.addActionListener(e -> System.exit(0));
        configuracionMenu.add(salirMenuItem);

    
        JPanel rolPanel = new JPanel();
        rolPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        rolPanel.add(new JLabel("Rol:"));

        JComboBox<String> rolSelectorComboBox = new JComboBox<>(
                new String[] { "ADMINISTRADOR", "DONANTE", "VOLUNTARIO", "BENEFICIARIO" });
        rolPanel.add(rolSelectorComboBox);
        contentPane.add(rolPanel, BorderLayout.SOUTH);

        voluntarioLabel = new JLabel("Voluntario:");
        voluntarioLabel.setVisible(false);
        rolPanel.add(voluntarioLabel);
        
        voluntarioSelectorComboBox = new JComboBox<>();
        voluntarioSelectorComboBox.setVisible(false);
        rolPanel.add(voluntarioSelectorComboBox);
        
        beneficiarioLabel = new JLabel("Beneficiario:");
        beneficiarioLabel.setVisible(false);
        rolPanel.add(beneficiarioLabel);
        
        beneficiarioSelectorComboBox = new JComboBox<>();
        beneficiarioSelectorComboBox.setVisible(false);
        rolPanel.add(beneficiarioSelectorComboBox);

        cargarListas();

        rolActual = (String) rolSelectorComboBox.getSelectedItem();
        actualizarUIporRol();

        rolSelectorComboBox.addActionListener(e -> {
            rolActual = (String) rolSelectorComboBox.getSelectedItem();
            if ("VOLUNTARIO".equals(rolActual) || "BENEFICIARIO".equals(rolActual)) {
                cargarListas();
            }
            actualizarUIporRol();
        });
    }
    
    private void cargarListas() {
        voluntarioSelectorComboBox.removeAllItems();
        List<UsuarioDTO> voluntarios = api.obtenerVoluntarios();
        if(voluntarios != null) {
            for (UsuarioDTO v : voluntarios) voluntarioSelectorComboBox.addItem(v);
        }
        
        beneficiarioSelectorComboBox.removeAllItems();
        List<UsuarioDTO> beneficiarios = api.obtenerBeneficiarios();
        if(beneficiarios != null) {
            for (UsuarioDTO b : beneficiarios) beneficiarioSelectorComboBox.addItem(b);
        }
    }

    public void actualizarUIporRol() {
        boolean isAdmin = "ADMINISTRADOR".equals(rolActual);
        boolean esDonante = "DONANTE".equals(rolActual);
        boolean esVoluntario = "VOLUNTARIO".equals(rolActual);
        boolean esBeneficiario = "BENEFICIARIO".equals(rolActual);

        usuarioMenu.setVisible(isAdmin);
        mnDonaciones.setVisible(isAdmin || esDonante);
        voluntarioMenu.setVisible(esVoluntario);
        beneficiarioMenu.setVisible(esBeneficiario);
        

        //  Gestión de Inventario (solo admin)
        if (mnDonaciones.getItemCount() > 0) mnDonaciones.getItem(0).setVisible(isAdmin);
        //  Registrar Pedido de Donación (admin o donante)
        if (mnDonaciones.getItemCount() > 1) mnDonaciones.getItem(1).setVisible(isAdmin || esDonante);
        //  Listado de Órdenes de Retiro (solo admin)
        if (mnDonaciones.getItemCount() > 2) mnDonaciones.getItem(2).setVisible(isAdmin);
        //  Listado de Pedidos de Donación (solo admin)
        if (mnDonaciones.getItemCount() > 3) mnDonaciones.getItem(3).setVisible(isAdmin);
        // Generar/Crear Orden de Retiro (solo admin)
        if (mnDonaciones.getItemCount() > 4) mnDonaciones.getItem(4).setVisible(isAdmin);
        // Generar Orden de Entrega (solo admin)
        if (mnDonaciones.getItemCount() > 5) mnDonaciones.getItem(5).setVisible(isAdmin);

        voluntarioLabel.setVisible(esVoluntario);
        voluntarioSelectorComboBox.setVisible(esVoluntario);
        
        beneficiarioLabel.setVisible(esBeneficiario);
        beneficiarioSelectorComboBox.setVisible(esBeneficiario);

        configuracionMenu.setVisible(true);
    }
}