package ar.edu.unrn.seminario.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import ar.edu.unrn.seminario.api.IApi;
import ar.edu.unrn.seminario.dto.OrdenEntregaDTO;
import ar.edu.unrn.seminario.dto.UsuarioDTO;

public class GestionarEntregas extends JFrame {

    private IApi api;
    private UsuarioDTO voluntarioActual;
    private JTable tabla;
    private DefaultTableModel modelo;

    public GestionarEntregas(IApi api, UsuarioDTO voluntario) {
        this.api = api;
        this.voluntarioActual = voluntario;
        
        setTitle("Gestionar Órdenes de Entrega - Voluntario: " + voluntario.getNombre());
        setSize(700, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel panelTop = new JPanel();
        panelTop.add(new JLabel("Entregas Pendientes de Realización"));
        add(panelTop, BorderLayout.NORTH);

        // Tabla
        String[] cols = {"ID", "Fecha Generación", "Estado", "Bienes a Entregar"};
        modelo = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modelo);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // Botones
        JButton btnEntregar = new JButton("Confirmar Entrega Realizada");
        btnEntregar.addActionListener(e -> confirmarEntrega());
        
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());

        JPanel panelBtn = new JPanel();
        panelBtn.add(btnEntregar);
        panelBtn.add(btnCerrar);
        add(panelBtn, BorderLayout.SOUTH);

        cargarPendientes();
    }

    private void cargarPendientes() {
        modelo.setRowCount(0);
        try {
            List<OrdenEntregaDTO> lista = api.obtenerEntregasPendientes();
            if (lista != null) {
                for (OrdenEntregaDTO o : lista) {
                    modelo.addRow(new Object[]{o.getId(), o.getFecha(), o.getEstado(), o.getDescripcionBienes()});
                }
            }
            if (lista == null || lista.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay entregas pendientes en este momento.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error cargando pendientes: " + e.getMessage());
        }
    }

    private void confirmarEntrega() {
        int row = tabla.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una orden de la lista para marcar como entregada.");
            return;
        }

        int idOrden = (int) tabla.getValueAt(row, 0);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "¿Confirma que ha entregado los bienes al beneficiario?\nLa orden se marcará como COMPLETADA.", 
            "Confirmar Entrega", JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                api.completarEntrega(idOrden, voluntarioActual.getUsuario());
                JOptionPane.showMessageDialog(this, "¡Entrega registrada con éxito!");
                cargarPendientes(); // Refrescar lista
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al procesar: " + ex.getMessage());
            }
        }
    }
}