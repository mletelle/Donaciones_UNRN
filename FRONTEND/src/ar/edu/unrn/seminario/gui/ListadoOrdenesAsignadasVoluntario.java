package ar.edu.unrn.seminario.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import ar.edu.unrn.seminario.api.IApi;
import ar.edu.unrn.seminario.dto.OrdenRetiroDTO;

public class ListadoOrdenesAsignadasVoluntario extends JFrame {

    private JTable tablaOrdenes;
    private DefaultTableModel modeloTabla;
    private IApi api;
    private String voluntarioActual;

    public ListadoOrdenesAsignadasVoluntario(IApi api, String voluntarioActual) {
        this.api = api;
        this.voluntarioActual = voluntarioActual;

        setTitle("Órdenes Asignadas");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panelPrincipal = new JPanel(new BorderLayout());
        modeloTabla = new DefaultTableModel(new Object[]{"ID", "Fecha", "Estado", "Descripción"}, 0);
        tablaOrdenes = new JTable(modeloTabla);
        JScrollPane scrollPane = new JScrollPane(tablaOrdenes);
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);

        JButton btnGestionar = new JButton("Gestionar Orden");
        btnGestionar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int filaSeleccionada = tablaOrdenes.getSelectedRow();
                if (filaSeleccionada != -1) {
                    int idOrden = (int) modeloTabla.getValueAt(filaSeleccionada, 0);
                    new GestionarOrdenVoluntario(api, idOrden).setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(ListadoOrdenesAsignadasVoluntario.this, "Seleccione una orden para gestionar.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        panelPrincipal.add(btnGestionar, BorderLayout.SOUTH);

        add(panelPrincipal);
        cargarOrdenesAsignadas();
    }

    public ListadoOrdenesAsignadasVoluntario(IApi api) {
        this(api, "Carlos"); // Hardcodear voluntario para pruebas
    }

    private void cargarOrdenesAsignadas() {
        List<OrdenRetiroDTO> ordenes = api.obtenerOrdenesAsignadas(voluntarioActual);
        for (OrdenRetiroDTO orden : ordenes) {
            modeloTabla.addRow(new Object[]{orden.getId(), orden.getFechaCreacion(), orden.getEstado(), orden.getDescripcion()});
        }
    }
}