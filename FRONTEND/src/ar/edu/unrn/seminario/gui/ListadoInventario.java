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
        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.addActionListener(e -> cargarInventario());
        
        JButton btnAjustar = new JButton("Ajustar Stock / Editar");
        btnAjustar.addActionListener(e -> abrirEdicion());

        // Desactivar botón si es Donante
        if ("DONANTE".equals(rol)) {
            btnAjustar.setEnabled(false);
            btnAjustar.setToolTipText("Acción no permitida para Donantes");
            //btnAjustar.setVisible(false);
        }

        buttonPanel.add(btnActualizar);
        buttonPanel.add(btnAjustar);
        
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
            // Se pasa 'this' como ventana padre para refrescar luego
            EditarBienDialog dialog = new EditarBienDialog(this, api, bienSeleccionado, this);
            dialog.setVisible(true);
        }
    }
}