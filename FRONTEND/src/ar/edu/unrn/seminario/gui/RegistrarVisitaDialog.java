package ar.edu.unrn.seminario.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import ar.edu.unrn.seminario.api.IApi;
import ar.edu.unrn.seminario.dto.VisitaDTO;
import ar.edu.unrn.seminario.exception.ReglaNegocioException;

public class RegistrarVisitaDialog extends JDialog {

    private JTextField txtFecha;
    private JTextField txtHora;
    private JComboBox<String> cmbResultado;
    private JTextArea txtObservaciones;
    private JTextField txtIdPedido; // convertido a campo de clase
    private IApi api;
    private int idOrden;
    private int idPedido;
    private GestionarOrdenVoluntario ventanaPadre; // referencia a la ventana padre

    public RegistrarVisitaDialog(IApi api, int idOrden) {
        this.api = api;
        this.idOrden = idOrden;

        setTitle("Registrar Visita");
        setSize(450, 350); 
        setModal(true);
        setLocationRelativeTo(null);

        JPanel panelPrincipal = new JPanel(new BorderLayout(5, 5)); // panel principal con borde

        JPanel panelCampos = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4); // espaciado entre componentes
        gbc.anchor = GridBagConstraints.EAST; // alinea etiquetas a la derecha

        // Fecha
        gbc.gridx = 0;
        gbc.gridy = 0;
        panelCampos.add(new JLabel("Fecha (YYYY-MM-DD):"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST; //  campos a la izquierda
        gbc.fill = GridBagConstraints.HORIZONTAL; //  horizontalmente
        gbc.weightx = 1.0; // dinamico
        txtFecha = new JTextField(10);
        txtFecha.setText(java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))); // fecha actual
        panelCampos.add(txtFecha, gbc);

        // Hora
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panelCampos.add(new JLabel("Hora (HH:MM):"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtHora = new JTextField(10);
        txtHora.setText(java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))); // hora actual
        panelCampos.add(txtHora, gbc);

        // Fila 2: Resultado
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panelCampos.add(new JLabel("Resultado:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        cmbResultado = new JComboBox<>(new String[]{"Recoleccion Exitosa", "Donante Ausente", "Recoleccion Parcial", "Cancelado"});
        panelCampos.add(cmbResultado, gbc);

        // pedido id
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panelCampos.add(new JLabel("ID Pedido:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtIdPedido = new JTextField(10); //
        txtIdPedido.setEditable(false); //  no editable
        panelCampos.add(txtIdPedido, gbc);

        // Observaciones
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.NORTHEAST; // 
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panelCampos.add(new JLabel("Observaciones:"), gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2; 
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0; 
        gbc.weighty = 1.0; 
        txtObservaciones = new JTextArea(5, 20);
        panelCampos.add(new JScrollPane(txtObservaciones), gbc);

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT)); 
        JButton btnGuardar = new JButton("Guardar");
        
        // Accion del boton "Guardar"
        btnGuardar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                guardarVisita();
            }
        });
        
        panelBotones.add(btnGuardar);

        // Al principal
        panelPrincipal.add(panelCampos, BorderLayout.CENTER);
        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);

        //  vacio para margen
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        add(panelPrincipal);
    }

    // Metodos
    public RegistrarVisitaDialog(IApi api, int idOrden, int idPedido) {
        this(api, idOrden); // Llama al constructor que construye la GUI
        this.idPedido = idPedido;
        this.txtIdPedido.setText(String.valueOf(this.idPedido)); 
    }
    
    // recibe la ventana padre para notificarla cuando se guarde
    public RegistrarVisitaDialog(IApi api, int idOrden, int idPedido, GestionarOrdenVoluntario ventanaPadre) {
        this(api, idOrden, idPedido);
        this.ventanaPadre = ventanaPadre;
    }

    // metodo para guardar una visita
    private void guardarVisita() {
        try {
            String fecha = txtFecha.getText();
            String hora = txtHora.getText();
            
            // verifica simple de formato
            if (!fecha.matches("\\d{4}-\\d{2}-\\d{2}")) {
                JOptionPane.showMessageDialog(this, "Formato de fecha incorrecto. Use YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!hora.matches("\\d{2}:\\d{2}")) {
                JOptionPane.showMessageDialog(this, "Formato de hora incorrecto. Use HH:MM.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // revisa que la fecha y hora sean validas antes de continuar
            if (fecha == null || fecha.isEmpty() || !fecha.matches("\\d{4}-\\d{2}-\\d{2}")) {
                JOptionPane.showMessageDialog(this, "La fecha es invalida o esta vacia. Use el formato YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (hora == null || hora.isEmpty() || !hora.matches("\\d{2}:\\d{2}")) {
                JOptionPane.showMessageDialog(this, "La hora es invalida o esta vacia. Use el formato HH:MM.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // obtiene el resultado y las observaciones antes de crear el DTO
            String resultado = (String) cmbResultado.getSelectedItem();
            String observaciones = txtObservaciones.getText();

            // Validar observaciones
            if (observaciones == null || observaciones.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "El campo de observaciones no puede estar vacio.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                LocalDateTime fechaHora = LocalDateTime.parse(fecha + "T" + hora);
                String nombreDonante = api.obtenerNombreDonantePorId(idPedido);
                VisitaDTO visita = new VisitaDTO(fechaHora, resultado, observaciones, nombreDonante);
                api.registrarVisita(idOrden, idPedido, visita);

                JOptionPane.showMessageDialog(this, "Visita registrada con exito.", "exito", JOptionPane.INFORMATION_MESSAGE);
                
                // notificar a la ventana padre para que recargue los datos
                if (ventanaPadre != null) {
                    ventanaPadre.recargarDatos();
                }
                
                dispose();
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "La fecha u hora no tienen un formato valido.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (ReglaNegocioException ex) {
                // captura especifica de violaciones de reglas de negocio
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Operacion no permitida", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al registrar la visita: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
}