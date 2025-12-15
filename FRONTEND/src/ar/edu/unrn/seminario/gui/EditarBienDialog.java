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

        // Agrandamos un poco la altura para que entre el nuevo campo
        setSize(400, 300); // <--- MODIFICADO (antes era 250)
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // Cambiamos el GridLayout de 3 filas a 4 filas para hacer lugar
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10)); // <--- MODIFICADO (4 filas)
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 1. ID (Igual que antes)
        formPanel.add(new JLabel("ID Bien:"));
        JTextField txtId = new JTextField(String.valueOf(bienDTO.getId()));
        txtId.setEditable(false);
        formPanel.add(txtId);

        // 2. Descripción (Igual que antes)
        formPanel.add(new JLabel("Descripción:"));
        txtDescripcion = new JTextField(bienDTO.getDescripcion());
        formPanel.add(txtDescripcion);

        // 3. Cantidad (Igual que antes)
        formPanel.add(new JLabel("Cantidad:"));
        txtCantidad = new JTextField(String.valueOf(bienDTO.getCantidad()));
        formPanel.add(txtCantidad);

        // 4. Vencimiento (TODO ESTO ES NUEVO)
        formPanel.add(new JLabel("Vencimiento:"));

        // Preparamos la fecha inicial.
        // Ojo acá: Si en la DB viene null, ponemos la fecha de hoy para que no explote la UI.
        // Esto es pensamiento de infraestructura: "Fail-safe".
        Date fechaInicial = bienDTO.getVencimiento();
        if (fechaInicial == null) {
            fechaInicial = new Date(); // Hoy
        }

        // Configuramos el modelo del Spinner para que entienda que son Fechas
        SpinnerDateModel modelFecha = new SpinnerDateModel(fechaInicial, null, null, Calendar.DAY_OF_MONTH);
        spinnerVencimiento = new JSpinner(modelFecha);

        // Le damos formato visual dd/MM/yyyy (Día/Mes/Año)
        JSpinner.DateEditor editorFecha = new JSpinner.DateEditor(spinnerVencimiento, "dd/MM/yyyy");
        spinnerVencimiento.setEditor(editorFecha);

        formPanel.add(spinnerVencimiento);
        // FIN DE LO NUEVO EN VISTA

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
            
            // Capturamos la fecha del spinner
            Date nuevaFechaVencimiento = (Date) spinnerVencimiento.getValue(); // <--- NUEVO

            // Actualizamos el DTO localmente
            bienDTO.setDescripcion(desc);
            bienDTO.setCantidad(cantidad);
            bienDTO.setVencimiento(nuevaFechaVencimiento); // <--- NUEVO: Asignamos al DTO

            api.actualizarBienInventario(bienDTO);

            JOptionPane.showMessageDialog(this, "Bien actualizado correctamente.");
            ventanaPadre.cargarInventario();
            dispose();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "La cantidad debe ser un número entero.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}