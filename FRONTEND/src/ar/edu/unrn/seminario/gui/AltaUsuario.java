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
// Importaciones de excepciones propias
import ar.edu.unrn.seminario.exception.CampoVacioException;
import ar.edu.unrn.seminario.exception.ObjetoNuloException;
import ar.edu.unrn.seminario.exception.UsuarioInvalidoException;

public class AltaUsuario extends JFrame {

    private JPanel contentPane;
    private JTextField usuarioTextField;
    private JTextField contrasenaTextField;
    private JTextField nombreTextField;
    private JTextField emailTextField;
    private JComboBox<String> rolComboBox;
    private JTextField contactoTextField;
    private JTextField ubicacionTextField;
    
    // nuevos campos añadidos que vienen de persona
    private JTextField apellidoTextField;
    private JTextField dniTextField;
    
    // campo de direccion (solo para Donante)
    private JLabel direccionLabel;
    private JTextField direccionTextField;

    private List<RolDTO> roles = new ArrayList<>();
    
    // Código de Rol DONANTE	
    private static final int CODIGO_ROL_DONANTE = 3;

    public AltaUsuario(IApi api) {
        // Carga de roles
        this.roles = api.obtenerRoles();

        setTitle("Alta Usuario");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 450, 380);

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(null);
        setContentPane(contentPane);

        JLabel usuarioLabel = new JLabel("Usuario:");
        usuarioLabel.setBounds(43, 16, 76, 16);
        contentPane.add(usuarioLabel);

        JLabel contrasenaLabel = new JLabel("Contraseña:");
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

        // Campo Apellido
        JLabel apellidoLabel = new JLabel("Apellido:");
        apellidoLabel.setBounds(43, 112, 76, 16);
        contentPane.add(apellidoLabel);

        apellidoTextField = new JTextField();
        apellidoTextField.setBounds(148, 109, 160, 22);
        contentPane.add(apellidoTextField);
        apellidoTextField.setColumns(10);

        // DNI
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

        if (this.roles != null) {
            for (RolDTO rol : this.roles) {
                rolComboBox.addItem(rol.getNombre());
            }
        }

        direccionLabel = new JLabel("Direccion:");
        direccionLabel.setBounds(43, 245, 93, 16);
        direccionLabel.setVisible(false);
        contentPane.add(direccionLabel);

        direccionTextField = new JTextField();
        direccionTextField.setBounds(148, 242, 160, 22);
        direccionTextField.setVisible(false);
        contentPane.add(direccionTextField);

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
                    int selectedIndex = rolComboBox.getSelectedIndex();
                    if (selectedIndex == -1) {
                        throw new CampoVacioException("Debe seleccionar un rol.");
                    }
                    RolDTO rol = roles.get(selectedIndex);
                    
                    String usuario = usuarioTextField.getText();
                    if (usuario == null || usuario.trim().isEmpty()) {
                        throw new CampoVacioException("El campo 'Usuario' no puede estar vacío.");
                    }
                    
                    String contrasena = contrasenaTextField.getText();
                    if (contrasena == null || contrasena.trim().isEmpty()) {
                        throw new CampoVacioException("El campo 'Contraseña' no puede estar vacío.");
                    }
                    
                    String nombre = nombreTextField.getText();
                    if (nombre == null || nombre.trim().isEmpty()) {
                        throw new CampoVacioException("El campo 'Nombre' no puede estar vacío.");
                    }
                    
                    String apellido = apellidoTextField.getText();
                    if (apellido == null || apellido.trim().isEmpty()) {
                        throw new CampoVacioException("El campo 'Apellido' no puede estar vacío.");
                    }
                    
                    String dniText = dniTextField.getText();
                    if (dniText == null || dniText.trim().isEmpty()) {
                        throw new CampoVacioException("El campo 'DNI' no puede estar vacío.");
                    }
                    
                    String email = emailTextField.getText();
                    if (email == null || email.trim().isEmpty()) {
                        throw new CampoVacioException("El campo 'Email' no puede estar vacío.");
                    }

                    String contacto = contactoTextField.getText();
                    if (contacto == null || contacto.trim().isEmpty()) {
                        throw new CampoVacioException("El campo 'Contacto' no puede estar vacío.");
                    }

                    String ubicacion = ubicacionTextField.getText();
                    if (ubicacion == null || ubicacion.trim().isEmpty()) {
                        throw new CampoVacioException("El campo 'Ubicación' no puede estar vacío.");
                    }

                    String direccion = null;
                    if (rol.getCodigo() == CODIGO_ROL_DONANTE) {
                        direccion = direccionTextField.getText();
                        if (direccion == null || direccion.trim().isEmpty()) {
                            throw new CampoVacioException("El campo 'Dirección' es obligatorio para un DONANTE.");
                        }
                    }

                    int dni = Integer.parseInt(dniText);
                    
                    api.registrarUsuario(
                        usuario,
                        contrasena,
                        email,
                        nombre,
                        rol.getCodigo(),
                        apellido,
                        dni,
                        direccion,
                        contacto, 
                        ubicacion 
                    );
                    
                    JOptionPane.showMessageDialog(null, "Usuario registrado con éxito!", "Info", JOptionPane.INFORMATION_MESSAGE);
                    setVisible(false);
                    dispose();
                    
                } catch (UsuarioInvalidoException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Error de Registro", JOptionPane.ERROR_MESSAGE);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "DNI debe ser un número válido (sin puntos ni letras).", "Error", JOptionPane.ERROR_MESSAGE);
                } catch (CampoVacioException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Error de Validación", JOptionPane.WARNING_MESSAGE);
                } catch (ObjetoNuloException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Error de API", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Error inesperado: " + ex.getMessage(), "Error Crítico", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace(); 
                }
            }
        });        
        aceptarButton.setBounds(218, 290, 97, 25);
        contentPane.add(aceptarButton);

        JButton cancelarButton = new JButton("Cancelar");
        
        // Accion del boton "cancelar"
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