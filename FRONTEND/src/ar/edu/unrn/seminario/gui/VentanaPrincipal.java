package ar.edu.unrn.seminario.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.EventQueue;
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
import ar.edu.unrn.seminario.api.MemoryApi;
import ar.edu.unrn.seminario.dto.DonanteDTO;

public class VentanaPrincipal extends JFrame {

	private JPanel contentPane;
	private String rolActual;
	private JMenu usuarioMenu;
	private JMenu mnDonaciones;
	private JMenu voluntarioMenu;
	private JMenu configuracionMenu;
	private JMenuItem salirMenuItem;
	private int donanteIdActual = -1; // hardcodear donanteId

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					IApi api = new MemoryApi();
					VentanaPrincipal frame = new VentanaPrincipal(api);
					frame.setLocationRelativeTo(null);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	} 

	public VentanaPrincipal(IApi api) {
		setTitle("Ventana Principal");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		setLayout(new BorderLayout()); //
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout());
		setContentPane(contentPane);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		// Menú Usuarios
		usuarioMenu = new JMenu("Usuarios");
		menuBar.add(usuarioMenu);

		JMenuItem altaUsuarioMenuItem = new JMenuItem("Alta/Modificación");
		altaUsuarioMenuItem.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				AltaUsuario alta = new AltaUsuario(api);
				alta.setLocationRelativeTo(null);
				alta.setVisible(true);
			}
			
		});
		usuarioMenu.add(altaUsuarioMenuItem);

		JMenuItem listadoUsuarioMenuItem = new JMenuItem("Listado");
		listadoUsuarioMenuItem.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				ListadoUsuario listado= new ListadoUsuario(api);
				listado.setLocationRelativeTo(null);
				listado.setVisible(true);
			}
			
		});
		usuarioMenu.add(listadoUsuarioMenuItem);



		// Menú Donaciones
		mnDonaciones = new JMenu("Donaciones");
		menuBar.add(mnDonaciones);

		JMenuItem mntmRegistrarPedido = new JMenuItem("Registrar Pedido de Donación");
		mntmRegistrarPedido.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if ("DONANTE".equals(rolActual)) {
					List<DonanteDTO> donantes = api.obtenerDonantes();
					if (!donantes.isEmpty()) {
						donanteIdActual = donantes.get(0).getId(); // hardcodear al primer donante
					} else {
						JOptionPane.showMessageDialog(VentanaPrincipal.this, "No hay donantes registrados para simular.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
				} else {
					donanteIdActual = -1; // hardcodear sin donante
				}

				RegistrarPedidoDonacion dialog = new RegistrarPedidoDonacion(api, donanteIdActual);
				dialog.setLocationRelativeTo(null);
				dialog.setVisible(true);
			}
		});
		mnDonaciones.add(mntmRegistrarPedido);

		JMenuItem listadoOrdenesMenuItem = new JMenuItem("Listado Órdenes de Retiro");
		listadoOrdenesMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ListadoOrdenesRetiro listadoOrdenes = new ListadoOrdenesRetiro(api);
				listadoOrdenes.setLocationRelativeTo(null);
				listadoOrdenes.setVisible(true);
			}
		});
		mnDonaciones.add(listadoOrdenesMenuItem);

		JMenuItem listadoPedidosMenuItem = new JMenuItem("Listado Pedidos de Donación");
		listadoPedidosMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ListadoPedidosDonacion listadoPedidos = new ListadoPedidosDonacion(api);
				listadoPedidos.setLocationRelativeTo(null);
				listadoPedidos.setVisible(true);
			}
		});
		mnDonaciones.add(listadoPedidosMenuItem);
		
		// Menú Voluntario
		voluntarioMenu = new JMenu("Voluntario");
		menuBar.add(voluntarioMenu);

		// Menú Configuración
		configuracionMenu = new JMenu("Configuración");
		menuBar.add(configuracionMenu);

		salirMenuItem = new JMenuItem("Salir");
		salirMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		configuracionMenu.add(salirMenuItem);

		// Panel inferior para el selector de rol un poco hardcodeado
		JPanel rolPanel = new JPanel();
		rolPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel rolLabel = new JLabel("Rol:");
		rolPanel.add(rolLabel);

		JComboBox<String> rolSelectorComboBox = new JComboBox<>(new String[]{"ADMINISTRADOR", "DONANTE", "VOLUNTARIO"});
		rolPanel.add(rolSelectorComboBox);
		contentPane.add(rolPanel, BorderLayout.SOUTH);

		// Inicializar rol actual hardcodeado
		rolActual = "ADMINISTRADOR";
		rolSelectorComboBox.setSelectedItem(rolActual);
		actualizarUIporRol();

		// ActionListener para el ComboBox también hardcodeado
		rolSelectorComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				rolActual = (String) rolSelectorComboBox.getSelectedItem();
				actualizarUIporRol();
			}
		});
	}

	private void actualizarUIporRol() {
		// Actualizar segun el rol
		usuarioMenu.setVisible("ADMINISTRADOR".equals(rolActual));
		mnDonaciones.setVisible("ADMINISTRADOR".equals(rolActual) || "DONANTE".equals(rolActual));
		voluntarioMenu.setVisible("VOLUNTARIO".equals(rolActual));
		configuracionMenu.setVisible(true);
		configuracionMenu.setEnabled(true);
		salirMenuItem.setVisible(true);
		salirMenuItem.setEnabled(true);
	}

}
