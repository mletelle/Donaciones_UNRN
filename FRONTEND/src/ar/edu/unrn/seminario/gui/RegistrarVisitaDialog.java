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
    JButton btnGuardar = new JButton("Guardar");
    
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
        
        
        // Accion del boton "Guardar"
        btnGuardar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Deshabilitar el botón para evitar doble click
                btnGuardar.setEnabled(false); 
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
        // 1. Recuperar y validar datos (esto es rápido, se hace en el EDT)
        try {
            String fecha = txtFecha.getText();
            String hora = txtHora.getText();
            
            // ... (todas tus validaciones de formato y campos vacíos) ...
            // if (!fecha.matches(...)) { ... btnGuardar.setEnabled(true); return; }
            // if (observaciones.trim().isEmpty()) { ... btnGuardar.setEnabled(true); return; }

            String resultado = (String) cmbResultado.getSelectedItem();
            String observaciones = txtObservaciones.getText();
            LocalDateTime fechaHora = LocalDateTime.parse(fecha + "T" + hora);
            
            // Preparar el DTO
            String nombreDonante = api.obtenerNombreDonantePorId(idPedido); // Esto debería ser rápido, pero idealmente también iría al worker
            VisitaDTO visita = new VisitaDTO(fechaHora, resultado, observaciones, nombreDonante);

            // SwingWorker para que la GUI no se "tilde"/"lagee" cuando interactue con la BD
            // hasta que la BD responda
            SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() throws Exception {
                    // Esta es la única línea que se ejecuta en un hilo separado
                    api.registrarVisita(idOrden, idPedido, visita);
                    
                    return "OK"; // Éxito
                }

                @Override
                protected void done() {
                    try {
                        // get() lanza la excepción si doInBackground() falló
                        get(); 
                        
                        // Esto se ejecuta en el EDT después de que termina el worker
                        JOptionPane.showMessageDialog(RegistrarVisitaDialog.this, "Visita registrada con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                        
                        if (ventanaPadre != null) {
                            ventanaPadre.recargarDatos();
                        }
                        dispose(); // Cerrar el diálogo

                    } catch (Exception ex) {
                        // Si get() lanzó una excepción, la capturamos aquí
                        String mensajeError;
                        String tituloError = "Error";
                        
                        // Obtener la causa real de la excepción
                        Throwable causa = ex.getCause();
                        
                        if (causa instanceof ReglaNegocioException) {
                            // Error de negocio (ej. "Pedido ya completado")
                            mensajeError = causa.getMessage();
                            tituloError = "Operación no permitida";
                        } else if (causa instanceof RuntimeException) {
                            // Error de BD (ya lo envolvimos en RuntimeException en la API)
                            mensajeError = "Error de base de datos. Intente más tarde.\n" + causa.getMessage();
                        } else {
                            // Otro error (ej. DateTimeParseException que se nos pasó)
                            mensajeError = "Error inesperado: " + ex.getMessage();
                        }
                        
                        JOptionPane.showMessageDialog(RegistrarVisitaDialog.this, mensajeError, tituloError, JOptionPane.ERROR_MESSAGE);
                    } finally {
                        // 3. Reactivar el botón, pase lo que pase
                    	btnGuardar.setEnabled(true); 
                    }
                }
            };
            
            // 4. Ejecutar el worker
            worker.execute();

        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "La fecha u hora no tienen un formato válido.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
            btnGuardar.setEnabled(true); // Reactivar
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al preparar la visita: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            btnGuardar.setEnabled(true); // Reactivar
        }
    }
    
}