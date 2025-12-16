package ar.edu.unrn.seminario.gui;

import javax.swing.*;
import java.awt.*;
import java.util.Date; // <--- AGREGAR ESTA IMPORTACIÓN
import java.util.Calendar; // <--- AGREGAR ESTA IMPORTACIÓN
import ar.edu.unrn.seminario.api.IApi;
import ar.edu.unrn.seminario.dto.BienDTO;
import ar.edu.unrn.seminario.exception.*;

public class EditarBienDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private JTextField txtCantidad;
    private JTextField txtDescripcion;
    private JSpinner spinnerVencimiento; // <--- NUEVO COMPONENTE
    private IApi api;
    private BienDTO bienDTO;
    private ListadoInventario ventanaPadre;

    public EditarBienDialog(Window owner, IApi api, BienDTO bienDTO, ListadoInventario ventanaPadre) {
        super(owner, "Ajustar Bien de Inventario", ModalityType.APPLICATION_MODAL);
        this.api = api;
        this.bienDTO = bienDTO;
        this.ventanaPadre = ventanaPadre;

        setSize(400, 300);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10)); 
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        formPanel.add(new JLabel("ID Bien:"));
        JTextField txtId = new JTextField(String.valueOf(bienDTO.getId()));
        txtId.setEditable(false);
        formPanel.add(txtId);


        formPanel.add(new JLabel("Descripción:"));
        txtDescripcion = new JTextField(bienDTO.getDescripcion());
        formPanel.add(txtDescripcion);

        formPanel.add(new JLabel("Cantidad:"));
        txtCantidad = new JTextField(String.valueOf(bienDTO.getCantidad()));
        formPanel.add(txtCantidad);

        formPanel.add(new JLabel("Vencimiento:"));

        Date fechaInicial = bienDTO.getVencimiento();
        if (fechaInicial == null) {
            fechaInicial = new Date(); // Hoy
        }

        SpinnerDateModel modelFecha = new SpinnerDateModel(fechaInicial, null, null, Calendar.DAY_OF_MONTH);
        spinnerVencimiento = new JSpinner(modelFecha);

        JSpinner.DateEditor editorFecha = new JSpinner.DateEditor(spinnerVencimiento, "dd/MM/yyyy");
        spinnerVencimiento.setEditor(editorFecha);

        formPanel.add(spinnerVencimiento);

        add(formPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton btnGuardar = new JButton("Guardar Cambios");
        JButton btnCancelar = new JButton("Cancelar");

        btnGuardar.addActionListener(e -> guardar());
        btnCancelar.addActionListener(e -> dispose());

        btnPanel.add(btnGuardar);
        btnPanel.add(btnCancelar);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void guardar() {
        try {
            String desc = txtDescripcion.getText().trim();
            String cantStr = txtCantidad.getText().trim();

            if (desc.isEmpty()) throw new CampoVacioException("Descripción obligatoria.");
            if (cantStr.isEmpty()) throw new CampoVacioException("Cantidad obligatoria.");

            int cantidad = Integer.parseInt(cantStr);
            
            Date fechaDate = (Date) spinnerVencimiento.getValue();
            
            if (fechaDate != null) {
                java.time.LocalDate fechaNueva = fechaDate.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();

                if (fechaNueva.isBefore(java.time.LocalDate.now())) {
                    throw new Exception("No se puede asignar un vencimiento anterior a la fecha actual (" + java.time.LocalDate.now() + ").");
                }
            }

            bienDTO.setDescripcion(desc);
            bienDTO.setCantidad(cantidad);
            bienDTO.setVencimiento(fechaDate); 

            api.actualizarBienInventario(bienDTO);

            JOptionPane.showMessageDialog(this, "Bien actualizado correctamente.");
            ventanaPadre.cargarInventario();
            dispose();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "La cantidad debe ser un número entero.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }
}