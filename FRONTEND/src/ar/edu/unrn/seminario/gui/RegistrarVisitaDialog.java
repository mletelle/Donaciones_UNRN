package ar.edu.unrn.seminario.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.concurrent.ExecutionException; // Importar para manejar excepciones del Worker

import ar.edu.unrn.seminario.api.IApi;
import ar.edu.unrn.seminario.dto.VisitaDTO;
// Importaciones de excepciones propias
import ar.edu.unrn.seminario.exception.CampoVacioException;
import ar.edu.unrn.seminario.exception.ObjetoNuloException;
import ar.edu.unrn.seminario.exception.ReglaNegocioException;

public class RegistrarVisitaDialog extends JDialog {

    private JTextField txtFecha;
    private JTextField txtHora;
    private JComboBox<String> cmbResultado;
    private JTextArea txtObservaciones;
    private JTextField txtIdPedido;
    private IApi api;
    private int idOrden;
    private int idPedido;
    private GestionarOrdenVoluntario ventanaPadre;
    JButton btnGuardar = new JButton("Guardar");
    
    public RegistrarVisitaDialog(IApi api, int idOrden) {
        this.api = api;
        this.idOrden = idOrden;

        setTitle("Registrar Visita");
        setSize(450, 350); 
        setModal(true);
        setLocationRelativeTo(null);

        JPanel panelPrincipal = new JPanel(new BorderLayout(5, 5));

        JPanel panelCampos = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.EAST;

        // Fecha
        gbc.gridx = 0;
        gbc.gridy = 0;
        panelCampos.add(new JLabel("Fecha (YYYY-MM-DD):"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtFecha = new JTextField(10);
        txtFecha.setText(java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")));
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
        txtHora.setText(java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
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

        // ID Pedido
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
        txtIdPedido = new JTextField(10);
        txtIdPedido.setEditable(false);
        panelCampos.add(txtIdPedido, gbc);

        // Observaciones
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.NORTHEAST;
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
                btnGuardar.setEnabled(false); 
                guardarVisita();
            }
        });
        
        panelBotones.add(btnGuardar);

        panelPrincipal.add(panelCampos, BorderLayout.CENTER);
        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);

        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        add(panelPrincipal);
    }

    // Metodos
    public RegistrarVisitaDialog(IApi api, int idOrden, int idPedido) {
        this(api, idOrden); 
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
            
            // --- USO DE CAMPOVACIOEXCEPTION (Validaciones de GUI) ---
            if (fecha == null || fecha.trim().isEmpty()) {
                throw new CampoVacioException("El campo Fecha no puede estar vacío.");
            }
            
            if (hora == null || hora.trim().isEmpty()) {
                throw new CampoVacioException("El campo Hora no puede estar vacío.");
            }
            
            // Validación simple de formato
            if (!fecha.matches("\\d{4}-\\d{2}-\\d{2}")) {
                 // Delegamos el error de formato a DateTimeParseException o lanzamos una excepción específica
                 // Aquí lanzamos una excepción runtime o usada para formato si se prefiere, 
                 // pero para este ejemplo usaremos el parseo posterior.
            }

            String observaciones = txtObservaciones.getText();
            if (observaciones == null || observaciones.trim().isEmpty()) {
                throw new CampoVacioException("El campo Observaciones es obligatorio.");
            }
            // --------------------------------------------------------

            String resultado = (String) cmbResultado.getSelectedItem();
            
            // Intentar parsear fecha/hora (Puede lanzar DateTimeParseException)
            LocalDateTime fechaHora = LocalDateTime.parse(fecha + "T" + hora);
            
            // SwingWorker para operación en segundo plano
            SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() throws Exception {
                    // Obtener nombre del donante (puede lanzar excepciones)
                    String nombreDonante = api.obtenerNombreDonantePorId(idPedido); 
                    
                    if (nombreDonante == null) {
                        // --- USO DE OBJETONULOEXCEPTION (Lógica interna) ---
                        throw new ObjetoNuloException("No se pudo obtener el nombre del donante para el pedido ID: " + idPedido);
                    }

                    VisitaDTO visita = new VisitaDTO(fechaHora, resultado, observaciones, nombreDonante);
                    
                    // Llamada a la API (puede lanzar ReglaNegocioException, ObjetoNuloException, etc.)
                    api.registrarVisita(idOrden, idPedido, visita);
                    
                    return "OK";
                }

                @Override
                protected void done() {
                    try {
                        get(); // Lanza la excepción ocurrida en doInBackground()
                        
                        JOptionPane.showMessageDialog(RegistrarVisitaDialog.this, "Visita registrada con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                        
                        if (ventanaPadre != null) {
                            ventanaPadre.recargarDatos();
                        }
                        dispose(); 

                    } catch (InterruptedException | ExecutionException ex) {
                        // Desempaquetar la excepción real (ExecutionException envuelve la original)
                        Throwable causa = ex.getCause();
                        
                        if (causa instanceof ReglaNegocioException) {
                            // --- MANEJO DE REGLANEGOCIOEXCEPTION ---
                            JOptionPane.showMessageDialog(RegistrarVisitaDialog.this, causa.getMessage(), "Operación no permitida", JOptionPane.WARNING_MESSAGE);
                        } else if (causa instanceof ObjetoNuloException) {
                            // --- MANEJO DE OBJETONULOEXCEPTION ---
                            JOptionPane.showMessageDialog(RegistrarVisitaDialog.this, causa.getMessage(), "Error de Datos", JOptionPane.ERROR_MESSAGE);
                        } else if (causa instanceof CampoVacioException) {
                             // Si la API lanza CampoVacioException (aunque ya validamos en GUI)
                            JOptionPane.showMessageDialog(RegistrarVisitaDialog.this, causa.getMessage(), "Error de Validación", JOptionPane.WARNING_MESSAGE);
                        } else {
                            // Otros errores (BD, conexión, etc.)
                            JOptionPane.showMessageDialog(RegistrarVisitaDialog.this, "Error inesperado: " + causa.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                            causa.printStackTrace();
                        }
                    } finally {
                    	btnGuardar.setEnabled(true); 
                    }
                }
            };
            
            worker.execute();

        } catch (CampoVacioException ex) {
            // Captura de validaciones de la GUI antes del Worker
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Datos Incompletos", JOptionPane.WARNING_MESSAGE);
            btnGuardar.setEnabled(true);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "La fecha u hora no tienen un formato válido (YYYY-MM-DD y HH:MM).", "Error de Formato", JOptionPane.ERROR_MESSAGE);
            btnGuardar.setEnabled(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al preparar la visita: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            btnGuardar.setEnabled(true);
        }
    }
    
}