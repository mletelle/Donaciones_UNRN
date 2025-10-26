package ar.edu.unrn.seminario.gui;

import java.awt.BorderLayout;
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
import ar.edu.unrn.seminario.dto.VoluntarioDTO;

public class VentanaPrincipal extends JFrame {

	private JPanel contentPane;
	private String rolActual;
	private JMenu usuarioMenu;
	private JMenu mnDonaciones;
	private JMenu voluntarioMenu;
	private JMenu configuracionMenu;
	private JMenuItem salirMenuItem;
	private int donanteIdActual = -1; // hardcodear donanteId
	private JMenuItem crearOrdenMenuItem; // Moved crearOrdenMenuItem to class level for broader accessibility
	private JMenuItem listadoOrdenesMenuItem; // Declare listadoOrdenesMenuItem at the class level
	private JMenuItem listadoPedidosMenuItem; // Declare listadoPedidosMenuItem at the class level
	private JComboBox<String> voluntarioSelectorComboBox;

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

		// Initialize listadoOrdenesMenuItem
		listadoOrdenesMenuItem = new JMenuItem("Listado Órdenes de Retiro");
		listadoOrdenesMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ListadoOrdenesRetiro listadoOrdenes = new ListadoOrdenesRetiro(api);
				listadoOrdenes.setLocationRelativeTo(null);
				listadoOrdenes.setVisible(true);
			}
		});
		mnDonaciones.add(listadoOrdenesMenuItem);

		// Initialize listadoPedidosMenuItem
		listadoPedidosMenuItem = new JMenuItem("Listado Pedidos de Donación");
		listadoPedidosMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ListadoPedidosDonacion listadoPedidos = new ListadoPedidosDonacion(api);
				listadoPedidos.setLocationRelativeTo(null);
				listadoPedidos.setVisible(true);
			}
		});
		mnDonaciones.add(listadoPedidosMenuItem);
		
		// Updated initialization to use the class-level variable
		crearOrdenMenuItem = new JMenuItem("Crear Orden de Retiro");
		crearOrdenMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CrearOrdenRetiro dialog = new CrearOrdenRetiro(VentanaPrincipal.this, api);
				dialog.setVisible(true);
			}
		});
		mnDonaciones.add(crearOrdenMenuItem);

		// Menú Voluntario
		voluntarioMenu = new JMenu("Voluntario");
		menuBar.add(voluntarioMenu);

		JMenuItem gestionarOrdenesMenuItem = new JMenuItem("Gestionar Órdenes");
        gestionarOrdenesMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ListadoOrdenesAsignadasVoluntario listadoOrdenesVoluntario = new ListadoOrdenesAsignadasVoluntario(api);
                listadoOrdenesVoluntario.setLocationRelativeTo(null);
                listadoOrdenesVoluntario.setVisible(true);
            }
        });
        voluntarioMenu.add(gestionarOrdenesMenuItem);

        JMenuItem registrarVisitaMenuItem = new JMenuItem("Registrar Visita");
        registrarVisitaMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RegistrarVisitaDialog registrarVisitaDialog = new RegistrarVisitaDialog(api, 0); // ID de orden hardcodeado
                registrarVisitaDialog.setLocationRelativeTo(null);
                registrarVisitaDialog.setVisible(true);
            }
        });
        voluntarioMenu.add(registrarVisitaMenuItem);

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

		JLabel voluntarioLabel = new JLabel("Voluntario:");
        voluntarioLabel.setVisible(false); // Inicialmente oculto
        rolPanel.add(voluntarioLabel);

        voluntarioSelectorComboBox = new JComboBox<>();
		voluntarioSelectorComboBox.setEnabled(false);
		voluntarioSelectorComboBox.setVisible(false); // Inicialmente oculto
		rolPanel.add(voluntarioSelectorComboBox);

		// Cargar voluntarios al iniciar
		List<VoluntarioDTO> voluntarios = api.obtenerVoluntarios(); // Método que debe implementarse en la API
		for (VoluntarioDTO voluntario : voluntarios) {
			voluntarioSelectorComboBox.addItem(voluntario.getNombre()); // Suponiendo que VoluntarioDTO tiene un método getNombre()
		}

		// Eliminado porque voluntarioActual no se utiliza
		/*
		voluntarioSelectorComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				voluntarioActual = (String) voluntarioSelectorComboBox.getSelectedItem();
			}
		});
		*/

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
				boolean esVoluntario = "VOLUNTARIO".equals(rolActual);
				voluntarioSelectorComboBox.setEnabled(esVoluntario);
				voluntarioSelectorComboBox.setVisible(esVoluntario);
				voluntarioLabel.setVisible(esVoluntario); // Actualizar visibilidad de la etiqueta
			}
		});
	}

	public void actualizarUIporRol() {
		// Actualizar segun el rol
		usuarioMenu.setVisible("ADMINISTRADOR".equals(rolActual));
		mnDonaciones.setVisible("ADMINISTRADOR".equals(rolActual) || "DONANTE".equals(rolActual));
		voluntarioMenu.setVisible("VOLUNTARIO".equals(rolActual));
		configuracionMenu.setVisible(true);
		configuracionMenu.setEnabled(true);
		salirMenuItem.setVisible(true);
		salirMenuItem.setEnabled(true);
		if ("ADMINISTRADOR".equals(rolActual)) {
			crearOrdenMenuItem.setVisible(true);
		} else {
			crearOrdenMenuItem.setVisible(false);
		}
		listadoOrdenesMenuItem.setVisible(!"DONANTE".equals(rolActual));
		listadoPedidosMenuItem.setVisible(!"DONANTE".equals(rolActual));
		voluntarioSelectorComboBox.setVisible("VOLUNTARIO".equals(rolActual)); // Actualizar visibilidad del selector
	}

}
