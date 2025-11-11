package ar.edu.unrn.seminario.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import ar.edu.unrn.seminario.api.IApi;
import ar.edu.unrn.seminario.dto.RolDTO;
import ar.edu.unrn.seminario.exception.CampoVacioException;
import ar.edu.unrn.seminario.exception.ObjetoNuloException;

public class AltaUsuario extends JFrame {


	private JPanel contentPane;
	private JTextField usuarioTextField;
	private JTextField contrasenaTextField;
	private JTextField nombreTextField;
	private JTextField emailTextField;
	private JComboBox<String> rolComboBox;
	
	// nuevos campos añadidos que vienen de persona
	private JTextField apellidoTextField;
	private JTextField dniTextField;
	
	// campo de direccion (solo para Donante)
	private JLabel direccionLabel;
	private JTextField direccionTextField;

	private List<RolDTO> roles = new ArrayList<>();

	public AltaUsuario(IApi api) {

		this.roles = api.obtenerRoles();

		setTitle("Alta Usuario");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 380); //mas chico menos campos

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(null);
		setContentPane(contentPane);

		JLabel usuarioLabel = new JLabel("Usuario:");
		usuarioLabel.setBounds(43, 16, 76, 16);
		contentPane.add(usuarioLabel);

		JLabel contrasenaLabel = new JLabel("Contrase\u00F1a:");
		contrasenaLabel.setBounds(43, 48, 93, 16);
		contentPane.add(contrasenaLabel);

		usuarioTextField = new JTextField();
		usuarioTextField.setBounds(148, 13, 160, 22);
		contentPane.add(usuarioTextField);
		usuarioTextField.setColumns(10);

		contrasenaTextField = new JTextField();
		contrasenaTextField.setBounds(148, 45, 160, 22);
		contentPane.add(contrasenaTextField);
		contrasenaTextField.setColumns(10);

		JLabel nombreLabel = new JLabel("Nombre:");
		nombreLabel.setBounds(43, 80, 76, 16);
		contentPane.add(nombreLabel);

		nombreTextField = new JTextField();
		nombreTextField.setBounds(148, 77, 160, 22);
		contentPane.add(nombreTextField);
		nombreTextField.setColumns(10);

		// Campo Apellido -persona
		JLabel apellidoLabel = new JLabel("Apellido:");
		apellidoLabel.setBounds(43, 112, 76, 16);
		contentPane.add(apellidoLabel);

		apellidoTextField = new JTextField();
		apellidoTextField.setBounds(148, 109, 160, 22);
		contentPane.add(apellidoTextField);
		apellidoTextField.setColumns(10);

		// DNI-persona
		JLabel dniLabel = new JLabel("DNI:");
		dniLabel.setBounds(43, 144, 76, 16);
		contentPane.add(dniLabel);

		dniTextField = new JTextField();
		dniTextField.setBounds(148, 141, 160, 22);
		contentPane.add(dniTextField);
		dniTextField.setColumns(10);

		JLabel emailLabel = new JLabel("Email:");
		emailLabel.setBounds(43, 176, 56, 16);
		contentPane.add(emailLabel);

		emailTextField = new JTextField();
		emailTextField.setBounds(148, 173, 160, 22);
		contentPane.add(emailTextField);
		emailTextField.setColumns(10);

		JLabel rolLabel = new JLabel("Rol:");
		rolLabel.setBounds(43, 208, 56, 16);
		contentPane.add(rolLabel);

		rolComboBox = new JComboBox<>();
		rolComboBox.setBounds(148, 205, 160, 22);
		contentPane.add(rolComboBox);

		for (RolDTO rol : this.roles) {
			rolComboBox.addItem(rol.getNombre());
		}

		// campo Direccion (solo para Donante
		direccionLabel = new JLabel("Direccion:");
		direccionLabel.setBounds(43, 245, 93, 16);
		direccionLabel.setVisible(false);
		contentPane.add(direccionLabel);

		direccionTextField = new JTextField();
		direccionTextField.setBounds(148, 242, 160, 22);
		direccionTextField.setVisible(false);
		contentPane.add(direccionTextField);

		// Listener para mostrar/ocultar campo  para DONANTE
		rolComboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					String rolSeleccionado = (String) rolComboBox.getSelectedItem();
					boolean mostrarDireccion = rolSeleccionado != null && rolSeleccionado.equals("DONANTE");
					
					direccionLabel.setVisible(mostrarDireccion);
					direccionTextField.setVisible(mostrarDireccion);
				}
			}
		});

		JButton aceptarButton = new JButton("Aceptar");
		aceptarButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					RolDTO rol = roles.get(rolComboBox.getSelectedIndex());
					
					// validar DNI
					int dni = Integer.parseInt(dniTextField.getText());
					
					String direccion = null;
					if (rol.getCodigo() == 3) { // DONANTE
						direccion = direccionTextField.getText();
					}

					api.registrarUsuario(
						usuarioTextField.getText(), 
						contrasenaTextField.getText(),
						emailTextField.getText(), 
						nombreTextField.getText(), 
						rol.getCodigo(),
						apellidoTextField.getText(),
						dni,
						direccion
					);
					JOptionPane.showMessageDialog(null, "Usuario registrado con exito!", "Info", JOptionPane.INFORMATION_MESSAGE);
					setVisible(false);
					dispose();
				} catch (NumberFormatException ex) {
					JOptionPane.showMessageDialog(null, "DNI debe ser un numero valido", "Error", JOptionPane.ERROR_MESSAGE);
				} catch (CampoVacioException | ObjetoNuloException ex) {
					JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(null, "Error inesperado: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					ex.printStackTrace();
				}
			}
		});
		aceptarButton.setBounds(218, 290, 97, 25);
		contentPane.add(aceptarButton);

		JButton cancelarButton = new JButton("Cancelar");
		cancelarButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		});
		cancelarButton.setBounds(323, 290, 97, 25);
		contentPane.add(cancelarButton);

	}
}

