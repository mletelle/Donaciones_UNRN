package ar.edu.unrn.seminario.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import ar.edu.unrn.seminario.api.IApi;
import ar.edu.unrn.seminario.dto.BienDTO;

public class ListadoInventario extends JFrame {

    private static final long serialVersionUID = 1L;
    private JTable table;
    private DefaultTableModel tableModel;
    private IApi api;
    private List<BienDTO> listaActualBienes; 

    public ListadoInventario(IApi api, String rol) {
        this.api = api;
        
        setTitle("Gestión de Inventario (En Stock)");
        setBounds(100, 100, 900, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panelPrincipal = new JPanel(new BorderLayout());

        String[] columnNames = {"ID", "Categoría", "Descripción", "Cantidad", "Estado", "Vencimiento"};
        
        tableModel = new DefaultTableModel(columnNames, 0) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        table = new JTable(tableModel);
        panelPrincipal.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        
        // Se elimino el boton "actualizar", no hacia nada.
        
        JButton btnAjustar = new JButton("Ajustar Stock / Editar");
        btnAjustar.addActionListener(e -> abrirEdicion());

        JButton btnDarDeBaja = new JButton("Dar de Baja");
        btnDarDeBaja.addActionListener(e -> darDeBaja());

        // Desactivar botón si es Donante
        if ("DONANTE".equals(rol)) {
            btnAjustar.setEnabled(false);
            btnAjustar.setToolTipText("Acción no permitida para Donantes");
            btnDarDeBaja.setEnabled(false);
            btnDarDeBaja.setToolTipText("Acción no permitida para Donantes");
        }

        buttonPanel.add(btnAjustar);
        buttonPanel.add(btnDarDeBaja);
        
        panelPrincipal.add(buttonPanel, BorderLayout.SOUTH);
        add(panelPrincipal);
        
        cargarInventario();
    }

    public void cargarInventario() {
        tableModel.setRowCount(0);
        listaActualBienes = api.obtenerInventario();
        
        if (listaActualBienes != null) {
            listaActualBienes.forEach(bien -> {
                Object[] fila = new Object[] {
                    bien.getId(),
                    bien.getCategoriaTexto(),
                    bien.getDescripcion(),
                    bien.getCantidad(),
                    bien.getEstadoTexto(),
                    bien.getVencimientoTexto()
                };
                tableModel.addRow(fila);
            });
        }
    }
    
    private void abrirEdicion() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un bien para editar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int idSeleccionado = (int) table.getValueAt(selectedRow, 0);
        
        BienDTO bienSeleccionado = listaActualBienes.stream()
                .filter(b -> b.getId() == idSeleccionado)
                .findFirst()
                .orElse(null);

        if (bienSeleccionado != null) {
            EditarBienDialog dialog = new EditarBienDialog(this, api, bienSeleccionado, this);
            dialog.setVisible(true);
        }
    }

    private void darDeBaja() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un bien para dar de baja.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idSeleccionado = (int) table.getValueAt(selectedRow, 0);
        String descripcion = (String) table.getValueAt(selectedRow, 2);

        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de que desea dar de baja el bien: " + descripcion + "?",
                "Confirmar Baja",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            try {
                String motivo = JOptionPane.showInputDialog(this, "Motivo de la baja:", "");
                if (motivo != null && !motivo.isEmpty()) {
                    api.darDeBajaBien(idSeleccionado, motivo);
                    JOptionPane.showMessageDialog(this, "Bien dado de baja exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    cargarInventario();
                } else if (motivo != null) {
                    JOptionPane.showMessageDialog(this, "El motivo no puede estar vacío.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al dar de baja: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}