package ar.edu.unrn.seminario.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import ar.edu.unrn.seminario.api.IApi;
import ar.edu.unrn.seminario.dto.RolDTO;

public class AltaUsuario extends JFrame {

    private JPanel contentPane;
    private JTextField usuarioTextField, contrasenaTextField, nombreTextField, 
                       apellidoTextField, dniTextField, emailTextField, direccionTextField,
                       necesidadTextField, personasCargoTextField;
    private JComboBox<String> rolComboBox, prioridadComboBox;
    private JLabel direccionLabel, lblNecesidad, lblPrioridad, lblPersonas;
    
    private List<RolDTO> roles;

    public AltaUsuario(IApi api) {
        this.roles = api.obtenerRoles();
        setTitle("Alta Usuario");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 450, 500); // Agrandamos para los nuevos campos

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(null);
        setContentPane(contentPane);
        crearCampo("Usuario:", 13, usuarioTextField = new JTextField());
        crearCampo("Contraseña:", 45, contrasenaTextField = new JTextField());
        crearCampo("Nombre:", 77, nombreTextField = new JTextField());
        crearCampo("Apellido:", 109, apellidoTextField = new JTextField());
        crearCampo("DNI:", 141, dniTextField = new JTextField());
        crearCampo("Email:", 173, emailTextField = new JTextField());
        JLabel rolLabel = new JLabel("Rol:");
        rolLabel.setBounds(43, 208, 56, 16);
        contentPane.add(rolLabel);

        rolComboBox = new JComboBox<>();
        rolComboBox.setBounds(148, 205, 160, 22);
        if (roles != null) {
            for (RolDTO r : roles) rolComboBox.addItem(r.getNombre());
        }
        contentPane.add(rolComboBox);
        direccionLabel = new JLabel("Dirección:");
        direccionLabel.setBounds(43, 240, 93, 16);
        contentPane.add(direccionLabel);
        direccionTextField = new JTextField();
        direccionTextField.setBounds(148, 237, 160, 22);
        contentPane.add(direccionTextField);

        lblNecesidad = new JLabel("Necesidad:");
        lblNecesidad.setBounds(43, 272, 93, 16);
        contentPane.add(lblNecesidad);
        necesidadTextField = new JTextField();
        necesidadTextField.setBounds(148, 269, 160, 22);
        contentPane.add(necesidadTextField);

        lblPrioridad = new JLabel("Prioridad:");
        lblPrioridad.setBounds(43, 304, 93, 16);
        contentPane.add(lblPrioridad);
        prioridadComboBox = new JComboBox<>(new String[]{"BAJA", "MEDIA", "ALTA"});
        prioridadComboBox.setBounds(148, 301, 160, 22);
        contentPane.add(prioridadComboBox);

        lblPersonas = new JLabel("Pers. Cargo:");
        lblPersonas.setBounds(43, 336, 93, 16);
        contentPane.add(lblPersonas);
        personasCargoTextField = new JTextField();
        personasCargoTextField.setBounds(148, 333, 160, 22);
        personasCargoTextField.setText("0");
        contentPane.add(personasCargoTextField);

        // Estado inicial de campos visuales
        actualizarVisibilidadCampos();

        // Listener para ocultar/mostrar (Lógica de Presentación permitida en Front)
        rolComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                actualizarVisibilidadCampos();
            }
            
        });

        // Botones
        JButton aceptarButton = new JButton("Aceptar");
        aceptarButton.setBounds(218, 400, 97, 25);
        contentPane.add(aceptarButton);

        JButton cancelarButton = new JButton("Cancelar");
        cancelarButton.setBounds(323, 400, 97, 25);
        contentPane.add(cancelarButton);

        // Aceptar
        aceptarButton.addActionListener(e -> {
            try {
                // Recolección de datos
                int idx = rolComboBox.getSelectedIndex();
                if (idx == -1) throw new Exception("Seleccione un rol");
                Integer codRol = roles.get(idx).getCodigo();

                String user = usuarioTextField.getText();
                String pass = contrasenaTextField.getText();
                String email = emailTextField.getText();
                String nom = nombreTextField.getText();
                String ape = apellidoTextField.getText();
                String dniStr = dniTextField.getText();
                String dir = direccionTextField.getText();
                String nec = necesidadTextField.getText();
                String prio = (String) prioridadComboBox.getSelectedItem();
                String persStr = personasCargoTextField.getText();

                int dni = 0;
                try { dni = Integer.parseInt(dniStr); } catch (NumberFormatException ex) {}
                
                int pers = 0;
                try { pers = Integer.parseInt(persStr); } catch (NumberFormatException ex) {}

                // Llamada a API (La API y el Modelo validan todo)
                api.registrarUsuario(user, pass, email, nom, codRol, ape, dni, dir, nec, pers, prio);

                JOptionPane.showMessageDialog(null, "Usuario registrado con éxito.");
                dispose();

            } catch (Exception ex) {
                // El Front solo muestra el error que viene del Back
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelarButton.addActionListener(e -> dispose());
    }

    private void crearCampo(String label, int y, JTextField field) {
        JLabel l = new JLabel(label);
        l.setBounds(43, y + 3, 100, 16);
        contentPane.add(l);
        field.setBounds(148, y, 160, 22);
        contentPane.add(field);
    }

    private void actualizarVisibilidadCampos() {
        String rol = (String) rolComboBox.getSelectedItem();
        boolean esBeneficiario = "BENEFICIARIO".equals(rol);
        boolean esDonante = "DONANTE".equals(rol);

        // Reglas de visualización
        direccionLabel.setVisible(esDonante || esBeneficiario);
        direccionTextField.setVisible(esDonante || esBeneficiario);

        lblNecesidad.setVisible(esBeneficiario);
        necesidadTextField.setVisible(esBeneficiario);
        lblPrioridad.setVisible(esBeneficiario);
        prioridadComboBox.setVisible(esBeneficiario);
        lblPersonas.setVisible(esBeneficiario);
        personasCargoTextField.setVisible(esBeneficiario);
    }
}