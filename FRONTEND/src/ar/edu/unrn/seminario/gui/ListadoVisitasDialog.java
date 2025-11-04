package ar.edu.unrn.seminario.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

import ar.edu.unrn.seminario.dto.VisitaDTO;
import ar.edu.unrn.seminario.api.IApi;

public class ListadoVisitasDialog extends JDialog {

    public ListadoVisitasDialog(IApi api, String voluntario) {
        setTitle("Historial de Visitas");
        setSize(600, 400);
        setModal(true);
        setLocationRelativeTo(null);

        String[] columnNames = {"Fecha", "Observaciones"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);

        List<VisitaDTO> visitas = api.obtenerVisitasPorVoluntario(voluntario);
        // Check if the list of visits is empty
        if (visitas == null || visitas.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay visitas registradas para este voluntario.", "Información", JOptionPane.INFORMATION_MESSAGE);
            dispose(); // Cierra el diálogo explícitamente
            return;
        }
        // Validate that each VisitaDTO has a non-null fecha
        for (VisitaDTO visita : visitas) {
            if (visita.getFechaDeVisita() == null) {
                JOptionPane.showMessageDialog(this, "Una de las visitas tiene una fecha nula. Verifique los datos.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        for (VisitaDTO visita : visitas) {
            tableModel.addRow(new Object[]{
                visita.getFechaDeVisita(),
                visita.getObservacion()
            });
        }

        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        add(scrollPane, BorderLayout.CENTER);

        JButton closeButton = new JButton("Cerrar");
        closeButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }
}