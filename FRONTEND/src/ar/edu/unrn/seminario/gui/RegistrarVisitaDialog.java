package ar.edu.unrn.seminario.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import ar.edu.unrn.seminario.api.IApi;
import ar.edu.unrn.seminario.dto.VisitaDTO;

public class RegistrarVisitaDialog extends JDialog {

    private JTextField txtFecha;
    private JTextField txtHora;
    private JComboBox<String> cmbResultado;
    private JTextArea txtObservaciones;
    private JTextField txtIdPedido; // Convertido a campo de clase
    private IApi api;
    private int idOrden;
    private int idPedido;

    /**
     * Constructor principal que construye la GUI.
     */
    public RegistrarVisitaDialog(IApi api, int idOrden) {
        this.api = api;
        this.idOrden = idOrden;

        setTitle("Registrar Visita");
        setSize(450, 350); // Ajustado el tamaño para el nuevo layout
        setModal(true);
        setLocationRelativeTo(null);

        JPanel panelPrincipal = new JPanel(new BorderLayout(5, 5)); // Panel principal con borde

        // Panel de campos con GridBagLayout para un formulario limpio
        JPanel panelCampos = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4); // Espaciado entre componentes
        gbc.anchor = GridBagConstraints.EAST; // Alinea etiquetas a la derecha

        // Fila 0: Fecha
        gbc.gridx = 0;
        gbc.gridy = 0;
        panelCampos.add(new JLabel("Fecha (YYYY-MM-DD):"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST; // Alinea campos a la izquierda
        gbc.fill = GridBagConstraints.HORIZONTAL; // Rellena horizontalmente
        gbc.weightx = 1.0; // Permite que el campo crezca
        txtFecha = new JTextField(10);
        panelCampos.add(txtFecha, gbc);

        // Fila 1: Hora
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
        cmbResultado = new JComboBox<>(new String[]{"Recolección Exitosa", "Donante Ausente", "Recolección Parcial", "Cancelado"});
        panelCampos.add(cmbResultado, gbc);

        // Fila 3: ID Pedido (movido aquí para mejor flujo)
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
        txtIdPedido = new JTextField(10); // Inicializado aquí
        txtIdPedido.setEditable(false); // Hecho no editable
        panelCampos.add(txtIdPedido, gbc);

        // Fila 4: Observaciones (etiqueta)
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.NORTHEAST; // Alinea arriba y a la derecha
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panelCampos.add(new JLabel("Observaciones:"), gbc);

        // Fila 5: Observaciones (área de texto)
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2; // Ocupa ambas columnas
        gbc.fill = GridBagConstraints.BOTH; // Rellena en ambas direcciones
        gbc.weightx = 1.0; // Ocupa espacio horizontal
        gbc.weighty = 1.0; // Ocupa todo el espacio vertical restante
        txtObservaciones = new JTextArea(5, 20);
        panelCampos.add(new JScrollPane(txtObservaciones), gbc);

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // Alinea botones a la derecha
        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                guardarVisita();
            }
        });
        panelBotones.add(btnGuardar);

        // Añadir paneles al principal
        panelPrincipal.add(panelCampos, BorderLayout.CENTER);
        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);

        // Añadir un borde vacío para margen
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        add(panelPrincipal);
    }

    /**
     * Constructor que recibe el idPedido.
     * Llama al constructor principal y LUEGO asigna el idPedido al campo de texto.
     */
    public RegistrarVisitaDialog(IApi api, int idOrden, int idPedido) {
        this(api, idOrden); // Llama al constructor que construye la GUI
        this.idPedido = idPedido;
        
        // **CORRECCIÓN IMPORTANTE**: Asigna el valor DESPUÉS de que la GUI esté construida
        this.txtIdPedido.setText(String.valueOf(this.idPedido)); 
    }

    private void guardarVisita() {
        try {
            String fecha = txtFecha.getText();
            String hora = txtHora.getText();
            
            // Validación simple de formato
            if (!fecha.matches("\\d{4}-\\d{2}-\\d{2}")) {
                JOptionPane.showMessageDialog(this, "Formato de fecha incorrecto. Use YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!hora.matches("\\d{2}:\\d{2}")) {
                JOptionPane.showMessageDialog(this, "Formato de hora incorrecto. Use HH:MM.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String resultado = (String) cmbResultado.getSelectedItem();
            String observaciones = txtObservaciones.getText();

            LocalDateTime fechaHora = LocalDateTime.parse(fecha + "T" + hora);
            VisitaDTO visita = new VisitaDTO(fechaHora, resultado, observaciones);

            // Asumo que tu api.registrarVisita espera el ID de la ORDEN
            api.registrarVisita(idOrden, visita); 
            
            JOptionPane.showMessageDialog(this, "Visita registrada con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            
        } catch (DateTimeParseException ex) {
             JOptionPane.showMessageDialog(this, "Fecha u hora inválida. Verifique los valores.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        } 
        catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al registrar la visita: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}