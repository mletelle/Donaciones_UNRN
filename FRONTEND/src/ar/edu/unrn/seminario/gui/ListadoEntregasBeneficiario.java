package ar.edu.unrn.seminario.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import ar.edu.unrn.seminario.api.IApi;
import ar.edu.unrn.seminario.dto.OrdenEntregaDTO;

public class ListadoEntregasBeneficiario extends JFrame {

    public ListadoEntregasBeneficiario(IApi api, String usuarioBeneficiario) {
        setTitle("Mis Pedidos y Entregas - Usuario: " + usuarioBeneficiario);
        setSize(800, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        String[] columnNames = {"ID Orden", "Fecha Generación", "Estado", "Bienes Asignados"};
        
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        
        try {
            // Llamada a la API (ya implementada en PersistenceApi y MemoryApi)
            List<OrdenEntregaDTO> ordenes = api.obtenerEntregasPorBeneficiario(usuarioBeneficiario);
            
            if (ordenes != null && !ordenes.isEmpty()) {
                for (OrdenEntregaDTO o : ordenes) {
                    model.addRow(new Object[]{
                        o.getId(), 
                        o.getFecha(), 
                        o.getEstado(), 
                        o.getDescripcionBienes()
                    });
                }
            } else {
                JOptionPane.showMessageDialog(this, "No se encontraron órdenes para este beneficiario.", "Información", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        add(new JScrollPane(table), BorderLayout.CENTER);
        
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());
        
        JPanel btnPanel = new JPanel();
        btnPanel.add(btnCerrar);
        add(btnPanel, BorderLayout.SOUTH);
    }
}