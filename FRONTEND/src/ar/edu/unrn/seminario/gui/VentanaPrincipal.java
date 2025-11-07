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
import ar.edu.unrn.seminario.dto.VisitaDTO;

public class VentanaPrincipal extends JFrame {

	private JPanel contentPane;
	private String rolActual;
	private JMenu usuarioMenu;
	private JMenu mnDonaciones;
	private JMenu voluntarioMenu;
	private JMenu configuracionMenu;
	private JMenuItem salirMenuItem;
	private int donanteIdActual = -1; // hardcodear donanteId
	private JMenuItem crearOrdenMenuItem; 
	private JMenuItem listadoOrdenesMenuItem;
	private JMenuItem listadoPedidosMenuItem; 
	private JComboBox<VoluntarioDTO> voluntarioSelectorComboBox;

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
		// Donaciones
		mnDonaciones = new JMenu("Donaciones");
		menuBar.add(mnDonaciones);

		JMenuItem mntmRegistrarPedido = new JMenuItem("Registrar Pedido de Donacion");
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
		listadoOrdenesMenuItem = new JMenuItem("Listado ordenes de Retiro");
		listadoOrdenesMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ListadoOrdenesRetiro listadoOrdenes = new ListadoOrdenesRetiro(api);
				listadoOrdenes.setLocationRelativeTo(null);
				listadoOrdenes.setVisible(true);
			}
		});
		mnDonaciones.add(listadoOrdenesMenuItem);
		listadoPedidosMenuItem = new JMenuItem("Listado Pedidos de Donacion Pendientes");//los no asignados a una orden de retiro
		listadoPedidosMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ListadoPedidosDonacion listadoPedidos = new ListadoPedidosDonacion(api);
				listadoPedidos.setLocationRelativeTo(null);
				listadoPedidos.setVisible(true);
			}
		});
		mnDonaciones.add(listadoPedidosMenuItem);
		crearOrdenMenuItem = new JMenuItem("Crear Orden de Retiro");
		crearOrdenMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CrearOrdenRetiro dialog = new CrearOrdenRetiro(VentanaPrincipal.this, api);
				dialog.setVisible(true);
			}
		});
		mnDonaciones.add(crearOrdenMenuItem);

		voluntarioMenu = new JMenu("Voluntario");
		menuBar.add(voluntarioMenu);
		
		JMenuItem gestionarOrdenesMenuItem = new JMenuItem("Gestionar ordenes");
		gestionarOrdenesMenuItem.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) { 
		        VoluntarioDTO voluntarioSeleccionado = (VoluntarioDTO) voluntarioSelectorComboBox.getSelectedItem();
		        if (voluntarioSeleccionado == null) {
		             JOptionPane.showMessageDialog(VentanaPrincipal.this, "Debe seleccionar un voluntario.", "Error", JOptionPane.ERROR_MESSAGE);
		             return;
		        }
		        ListadoOrdenesAsignadasVoluntario listadoOrdenesVoluntario = new ListadoOrdenesAsignadasVoluntario(api, voluntarioSeleccionado);
		        listadoOrdenesVoluntario.setLocationRelativeTo(null);
		        listadoOrdenesVoluntario.setVisible(true);
		    }
		});
		voluntarioMenu.add(gestionarOrdenesMenuItem);
        
		/*JMenuItem registrarVisitaMenuItem = new JMenuItem("Registrar Visita");
        registrarVisitaMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RegistrarVisitaDialog registrarVisitaDialog = new RegistrarVisitaDialog(api, 0); // ID de orden hardcodeado
                registrarVisitaDialog.setLocationRelativeTo(null);
                registrarVisitaDialog.setVisible(true);
            }
        });
        voluntarioMenu.add(registrarVisitaMenuItem);*/
        
        JMenuItem listadoVisitasMenuItem = new JMenuItem("Historial de Visitas");
        listadoVisitasMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                VoluntarioDTO voluntarioSeleccionado = (VoluntarioDTO) voluntarioSelectorComboBox.getSelectedItem();
                if (voluntarioSeleccionado == null) {
                    JOptionPane.showMessageDialog(VentanaPrincipal.this, "Debe seleccionar un voluntario.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                List<VisitaDTO> visitas = api.obtenerVisitasPorVoluntario(voluntarioSeleccionado);
                if (visitas == null || visitas.isEmpty()) {
                    JOptionPane.showMessageDialog(VentanaPrincipal.this, "No hay visitas registradas para este voluntario.", "Informacion", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                ListadoVisitasDialog listadoVisitasDialog = new ListadoVisitasDialog(api, voluntarioSeleccionado);
                listadoVisitasDialog.setLocationRelativeTo(null);
                listadoVisitasDialog.setVisible(true);
            }
        });
        voluntarioMenu.add(listadoVisitasMenuItem);
		//  Configuracion
		configuracionMenu = new JMenu("Configuracion");
		menuBar.add(configuracionMenu);

		salirMenuItem = new JMenuItem("Salir");
		salirMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		configuracionMenu.add(salirMenuItem);
		JPanel rolPanel = new JPanel();
		rolPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel rolLabel = new JLabel("Rol:");
		rolPanel.add(rolLabel);

		JComboBox<String> rolSelectorComboBox = new JComboBox<>(new String[]{"ADMINISTRADOR", "DONANTE", "VOLUNTARIO"});
		rolPanel.add(rolSelectorComboBox);
		contentPane.add(rolPanel, BorderLayout.SOUTH);

		JLabel voluntarioLabel = new JLabel("Voluntario:");
        voluntarioLabel.setVisible(false); //  oculto
        rolPanel.add(voluntarioLabel);

        voluntarioSelectorComboBox = new JComboBox<>();
		voluntarioSelectorComboBox.setEnabled(false);
		voluntarioSelectorComboBox.setVisible(false); //  oculto
		rolPanel.add(voluntarioSelectorComboBox);

		// cargar voluntarios al iniciar
		List<VoluntarioDTO> voluntarios = api.obtenerVoluntarios(); //  en la API
		for (VoluntarioDTO voluntario : voluntarios) {
			voluntarioSelectorComboBox.addItem(voluntario);
		}

		// inicia del rol
		rolActual = (String) rolSelectorComboBox.getSelectedItem();
		actualizarUIporRol();

		// ActionListener para el ComboBox
		rolSelectorComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				rolActual = (String) rolSelectorComboBox.getSelectedItem();
				actualizarUIporRol();
				boolean esVoluntario = "VOLUNTARIO".equals(rolActual);
				voluntarioSelectorComboBox.setEnabled(esVoluntario);
				voluntarioSelectorComboBox.setVisible(esVoluntario);
				voluntarioLabel.setVisible(esVoluntario);

				// Depuracion
				System.out.println("Rol actual: " + rolActual);

				// actualizar lista de voluntarios dinamicamente
				if (esVoluntario) {
					voluntarioSelectorComboBox.removeAllItems();
					List<VoluntarioDTO> voluntarios = api.obtenerVoluntarios();
					if (voluntarios == null || voluntarios.isEmpty()) {
						System.out.println("No se encontraron voluntarios.");
					} else {
						for (VoluntarioDTO voluntario : voluntarios) {
							voluntarioSelectorComboBox.addItem(voluntario);
						}
					}
				}
			}
		});
	}

	public void actualizarUIporRol() {
	    // segun el rol
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
	    voluntarioSelectorComboBox.setVisible("VOLUNTARIO".equals(rolActual)); // actualizar visibilidad del selector
	}
}